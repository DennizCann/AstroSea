package com.denizcan.astrosea.ads

import com.denizcan.astrosea.BuildConfig

object AdConfig {
    // Reklam testi, abonelik testinden bağımsızdır. Debug gerçek test reklamı gösterir;
    // reklam izlenmeden ödül veren bir simülasyon kullanılmaz.
    val TEST_MODE: Boolean = BuildConfig.ADS_TEST_MODE

    internal const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    internal const val LIVE_REWARDED_AD_UNIT_ID = "ca-app-pub-8919976378865435/5824057683"
    val REWARDED_AD_UNIT_ID: String = rewardedAdUnitId(TEST_MODE)

    internal fun rewardedAdUnitId(testMode: Boolean): String =
        if (testMode) TEST_REWARDED_AD_UNIT_ID else LIVE_REWARDED_AD_UNIT_ID

    /** Ücretsiz kullanıcının günde reklam izleyerek açabileceği maksimum yorum sayısı */
    const val DAILY_AD_UNLOCK_LIMIT = 3

    /** Premium kullanıcı için yorumun hazırlanma süresi (ekranda gösterilmez) */
    const val PREMIUM_INTERPRETATION_DELAY_MILLIS: Long = 5_000L

    /** Ücretsiz / reklam izleyen kullanıcı için yorumun hazırlanma süresi (ekranda gösterilmez) */
    const val AD_INTERPRETATION_DELAY_MILLIS: Long = 8_000L
}
