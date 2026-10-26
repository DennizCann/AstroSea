package com.denizcan.astrosea.ads

import com.denizcan.astrosea.BuildConfig
import org.junit.Assert.*
import org.junit.Test

class AdConfigTest {
    @Test fun debugAdsUseGoogleDemoUnit() {
        assertTrue(BuildConfig.ADS_TEST_MODE)
        assertEquals("ca-app-pub-3940256099942544/5224354917", AdConfig.REWARDED_AD_UNIT_ID)
    }

    @Test fun releaseSelectionUsesAstroSeaUnit() {
        assertEquals("ca-app-pub-8919976378865435/5824057683", AdConfig.rewardedAdUnitId(false))
        assertNotEquals(AdConfig.rewardedAdUnitId(true), AdConfig.rewardedAdUnitId(false))
    }

    @Test fun adTestingDoesNotEnableBillingSimulation() {
        assertFalse(BuildConfig.BILLING_TEST_MODE)
        assertTrue(AdConfig.TEST_MODE)
    }
}
