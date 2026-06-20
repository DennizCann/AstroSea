package com.denizcan.astrosea

import android.app.Application
import android.util.Log
import com.adapty.Adapty
import com.adapty.models.AdaptyConfig
import com.denizcan.astrosea.billing.BillingConfig

class AstroSeaApplication : Application() {

    companion object {
        private const val TAG = "AstroSeaApplication"
    }

    override fun onCreate() {
        super.onCreate()

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
