package com.denizcan.astrosea.billing

import com.denizcan.astrosea.presentation.premium.PremiumUiState
import org.junit.Assert.*
import org.junit.Test

class PremiumUiStateTest {
    private val product = SubscriptionProduct("monthly", "Monthly", "3.49", "/month", 30)

    @Test fun plansDisplayWeeklyMonthlyYearlyRegardlessOfStoreOrder() {
        val weekly = SubscriptionProduct("weekly", "Silver", "59.99", "/week", 7)
        val yearly = SubscriptionProduct("yearly", "Platinum", "1779.99", "/year", 365)

        val ordered = listOf(yearly, product, weekly).inDisplayOrder()

        assertEquals(listOf(weekly, product, yearly), ordered)
        assertSame(weekly, ordered[0])
        assertSame(product, ordered[1])
        assertSame(yearly, ordered[2])
    }

    @Test fun emptyCatalogStaysEmptyWhenOrdered() {
        assertTrue(emptyList<SubscriptionProduct>().inDisplayOrder().isEmpty())
    }

    @Test fun emptyCatalogCannotOpenPurchaseConfirmation() {
        assertFalse(PremiumUiState(isTestMode = false).canContinue)
    }

    @Test fun loadedSelectedProductCanContinue() {
        assertTrue(PremiumUiState(products = listOf(product), selectedProductIndex = 0, isTestMode = false).canContinue)
    }

    @Test fun invalidSelectionCannotContinue() {
        assertFalse(PremiumUiState(products = listOf(product), selectedProductIndex = 4, isTestMode = false).canContinue)
    }

    @Test fun loadingCannotUseStaleProducts() {
        assertFalse(PremiumUiState(isLoading = true, products = listOf(product), selectedProductIndex = 0, isTestMode = false).canContinue)
    }

    @Test fun verifiedPremiumDoesNotRequireStoreProducts() {
        assertTrue(PremiumUiState(hasPremiumAccess = true, isLoading = true, isTestMode = false).canContinue)
    }

    @Test fun pendingPurchasePreventsAnotherClick() {
        assertFalse(PremiumUiState(hasPremiumAccess = true, isPurchasing = true, isTestMode = false).canContinue)
    }
}
