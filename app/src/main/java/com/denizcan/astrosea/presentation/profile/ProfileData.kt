package com.denizcan.astrosea.presentation.profile

data class ProfileData(
    val name: String = "",
    val surname: String = "",
    val birthDate: String = "",
    val birthTime: String = "",
    val country: String = "",
    val city: String = "",
    val card_0_id: String? = null,
    val card_0_revealed: Boolean? = null,
    val card_1_id: String? = null,
    val card_1_revealed: Boolean? = null,
    val card_2_id: String? = null,
    val card_2_revealed: Boolean? = null,
    val last_draw_date: String? = null
) {
    // Firestore için boş constructor gerekli
    constructor() : this("", "", "", "", "", "", null, null, null, null, null, null, null)
} 