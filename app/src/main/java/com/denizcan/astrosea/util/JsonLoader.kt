package com.denizcan.astrosea.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.denizcan.astrosea.model.TarotCard

class JsonLoader(private val context: Context) {
    fun loadTarotCards(): List<TarotCard> {
        return try {
            val jsonString = context.assets.open("tarot_cards.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<TarotCardsResponse>() {}.type
            val response = Gson().fromJson<TarotCardsResponse>(jsonString, type)
            
            // Tüm kartları birleştir
            val allCards = mutableListOf<TarotCard>()
            allCards.addAll(response.cards)
            allCards.addAll(response.minor_arcana.cups)
            allCards.addAll(response.minor_arcana.swords)
            allCards.addAll(response.minor_arcana.wands)
            allCards.addAll(response.minor_arcana.pentacles)
            
            allCards
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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