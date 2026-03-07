package com.denizcan.astrosea.presentation.premium

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adapty.Adapty
import com.denizcan.astrosea.billing.BillingConfig
import com.denizcan.astrosea.billing.BillingManager
import com.denizcan.astrosea.billing.BillingState
import com.denizcan.astrosea.billing.SubscriptionProduct
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class PremiumUiState(
    val isLoading: Boolean = false,
    val products: List<SubscriptionProduct> = emptyList(),
    val selectedProductIndex: Int = 1, // Varsayılan: Aylık
    val isPurchasing: Boolean = false,
    val purchaseSuccess: Boolean = false,
    val errorMessage: String? = null,
    val showConfirmDialog: Boolean = false,
    val isTestMode: Boolean = BillingConfig.TEST_MODE
)

class PremiumViewModel(
    private val context: Context
) : ViewModel() {
    
    companion object {
        private const val TAG = "PremiumViewModel"
    }
    
    private val billingManager = BillingManager.getInstance(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()
    
    init {
        identifyUser()
        observeBillingState()
        loadProducts()
    }
    
    private fun identifyUser() {
        val userId = auth.currentUser?.uid ?: return
        Adapty.identify(userId) { error ->
            if (error != null) {
                Log.e(TAG, "Adapty kullanıcı tanımlama hatası: ${error.message}")
            } else {
                Log.d(TAG, "Adapty kullanıcı tanımlandı: $userId")
            }
        }
    }
    
    private fun observeBillingState() {
        viewModelScope.launch {
            billingManager.billingState.collect { state ->
                when (state) {
                    is BillingState.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is BillingState.Connected -> {
                        Log.d(TAG, "Billing bağlantısı kuruldu")
                    }
                    is BillingState.ProductsLoaded -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            products = state.products
                        )
                    }
                    is BillingState.PurchaseSuccess -> {
                        Log.d(TAG, "Satın alma başarılı: ${state.productId}")
                        handlePurchaseSuccess(state.productId)
                    }
                    is BillingState.PurchaseCancelled -> {
                        _uiState.value = _uiState.value.copy(
                            isPurchasing = false,
                            errorMessage = state.message
                        )
                    }
                    is BillingState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isPurchasing = false,
                            errorMessage = state.message
                        )
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun loadProducts() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        billingManager.startConnection()
    }
    
    fun selectProduct(index: Int) {
        _uiState.value = _uiState.value.copy(selectedProductIndex = index)
    }
    
    /**
     * Satın alma onay dialogunu göster
     */
    fun showPurchaseConfirmation() {
        _uiState.value = _uiState.value.copy(showConfirmDialog = true)
    }
    
    /**
     * Onay dialogunu kapat
     */
    fun dismissConfirmDialog() {
        _uiState.value = _uiState.value.copy(showConfirmDialog = false)
    }
    
    /**
     * Satın almayı başlat
     */
    fun startPurchase(activity: Activity) {
        val selectedProduct = _uiState.value.products.getOrNull(_uiState.value.selectedProductIndex)
        if (selectedProduct == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Lütfen bir plan seçin")
            return
        }
        
        _uiState.value = _uiState.value.copy(
            isPurchasing = true,
            showConfirmDialog = false
        )
        
        billingManager.launchPurchaseFlow(activity, selectedProduct.productId)
    }
    
    /**
     * Satın alma başarılı olduğunda
     */
    private fun handlePurchaseSuccess(productId: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        errorMessage = "Kullanıcı oturumu bulunamadı"
                    )
                    return@launch
                }
                
                // Premium süresini hesapla
                val product = _uiState.value.products.find { it.productId == productId }
                val durationDays = product?.durationDays ?: 30
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                
                val startDate = Date()
                val calendar = Calendar.getInstance()
                calendar.time = startDate
                calendar.add(Calendar.DAY_OF_YEAR, durationDays)
                val endDate = calendar.time
                
                // Tarihleri String olarak kaydet (ProfileData ile uyumlu)
                val premiumData = mapOf(
                    "isPremium" to true,
                    "premiumStartDate" to dateFormat.format(startDate),
                    "premiumEndDate" to dateFormat.format(endDate),
                    "premiumProductId" to productId,
                    "premiumPurchaseDate" to dateFormat.format(Date()),
                    "isTestPurchase" to BillingConfig.TEST_MODE
                )
                
                firestore.collection("users")
                    .document(userId)
                    .set(premiumData, SetOptions.merge())
                    .await()
                
                Log.d(TAG, "Premium durumu Firestore'a kaydedildi")
                
                _uiState.value = _uiState.value.copy(
                    isPurchasing = false,
                    purchaseSuccess = true
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Premium kaydetme hatası", e)
                _uiState.value = _uiState.value.copy(
                    isPurchasing = false,
                    errorMessage = "Premium durumu kaydedilemedi: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Satın almaları geri yükle (Restore Purchases)
     */
    fun restorePurchases() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        billingManager.restorePurchases { hasAccess ->
            if (hasAccess) {
                viewModelScope.launch {
                    try {
                        val userId = auth.currentUser?.uid ?: return@launch
                        val premiumData = mapOf(
                            "isPremium" to true,
                            "premiumRestoredAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                        )
                        firestore.collection("users")
                            .document(userId)
                            .set(premiumData, SetOptions.merge())
                            .await()
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            purchaseSuccess = true
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Restore kaydetme hatası", e)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Geri yükleme kaydedilemedi"
                        )
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Geri yüklenecek satın alma bulunamadı"
                )
            }
        }
    }

    /**
     * Hata mesajını temizle
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Başarı durumunu sıfırla
     */
    fun resetPurchaseSuccess() {
        _uiState.value = _uiState.value.copy(purchaseSuccess = false)
        billingManager.resetState()
    }
    
    override fun onCleared() {
        super.onCleared()
        // BillingManager singleton olduğu için burada kapatmıyoruz
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PremiumViewModel::class.java)) {
                return PremiumViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

