package com.denizcan.astrosea.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Google Play Billing yönetim sınıfı
 * TEST_MODE = true iken gerçek ödeme almaz, simülasyon yapar
 */
object BillingConfig {
    // 🔧 TEST MODU - Geliştirme için true, yayınlamadan önce false yap
    const val TEST_MODE = true
    
    // Ürün ID'leri (Google Play Console'da oluşturunca güncelle)
    const val PRODUCT_WEEKLY = "astrosea_weekly"
    const val PRODUCT_MONTHLY = "astrosea_monthly"
    const val PRODUCT_YEARLY = "astrosea_yearly"
    
    // Abonelik süreleri (gün cinsinden)
    const val DURATION_WEEKLY = 7
    const val DURATION_MONTHLY = 30
    const val DURATION_YEARLY = 365
}

data class SubscriptionProduct(
    val productId: String,
    val name: String,
    val price: String,
    val duration: String,
    val durationDays: Int,
    val pricePerMonth: String? = null,
    val isPopular: Boolean = false
)

sealed class BillingState {
    object Idle : BillingState()
    object Loading : BillingState()
    object Connected : BillingState()
    object Disconnected : BillingState()
    data class ProductsLoaded(val products: List<SubscriptionProduct>) : BillingState()
    data class PurchaseSuccess(val productId: String) : BillingState()
    data class PurchaseCancelled(val message: String) : BillingState()
    data class Error(val message: String) : BillingState()
}

class BillingManager(private val context: Context) {
    
