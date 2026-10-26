package com.denizcan.astrosea.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/** Tek izin kaynağı UMP'dir; KVKK kabulü reklam izni yerine kullanılmaz. */
object AdConsentManager {
    private const val TAG = "AdConsentManager"
    private val mainHandler = Handler(Looper.getMainLooper())
    private var consentInformation: ConsentInformation? = null
    private var busy = false
    private var sdkInitialized = false
    private val callbacks = mutableListOf<(Boolean) -> Unit>()

    var privacyOptionsRequired by mutableStateOf(false)
        private set
    var consentRevision = 0
        private set

    val canRequestAds: Boolean
        get() = !busy && sdkInitialized && consentInformation?.canRequestAds() == true

    /** Her yeni Activity açılışında güncellenir; gerekli değilse form gösterilmez. */
    fun gatherConsent(activity: Activity, onComplete: (Boolean) -> Unit = {}) {
        if (activity.isFinishing || activity.isDestroyed) {
            onComplete(false)
            return
        }
        callbacks.add(onComplete)
        if (busy) return
        busy = true
        val information = UserMessagingPlatform.getConsentInformation(activity.applicationContext)
        consentInformation = information
        information.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                if (activity.isFinishing || activity.isDestroyed) {
                    finish(false)
                } else {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                        if (error != null) Log.w(TAG, "İzin formu: ${error.message}")
                        initializeIfAllowed(activity.applicationContext)
                    }
                }
            },
            { error ->
                Log.w(TAG, "İzin bilgisi güncellenemedi: ${error.message}")
                // UMP, önceki geçerli tercihlerle reklam istenip istenemeyeceğine karar verir.
                initializeIfAllowed(activity.applicationContext)
            }
        )
    }

    fun showPrivacyOptions(activity: Activity, onComplete: (Boolean) -> Unit) {
        if (busy || activity.isFinishing || activity.isDestroyed) {
            onComplete(false)
            return
        }
        busy = true
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            consentRevision++ // Eski tercihlerle yüklenmiş reklamlar yeniden kullanılmasın.
            initializeIfAllowed(activity.applicationContext)
            onComplete(error == null)
        }
    }

    private fun initializeIfAllowed(context: Context) {
        updatePrivacyOptions()
        if (consentInformation?.canRequestAds() != true) {
            finish(false)
        } else if (sdkInitialized) {
            finish(true)
        } else {
            try {
                MobileAds.initialize(context) {
                    mainHandler.post {
                        sdkInitialized = true
                        finish(true)
                    }
                }
            } catch (error: Exception) {
                Log.w(TAG, "Reklam SDK'sı başlatılamadı", error)
                finish(false)
            }
        }
    }

    private fun updatePrivacyOptions() {
        privacyOptionsRequired = consentInformation?.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    private fun finish(allowed: Boolean) {
        busy = false
        updatePrivacyOptions()
        val pending = callbacks.toList()
        callbacks.clear()
        pending.forEach { it(allowed && canRequestAds) }
    }
}
