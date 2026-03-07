package com.denizcan.astrosea

import android.app.Application
import com.adapty.Adapty
import com.adapty.models.AdaptyConfig

class AstroSeaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Adapty SDK'yı başlat
        Adapty.activate(
            this,
            AdaptyConfig.Builder("public_live_IKRYXMEP.oj4hibl7kTkeXRZqepAo")
                .build()
        )
    }
}