    companion object {
        private const val TAG = "BillingManager"
        
        @Volatile
        private var INSTANCE: BillingManager? = null
        
        fun getInstance(context: Context): BillingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()
    
    private var billingClient: BillingClient? = null
    private var productDetailsList: List<ProductDetails> = emptyList()
    
    // Test modu için sabit ürünler
    private val testProducts = listOf(
        SubscriptionProduct(
            productId = BillingConfig.PRODUCT_WEEKLY,
            name = "Haftalık",
            price = "25 ₺",
            duration = "/hafta",
            durationDays = BillingConfig.DURATION_WEEKLY,
            pricePerMonth = null,
            isPopular = false
        ),
        SubscriptionProduct(
            productId = BillingConfig.PRODUCT_MONTHLY,
            name = "Aylık",
            price = "40 ₺",
            duration = "/ay",
            durationDays = BillingConfig.DURATION_MONTHLY,
            pricePerMonth = null,
            isPopular = true
        ),
        SubscriptionProduct(
            productId = BillingConfig.PRODUCT_YEARLY,
            name = "Yıllık",
            price = "400 ₺",
            duration = "/yıl",
            durationDays = BillingConfig.DURATION_YEARLY,
            pricePerMonth = "33 ₺/ay",
            isPopular = false
        )
    )
    
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Kullanıcı satın almayı iptal etti")
                _billingState.value = BillingState.PurchaseCancelled("Satın alma iptal edildi")
            }
            else -> {
                Log.e(TAG, "Satın alma hatası: ${billingResult.debugMessage}")
                _billingState.value = BillingState.Error("Satın alma hatası: ${billingResult.debugMessage}")
            }
        }
    }
    
    /**
     * BillingClient'ı başlat ve Google Play'e bağlan
     */
    fun startConnection() {
        if (BillingConfig.TEST_MODE) {
            Log.d(TAG, "TEST MODU: Gerçek bağlantı yapılmıyor")
            _billingState.value = BillingState.Connected
            _billingState.value = BillingState.ProductsLoaded(testProducts)
            return
        }
        
        _billingState.value = BillingState.Loading
        
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Google Play Billing bağlantısı başarılı")
                    _billingState.value = BillingState.Connected
                    queryProducts()
                } else {
                    Log.e(TAG, "Bağlantı hatası: ${billingResult.debugMessage}")
                    _billingState.value = BillingState.Error("Bağlantı hatası: ${billingResult.debugMessage}")
                }
            }
            
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Google Play Billing bağlantısı kesildi")
                _billingState.value = BillingState.Disconnected
                // Yeniden bağlanmayı dene
                startConnection()
            }
        })
    }
    
    /**
     * Mevcut abonelikleri sorgula
     */
    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BillingConfig.PRODUCT_WEEKLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BillingConfig.PRODUCT_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BillingConfig.PRODUCT_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        
        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                this.productDetailsList = productDetailsList
                
                val products = productDetailsList.mapNotNull { details ->
                    val offerDetails = details.subscriptionOfferDetails?.firstOrNull()
                    val pricingPhase = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()
                    
                    if (pricingPhase != null) {
                        SubscriptionProduct(
                            productId = details.productId,
                            name = details.name,
                            price = pricingPhase.formattedPrice,
                            duration = getDurationString(pricingPhase.billingPeriod),
                            durationDays = getDurationDays(pricingPhase.billingPeriod),
                            pricePerMonth = calculateMonthlyPrice(details),
                            isPopular = details.productId == BillingConfig.PRODUCT_MONTHLY
                        )
                    } else null
                }
                
                Log.d(TAG, "Ürünler yüklendi: ${products.size} adet")
                _billingState.value = BillingState.ProductsLoaded(products)
            } else {
                Log.e(TAG, "Ürün sorgulama hatası: ${billingResult.debugMessage}")
                _billingState.value = BillingState.Error("Ürünler yüklenemedi")
            }
        }
    }
    
    /**
     * Satın alma akışını başlat
     */
    fun launchPurchaseFlow(activity: Activity, productId: String) {
        if (BillingConfig.TEST_MODE) {
            Log.d(TAG, "TEST MODU: Simüle edilmiş satın alma - $productId")
            // Test modunda direkt başarılı döndür
            _billingState.value = BillingState.PurchaseSuccess(productId)
            return
        }
        
        val productDetails = productDetailsList.find { it.productId == productId }
        if (productDetails == null) {
            _billingState.value = BillingState.Error("Ürün bulunamadı: $productId")
            return
        }
        
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            _billingState.value = BillingState.Error("Teklif bulunamadı")
            return
        }
        
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        
        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }
    
    /**
     * Satın almayı işle ve onayla
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Satın alma başarılı
            if (!purchase.isAcknowledged) {
                // Satın almayı onayla
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Satın alma onaylandı")
                        val productId = purchase.products.firstOrNull() ?: ""
                        _billingState.value = BillingState.PurchaseSuccess(productId)
                    } else {
                        Log.e(TAG, "Onaylama hatası: ${billingResult.debugMessage}")
                        _billingState.value = BillingState.Error("Satın alma onaylanamadı")
                    }
                }
            } else {
                val productId = purchase.products.firstOrNull() ?: ""
                _billingState.value = BillingState.PurchaseSuccess(productId)
            }
        }
    }
    
    /**
     * Mevcut abonelikleri kontrol et
     */
    fun queryExistingPurchases(onResult: (List<Purchase>) -> Unit) {
        if (BillingConfig.TEST_MODE) {
            Log.d(TAG, "TEST MODU: Mevcut abonelik yok")
            onResult(emptyList())
            return
        }
        
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        
        billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                onResult(purchases)
            } else {
                Log.e(TAG, "Abonelik sorgulama hatası: ${billingResult.debugMessage}")
                onResult(emptyList())
            }
        }
    }
    
    /**
     * Test ürünlerini al
     */
    fun getTestProducts(): List<SubscriptionProduct> = testProducts
    
    /**
     * State'i sıfırla
     */
    fun resetState() {
        _billingState.value = BillingState.Idle
    }
    
    /**
     * Bağlantıyı kapat
     */
    fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
    }
    
    // Yardımcı fonksiyonlar
    private fun getDurationString(billingPeriod: String): String {
        return when {
            billingPeriod.contains("W") -> "/hafta"
            billingPeriod.contains("M") -> "/ay"
            billingPeriod.contains("Y") -> "/yıl"
            else -> ""
        }
    }
    
    private fun getDurationDays(billingPeriod: String): Int {
        return when {
            billingPeriod.contains("W") -> 7
            billingPeriod.contains("M") -> 30
            billingPeriod.contains("Y") -> 365
            else -> 30
        }
    }
    
    private fun calculateMonthlyPrice(productDetails: ProductDetails): String? {
        val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
        val pricingPhase = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()
        
        if (pricingPhase?.billingPeriod?.contains("Y") == true) {
            val yearlyMicros = pricingPhase.priceAmountMicros
            val monthlyMicros = yearlyMicros / 12
            val monthlyPrice = monthlyMicros / 1_000_000.0
            return String.format("%.0f ₺/ay", monthlyPrice)
        }
        return null
    }
}

