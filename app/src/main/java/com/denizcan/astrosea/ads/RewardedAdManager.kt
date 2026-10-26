package com.denizcan.astrosea.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Ödüllü reklam yükleme ve gösterme yöneticisi.
 * Debug'da Google test reklamları kullanılır; ödül yalnızca SDK tarafından verilir.
 */
class RewardedAdManager(private val context: Context) {

    companion object {
        private const val TAG = "RewardedAdManager"
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var isShowing = false
    private var loadedAt = 0L
    private var loadedConsentRevision = -1
    private val loadCallbacks = mutableListOf<(RewardedAd?) -> Unit>()

    fun preload() {
        load()
    }

    private fun load(onLoaded: ((RewardedAd?) -> Unit)? = null) {
        if (!AdConsentManager.canRequestAds) {
            rewardedAd = null
            onLoaded?.invoke(null)
            return
        }
        if (loadedConsentRevision != AdConsentManager.consentRevision ||
            SystemClock.elapsedRealtime() - loadedAt >= 55 * 60 * 1000L) {
            rewardedAd = null
        }
        rewardedAd?.let {
            onLoaded?.invoke(it)
            return
        }
        onLoaded?.let(loadCallbacks::add)
        if (isLoading) return
        isLoading = true
        val requestConsentRevision = AdConsentManager.consentRevision

        RewardedAd.load(
            context.applicationContext,
            AdConfig.REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ödüllü reklam yüklendi")
                    val allowed = AdConsentManager.canRequestAds &&
                        requestConsentRevision == AdConsentManager.consentRevision
                    rewardedAd = ad.takeIf { allowed }
                    loadedAt = SystemClock.elapsedRealtime()
                    loadedConsentRevision = requestConsentRevision
                    isLoading = false
                    completeLoad(rewardedAd)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Ödüllü reklam yüklenemedi: ${error.message}")
                    rewardedAd = null
                    isLoading = false
                    completeLoad(null)
                }
            }
        )
    }

    private fun completeLoad(ad: RewardedAd?) {
        val pending = loadCallbacks.toList()
        loadCallbacks.clear()
        pending.forEach { it(ad) }
    }

    /**
     * Reklamı gösterir. Kullanıcı ödülü hak ederse [onRewarded],
     * reklam gösterilemezse [onFailed] çağrılır.
     */
    fun show(activity: Activity, onRewarded: () -> Unit, onFailed: () -> Unit) {
        if (isShowing || activity.isFinishing || activity.isDestroyed) {
            onFailed()
            return
        }
        isShowing = true
        fun loadAndShow(allowed: Boolean) {
            if (!allowed) {
                isShowing = false
                onFailed()
            } else {
                load { ad ->
                    if (ad == null || activity.isFinishing || activity.isDestroyed ||
                        !AdConsentManager.canRequestAds) {
                        isShowing = false
                        onFailed()
                    } else {
                        showLoadedAd(activity, ad, onRewarded, onFailed)
                    }
                }
            }
        }
        if (AdConsentManager.canRequestAds) loadAndShow(true)
        else AdConsentManager.gatherConsent(activity, ::loadAndShow)
    }

    private fun showLoadedAd(
        activity: Activity,
        ad: RewardedAd,
        onRewarded: () -> Unit,
        onFailed: () -> Unit
    ) {
        rewardedAd = null // Tek kullanımlık reklamı tekrar göstermeyelim.
        var rewardEarned = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Reklam kapatıldı, ödül kazanıldı: $rewardEarned")
                isShowing = false
                preload() // sıradaki reklamı hazırla
                if (rewardEarned) onRewarded() else onFailed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Reklam gösterilemedi: ${error.message}")
                isShowing = false
                preload()
                onFailed()
            }
        }

        ad.show(activity) { _ ->
            rewardEarned = true
        }
    }
}
