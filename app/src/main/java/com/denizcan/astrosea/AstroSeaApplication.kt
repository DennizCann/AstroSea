package com.denizcan.astrosea

import android.app.Application
import android.util.Log
import com.adapty.Adapty
import com.adapty.models.AdaptyConfig
import com.denizcan.astrosea.ads.AdConfig
import com.denizcan.astrosea.billing.BillingConfig
import com.google.android.gms.ads.MobileAds

class AstroSeaApplication : Application() {

    companion object {
        private const val TAG = "AstroSeaApplication"
    }

    override fun onCreate() {
        super.onCreate()

        // AdMob'u başlat (test modunda reklamlar simüle edildiği için gerek yok)
        if (!AdConfig.TEST_MODE) {
            try {
                MobileAds.initialize(this) {
                    Log.d(TAG, "AdMob başlatıldı")
                }
            } catch (e: Exception) {
                Log.e(TAG, "AdMob başlatılamadı", e)
            }
        }

        if (BillingConfig.TEST_MODE) {
            Log.d(TAG, "Debug test modu — Adapty başlatılmadı")
            return
        }

        Adapty.activate(
            this,
            AdaptyConfig.Builder("public_live_IKRYXMEP.oj4hibl7kTkeXRZqepAo")
                .build()
        )
    }
}
