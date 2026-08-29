package com.denizcan.astrosea.model

import android.content.Context
import com.denizcan.astrosea.R
import com.denizcan.astrosea.util.LanguageManager

data class TarotCard(
    val id: String,
    val name: String,
    val turkishName: String? = null,
    val number: String? = null,
    val type: String,
    val suit: String? = null,
    val imageResName: String,
    val meaningUpright: String,
    val meaningReversed: String,
    val englishMeaningUpright: String? = null,
    val englishMeaningReversed: String? = null,
    val englishDescription: String? = null,
    val description: String,
    val keywords: List<String>,
    val zodiacSigns: String?,
    val predictions: List<String>?
) {
    fun displayName(): String =
        if (LanguageManager.isAppTurkish()) turkishName ?: name else name

    /** Fallback yorumlarda kullanılır; İngilizce modda Türkçe anlam metni gösterilmez. */
    fun displayMeaning(context: Context): String =
        if (LanguageManager.isTurkish(context)) meaningUpright
        else displayMeaningUpright()

    fun displayMeaningUpright(): String =
        if (LanguageManager.isAppTurkish()) meaningUpright
        else englishMeaningUpright ?: meaningUpright

    fun displayMeaningReversed(): String =
        if (LanguageManager.isAppTurkish()) meaningReversed
        else englishMeaningReversed ?: meaningReversed

    fun displayDescription(): String =
        if (LanguageManager.isAppTurkish()) description
        else englishDescription ?: "${displayName()} represents ${displayMeaningUpright().replaceFirstChar { it.lowercase() }}. " +
            "When reversed, it can point to ${displayMeaningReversed().replaceFirstChar { it.lowercase() }}."

    fun displayPredictions(): List<String> {
        if (LanguageManager.isAppTurkish()) return predictions.orEmpty()

        val themes = displayMeaningUpright()
            .split(",")
            .map { it.trim().replaceFirstChar { character -> character.lowercase() } }
            .filter { it.isNotBlank() }

        return listOfNotNull(
            themes.getOrNull(0)?.let { "An opportunity for $it." },
            themes.getOrNull(1)?.let { "A period that invites $it." },
            themes.getOrNull(2)?.let { "A focus on $it." },
            themes.getOrNull(3)?.let { "A chance to develop $it." }
        )
    }

    fun displayZodiacSigns(): String? {
        if (LanguageManager.isAppTurkish()) return zodiacSigns
        return zodiacSigns
            ?.replace("Hava burçları", "Air signs")
            ?.replace("Ateş elementinin burçları", "Fire signs")
            ?.replace("Ateş burçları", "Fire signs")
            ?.replace("Toprak elementinin burçları", "Earth signs")
            ?.replace("Toprak burçları", "Earth signs")
            ?.replace("Özellikle", "especially")
            ?.replace("İkizler", "Gemini")
            ?.replace("Başak", "Virgo")
            ?.replace("Yengeç", "Cancer")
            ?.replace("Balık", "Pisces")
            ?.replace("Boğa", "Taurus")
            ?.replace("Terazi", "Libra")
            ?.replace("Koç", "Aries")
            ?.replace("Oğlak", "Capricorn")
            ?.replace("Aslan", "Leo")
            ?.replace("Akrep", "Scorpio")
            ?.replace("Yay", "Sagittarius")
            ?.replace("Kova", "Aquarius")
            ?.replace("Merkür", "Mercury")
            ?.replace("Venüs", "Venus")
            ?.replace("Güneş", "Sun")
    }
} 
