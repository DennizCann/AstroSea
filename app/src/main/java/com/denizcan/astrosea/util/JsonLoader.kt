package com.denizcan.astrosea.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.denizcan.astrosea.model.TarotCard
import com.denizcan.astrosea.model.ReadingFormats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class JsonLoader(private val context: Context) {
    companion object {
        private val lock = Mutex()
        private var cachedCards: List<TarotCard>? = null
        private var cachedFormats: ReadingFormats? = null
    }

    suspend fun loadTarotCards(): List<TarotCard> = withContext(Dispatchers.IO) {
        lock.withLock {
            cachedCards ?: readTarotCards().also { if (it.isNotEmpty()) cachedCards = it }
        }
    }

    private fun readTarotCards(): List<TarotCard> {
        return try {
            val jsonString = context.assets.open("tarot_cards.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<TarotCardsResponse>() {}.type
            val response = Gson().fromJson<TarotCardsResponse>(jsonString, type)
            val translationType = object : TypeToken<Map<String, EnglishTarotTranslation>>() {}.type
            val englishTranslations: Map<String, EnglishTarotTranslation> = context.assets
                .open("tarot_card_translations_en.json")
                .bufferedReader()
                .use { Gson().fromJson(it, translationType) }
            val descriptionType = object : TypeToken<Map<String, String>>() {}.type
            val englishDescriptions: Map<String, String> = context.assets
                .open("tarot_card_descriptions_en.json")
                .bufferedReader()
                .use { Gson().fromJson(it, descriptionType) }
            
            // Tüm kartları birleştir
            val allCards = mutableListOf<TarotCard>()
            allCards.addAll(response.cards)
            allCards.addAll(response.minor_arcana.cups)
            allCards.addAll(response.minor_arcana.swords)
            allCards.addAll(response.minor_arcana.wands)
            allCards.addAll(response.minor_arcana.pentacles)
            
            allCards.map { card ->
                englishTranslations[card.id]?.let { translation ->
                    card.copy(
                        englishMeaningUpright = translation.upright,
                        englishMeaningReversed = translation.reversed,
                        englishDescription = englishDescriptions[card.id]
                    )
                } ?: card.copy(englishDescription = englishDescriptions[card.id])
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun loadReadingFormats(): ReadingFormats? = withContext(Dispatchers.IO) {
        lock.withLock { cachedFormats ?: readReadingFormats().also { cachedFormats = it } }
    }

    private fun readReadingFormats(): ReadingFormats? {
        return try {
            val jsonString = context.assets.open("reading_formats.json").bufferedReader().use { it.readText() }
            Gson().fromJson(jsonString, ReadingFormats::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

data class TarotCardsResponse(
    val cards: List<TarotCard>,
    val minor_arcana: MinorArcana
)

data class MinorArcana(
    val cups: List<TarotCard>,
    val swords: List<TarotCard>,
    val wands: List<TarotCard>,
    val pentacles: List<TarotCard>
)

data class EnglishTarotTranslation(
    val upright: String,
    val reversed: String
)
