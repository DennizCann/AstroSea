package com.denizcan.astrosea.presentation.profile

data class ProfileData(
    val name: String = "",
    val surname: String = "",
    val birthDate: String = "",
    val birthTime: String = "",
    val country: String = "",
    val city: String = "",
    val isPremium: Boolean = false,  // Premium üyelik durumu - varsayılan: false (standart üye)
    val premiumStartDate: String? = null,  // Premium üyelik başlangıç tarihi
    val premiumEndDate: String? = null,  // Premium üyelik bitiş tarihi (null = süresiz)
    val premiumProductId: String? = null,  // Seçilen plan ID'si (astrosea_weekly, astrosea_monthly, astrosea_yearly)
    val card_0_id: String? = null,
    val card_0_revealed: Boolean? = null,
    val card_1_id: String? = null,
    val card_1_revealed: Boolean? = null,
    val card_2_id: String? = null,
    val card_2_revealed: Boolean? = null,
    val last_draw_date: String? = null
) {
    // Firestore için boş constructor gerekli
    constructor() : this("", "", "", "", "", "", false, null, null, null, null, null, null, null, null, null, null)
} 