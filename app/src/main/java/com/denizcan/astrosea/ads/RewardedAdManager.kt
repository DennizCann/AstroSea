package com.denizcan.astrosea.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Ödüllü reklam yükleme ve gösterme yöneticisi.
 * TEST_MODE'da reklam gösterimi simüle edilir (2 sn bekleyip ödül verir).
 */
class RewardedAdManager(private val context: Context) {

    companion object {
        private const val TAG = "RewardedAdManager"
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun preload() {
        if (AdConfig.TEST_MODE || rewardedAd != null || isLoading) return
        isLoading = true

        RewardedAd.load(
            context,
            AdConfig.REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ödüllü reklam yüklendi")
                    rewardedAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Ödüllü reklam yüklenemedi: ${error.message}")
                    rewardedAd = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Reklamı gösterir. Kullanıcı ödülü hak ederse [onRewarded],
     * reklam gösterilemezse [onFailed] çağrılır.
     */
    fun show(activity: Activity, onRewarded: () -> Unit, onFailed: () -> Unit) {
        if (AdConfig.TEST_MODE) {
            Log.d(TAG, "TEST MODU: Reklam izleme simüle ediliyor (2 sn)")
            Handler(Looper.getMainLooper()).postDelayed({ onRewarded() }, 2000)
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            Log.w(TAG, "Reklam henüz hazır değil, yükleme tetiklendi")
            preload()
            onFailed()
            return
        }

        var rewardEarned = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Reklam kapatıldı, ödül kazanıldı: $rewardEarned")
                rewardedAd = null
                preload() // sıradaki reklamı hazırla
                if (rewardEarned) onRewarded() else onFailed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Reklam gösterilemedi: ${error.message}")
                rewardedAd = null
                preload()
                onFailed()
            }
        }

        ad.show(activity) { _ ->
            rewardEarned = true
        }
    }
}
