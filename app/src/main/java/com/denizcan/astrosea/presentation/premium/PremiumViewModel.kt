package com.denizcan.astrosea.presentation.premium

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.denizcan.astrosea.R
import com.denizcan.astrosea.billing.BillingConfig
import com.denizcan.astrosea.billing.BillingManager
import com.denizcan.astrosea.billing.BillingState
import com.denizcan.astrosea.billing.SubscriptionProduct
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PremiumUiState(
    val isLoading: Boolean = false,
    val hasPremiumAccess: Boolean = false,
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
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            com.denizcan.astrosea.billing.MembershipRepository.state.collect { membership ->
                _uiState.value = _uiState.value.copy(hasPremiumAccess = membership.uid == auth.currentUser?.uid && membership.hasAccess)
            }
        }
        viewModelScope.launch { runCatching { com.denizcan.astrosea.billing.MembershipRepository.refresh() } }
        observeBillingState()
        loadProducts()
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
                        val monthlyPlanIndex = state.products.indexOfFirst {
                            it.durationDays == BillingConfig.DURATION_MONTHLY
                        }.takeIf { it >= 0 } ?: 0
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            products = state.products,
                            selectedProductIndex = monthlyPlanIndex
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
        if (_uiState.value.isPurchasing) return
        _uiState.value = _uiState.value.copy(isPurchasing = true)
        viewModelScope.launch {
            try {
                val membership = com.denizcan.astrosea.billing.MembershipRepository.refresh()
                _uiState.value = _uiState.value.copy(isPurchasing = false,
                    purchaseSuccess = membership.hasAccess, showConfirmDialog = !membership.hasAccess)
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(isPurchasing = false,
                    errorMessage = context.getString(R.string.membership_unavailable))
            }
        }
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
            _uiState.value = _uiState.value.copy(errorMessage = context.getString(R.string.prem_select_plan))
            return
        }
        
        _uiState.value = _uiState.value.copy(
            isPurchasing = true,
            showConfirmDialog = false
        )
        
        viewModelScope.launch {
            try {
                val membership = com.denizcan.astrosea.billing.MembershipRepository.refresh()
                if (membership.hasAccess) {
                    _uiState.value = _uiState.value.copy(isPurchasing = false, purchaseSuccess = true)
                } else {
                    billingManager.launchPurchaseFlow(activity, selectedProduct.productId)
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(isPurchasing = false,
                    errorMessage = context.getString(R.string.membership_unavailable))
            }
        }
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
                        errorMessage = context.getString(R.string.prem_no_session)
                    )
                    return@launch
                }
                
                val membership = com.denizcan.astrosea.billing.MembershipRepository.refresh()
                if (!membership.hasAccess) {
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        errorMessage = context.getString(R.string.membership_pending)
                    )
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    isPurchasing = false,
                    purchaseSuccess = true
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Premium kaydetme hatası", e)
                _uiState.value = _uiState.value.copy(
                    isPurchasing = false,
                    errorMessage = context.getString(R.string.membership_pending)
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
                        val membership = com.denizcan.astrosea.billing.MembershipRepository.refresh()
                        if (!membership.hasAccess) {
                            _uiState.value = _uiState.value.copy(isLoading = false,
                                errorMessage = context.getString(R.string.membership_pending))
                            return@launch
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            purchaseSuccess = true
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Restore kaydetme hatası", e)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = context.getString(R.string.prem_restore_failed)
                        )
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = context.getString(R.string.prem_nothing_to_restore)
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

