package com.denizcan.astrosea.ads

import com.denizcan.astrosea.BuildConfig

object AdConfig {
    /**
     * Test modunda reklamlar gerçekten yüklenmez, izlenmiş gibi simüle edilir
     * ve bekleme süreleri kısaltılır (emülatörde test edilebilsin diye).
     */
    val TEST_MODE: Boolean = BuildConfig.BILLING_TEST_MODE

    // TODO: Yayın öncesi gerçek AdMob ödüllü reklam birimi ID'si ile değiştirilecek
    // Şu anki değer Google'ın herkese açık test reklam birimi
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    /** Ücretsiz kullanıcının günde reklam izleyerek açabileceği maksimum yorum sayısı */
    const val DAILY_AD_UNLOCK_LIMIT = 3

    /** Premium kullanıcı için yorumun hazırlanma süresi (reklamsız) */
    val PREMIUM_INTERPRETATION_DELAY_MILLIS: Long =
        if (TEST_MODE) 60_000L else 10 * 60_000L // test: 1 dk, gerçek: 10 dk

    /** Reklam izleyen kullanıcı için yorumun hazırlanma süresi */
    val AD_INTERPRETATION_DELAY_MILLIS: Long =
        if (TEST_MODE) 30_000L else 5 * 60_000L // test: 30 sn, gerçek: 5 dk

    /** Bekleme sırasında reklam izlemenin süreden düşürdüğü miktar (10 dk -> 5 dk) */
    val SPEED_UP_MILLIS: Long =
        if (TEST_MODE) 30_000L else 5 * 60_000L
}
