package com.denizcan.astrosea.billing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.adapty.models.AdaptyProfile
import android.app.Activity
import android.content.Context
import android.util.Log
import com.adapty.Adapty
import com.adapty.models.AdaptyPaywall
import com.adapty.models.AdaptyPaywallProduct
import com.adapty.models.AdaptyPeriodUnit
import com.adapty.models.AdaptyPurchaseResult
import com.adapty.utils.AdaptyResult
import com.denizcan.astrosea.BuildConfig
import com.denizcan.astrosea.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object BillingConfig {
    val TEST_MODE: Boolean = BuildConfig.BILLING_TEST_MODE

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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private var adaptyProducts: List<AdaptyPaywallProduct> = emptyList()

    private val testProducts: List<SubscriptionProduct>
        get() = listOf(
        SubscriptionProduct(
            productId = BillingConfig.PRODUCT_WEEKLY,
            name = context.getString(R.string.prem_plan_weekly_short),
            price = "₺49.99",
            duration = context.getString(R.string.prem_duration_week),
            durationDays = BillingConfig.DURATION_WEEKLY,
            isPopular = false
        ),
        SubscriptionProduct(
            productId = BillingConfig.PRODUCT_MONTHLY,
            name = context.getString(R.string.prem_plan_monthly_short),
            price = "₺99.99",
            duration = context.getString(R.string.prem_duration_month),
            durationDays = BillingConfig.DURATION_MONTHLY,
            isPopular = true
        ),
        SubscriptionProduct(
            productId = BillingConfig.PRODUCT_YEARLY,
            name = context.getString(R.string.prem_plan_yearly_short),
            price = "₺599.99",
            duration = context.getString(R.string.prem_duration_year),
            durationDays = BillingConfig.DURATION_YEARLY,
            pricePerMonth = context.getString(R.string.prem_price_monthly_fallback),
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
                    _billingState.value = BillingState.Error(context.getString(R.string.prem_products_load_failed))
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
                            AdaptyPeriodUnit.WEEK -> Triple(context.getString(R.string.prem_duration_week), 7, false)
                            AdaptyPeriodUnit.MONTH -> Triple(context.getString(R.string.prem_duration_month), 30, true)
                            AdaptyPeriodUnit.YEAR -> Triple(context.getString(R.string.prem_duration_year), 365, false)
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
                    _billingState.value = BillingState.Error(context.getString(R.string.prem_products_load_failed))
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val product = adaptyProducts.find { it.vendorProductId == productId }
        if (product == null) {
            _billingState.value = BillingState.Error(context.getString(R.string.prem_product_not_found, productId))
            return
        }
        scope.launch {
            try {
                val result = MembershipRepository.withBillingIdentity {
                    suspendCoroutine<AdaptyResult<AdaptyPurchaseResult>> { continuation ->
                        Adapty.makePurchase(activity, product, null) { continuation.resume(it) }
                    }
                }
                _billingState.value = when (result) {
                    is AdaptyResult.Success -> when (result.value) {
                        is AdaptyPurchaseResult.Success -> BillingState.PurchaseSuccess(productId)
                        is AdaptyPurchaseResult.UserCanceled -> BillingState.PurchaseCancelled(context.getString(R.string.prem_purchase_cancelled))
                        is AdaptyPurchaseResult.Pending -> BillingState.PurchaseCancelled(context.getString(R.string.prem_purchase_pending))
                    }
                    is AdaptyResult.Error -> BillingState.Error(context.getString(R.string.prem_restore_failed))
                }
            } catch (error: Exception) {
                _billingState.value = BillingState.Error(context.getString(R.string.membership_unavailable))
            }
        }
    }

    fun checkPremiumAccess(onResult: (Boolean) -> Unit) {
        scope.launch {
            val membership = runCatching { MembershipRepository.refresh() }.getOrNull()
            if (membership != null) onResult(membership.hasAccess)
        }
    }

    fun restorePurchases(onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                val result = MembershipRepository.withBillingIdentity {
                    suspendCoroutine<AdaptyResult<AdaptyProfile>> { continuation ->
                        Adapty.restorePurchases { continuation.resume(it) }
                    }
                }
                when (result) {
                    is AdaptyResult.Success -> onResult(MembershipRepository.refresh().hasAccess)
                    is AdaptyResult.Error -> {
                        _billingState.value = BillingState.Error(context.getString(R.string.prem_restore_failed))
                    }
                }
            } catch (error: Exception) {
                _billingState.value = BillingState.Error(context.getString(R.string.membership_unavailable))
            }
        }
    }

    fun resetState() {
        _billingState.value = BillingState.Idle
    }

    private fun calculateMonthlyPrice(product: AdaptyPaywallProduct): String? {
        return runCatching {
            val monthly = product.price.amount.toDouble() / 12
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                currency = Currency.getInstance(product.price.currencyCode)
            }
            context.getString(R.string.prem_monthly_equivalent, formatter.format(monthly))
        }.getOrNull()
    }
}
