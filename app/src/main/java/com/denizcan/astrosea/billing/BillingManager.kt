package com.denizcan.astrosea.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.adapty.Adapty
import com.adapty.models.AdaptyPaywall
import com.adapty.models.AdaptyPaywallProduct
import com.adapty.models.AdaptyPeriodUnit
import com.adapty.models.AdaptyPurchaseResult
import com.adapty.utils.AdaptyResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BillingConfig {
    // 🔧 TEST MODU - Geliştirme için true, yayınlamadan önce false yap
    const val TEST_MODE = true

    // Adapty Placement ID - Dashboard'da oluşturacaksın
    const val PLACEMENT_ID = "premium"

    // Access Level ID - Dashboard'da tanımlı
    const val ACCESS_LEVEL = "premium"

    // Ürün ID'leri (Google Play Console'da oluşturunca Adapty'de eşle)
    const val PRODUCT_WEEKLY = "astrosea_weekly"
    const val PRODUCT_MONTHLY = "astrosea_monthly"
    const val PRODUCT_YEARLY = "astrosea_yearly"

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
    val isPopular: Boolean = false,
    val adaptyProduct: AdaptyPaywallProduct? = null
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

    private var adaptyProducts: List<AdaptyPaywallProduct> = emptyList()

    private val testProducts = listOf(
        SubscriptionProduct(
            productId = BillingConfig.PRODUCT_WEEKLY,
            name = "Haftalık",
            price = "₺49.99",
            duration = "/hafta",
            durationDays = BillingConfig.DURATION_WEEKLY,
            isPopular = false
        ),
        SubscriptionProduct(
            productId = BillingConfig.PRODUCT_MONTHLY,
            name = "Aylık",
            price = "₺99.99",
            duration = "/ay",
            durationDays = BillingConfig.DURATION_MONTHLY,
            isPopular = true
        ),
        SubscriptionProduct(
            productId = BillingConfig.PRODUCT_YEARLY,
            name = "Yıllık",
            price = "₺599.99",
            duration = "/yıl",
            durationDays = BillingConfig.DURATION_YEARLY,
            pricePerMonth = "Aylık ₺50",
            isPopular = false
        )
    )

    fun startConnection() {
        if (BillingConfig.TEST_MODE) {
            Log.d(TAG, "TEST MODU: Adapty bağlantısı simüle ediliyor")
            _billingState.value = BillingState.Connected
            _billingState.value = BillingState.ProductsLoaded(testProducts)
            return
        }

        _billingState.value = BillingState.Loading

        // Adapty üzerinden paywall ve ürünleri çek
        Adapty.getPaywall(BillingConfig.PLACEMENT_ID) { result ->
            when (result) {
                is AdaptyResult.Success -> {
                    val paywall = result.value
                    Log.d(TAG, "Adapty paywall yüklendi: ${paywall.placement.id}")
                    loadProducts(paywall)
                }
                is AdaptyResult.Error -> {
                    Log.e(TAG, "Adapty paywall hatası: ${result.error.message}")
                    _billingState.value = BillingState.Error("Ürünler yüklenemedi: ${result.error.message}")
                }
            }
        }
    }

    private fun loadProducts(paywall: AdaptyPaywall) {
        Adapty.getPaywallProducts(paywall) { result ->
            when (result) {
                is AdaptyResult.Success -> {
                    adaptyProducts = result.value
                    
                    val products = adaptyProducts.map { adaptyProduct ->
                        val subscriptionDetails = adaptyProduct.subscriptionDetails
                        val periodUnit = subscriptionDetails?.subscriptionPeriod?.unit
                        
                        val (duration, durationDays, isPopular) = when (periodUnit) {
                            AdaptyPeriodUnit.WEEK -> Triple("/hafta", 7, false)
                            AdaptyPeriodUnit.MONTH -> Triple("/ay", 30, true)
                            AdaptyPeriodUnit.YEAR -> Triple("/yıl", 365, false)
                            else -> Triple("", 30, false)
                        }
                        
                        SubscriptionProduct(
                            productId = adaptyProduct.vendorProductId,
                            name = adaptyProduct.localizedTitle,
                            price = adaptyProduct.price.localizedString,
                            duration = duration,
                            durationDays = durationDays,
                            pricePerMonth = if (durationDays == 365) calculateMonthlyPrice(adaptyProduct) else null,
                            isPopular = isPopular,
                            adaptyProduct = adaptyProduct
                        )
                    }

                    Log.d(TAG, "Adapty ürünleri yüklendi: ${products.size} adet")
                    _billingState.value = BillingState.ProductsLoaded(products)
                }
                is AdaptyResult.Error -> {
                    Log.e(TAG, "Adapty ürün yükleme hatası: ${result.error.message}")
                    _billingState.value = BillingState.Error("Ürünler yüklenemedi")
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        if (BillingConfig.TEST_MODE) {
            Log.d(TAG, "TEST MODU: Simüle edilmiş satın alma - $productId")
            _billingState.value = BillingState.PurchaseSuccess(productId)
            return
        }

        val product = adaptyProducts.find { it.vendorProductId == productId }
        if (product == null) {
            _billingState.value = BillingState.Error("Ürün bulunamadı: $productId")
            return
        }

        Adapty.makePurchase(activity, product, null) { result ->
            when (result) {
                is AdaptyResult.Success -> {
                    when (val purchaseResult = result.value) {
                        is AdaptyPurchaseResult.Success -> {
                            val profile = purchaseResult.profile
                            val hasAccess = profile.accessLevels[BillingConfig.ACCESS_LEVEL]?.isActive == true
                            Log.d(TAG, "Satın alma başarılı! Premium erişim: $hasAccess")
                            _billingState.value = BillingState.PurchaseSuccess(productId)
                        }
                        is AdaptyPurchaseResult.UserCanceled -> {
                            Log.d(TAG, "Kullanıcı satın almayı iptal etti")
                            _billingState.value = BillingState.PurchaseCancelled("Satın alma iptal edildi")
                        }
                        is AdaptyPurchaseResult.Pending -> {
                            Log.d(TAG, "Satın alma beklemede")
                            _billingState.value = BillingState.PurchaseCancelled("Satın alma işlemi beklemede")
                        }
                    }
                }
                is AdaptyResult.Error -> {
                    Log.e(TAG, "Adapty satın alma hatası: ${result.error.message}")
                    _billingState.value = BillingState.Error("Satın alma hatası: ${result.error.message}")
                }
            }
        }
    }

    /**
     * Kullanıcının premium erişimi olup olmadığını Adapty'den kontrol et
     */
    fun checkPremiumAccess(onResult: (Boolean) -> Unit) {
        if (BillingConfig.TEST_MODE) {
            Log.d(TAG, "TEST MODU: Premium erişim kontrolü atlandı")
            onResult(false)
            return
        }

        Adapty.getProfile { result ->
            when (result) {
                is AdaptyResult.Success -> {
                    val hasAccess = result.value.accessLevels[BillingConfig.ACCESS_LEVEL]?.isActive == true
                    Log.d(TAG, "Premium erişim durumu: $hasAccess")
                    onResult(hasAccess)
                }
                is AdaptyResult.Error -> {
                    Log.e(TAG, "Profil kontrol hatası: ${result.error.message}")
                    onResult(false)
                }
            }
        }
    }

    /**
     * Satın almaları geri yükle
     */
    fun restorePurchases(onResult: (Boolean) -> Unit) {
        if (BillingConfig.TEST_MODE) {
            Log.d(TAG, "TEST MODU: Restore atlandı")
            onResult(false)
            return
        }

        Adapty.restorePurchases { result ->
            when (result) {
                is AdaptyResult.Success -> {
                    val hasAccess = result.value.accessLevels[BillingConfig.ACCESS_LEVEL]?.isActive == true
                    Log.d(TAG, "Restore sonucu: premium=$hasAccess")
                    onResult(hasAccess)
                }
                is AdaptyResult.Error -> {
                    Log.e(TAG, "Restore hatası: ${result.error.message}")
                    onResult(false)
                }
            }
        }
    }

    fun resetState() {
        _billingState.value = BillingState.Idle
    }

    private fun calculateMonthlyPrice(product: AdaptyPaywallProduct): String? {
        val priceAmount = product.price.amount
        val monthly = priceAmount.toDouble() / 12
        return "Aylık ₺${String.format("%.0f", monthly)}"
    }
}
