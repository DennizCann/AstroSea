package com.denizcan.astrosea.util

import android.content.Context
import org.json.JSONObject
import com.denizcan.astrosea.model.TarotCard

class JsonParser(private val context: Context) {
    fun loadTarotCards(): List<TarotCard> {
        val jsonString = context.assets.open("tarot_cards.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val cards = mutableListOf<TarotCard>()

        // Major Arkana kartlarını yükle
        val majorArcana = jsonObject.getJSONArray("major_arcana")
        for (i in 0 until majorArcana.length()) {
            val card = majorArcana.getJSONObject(i)
            cards.add(parseTarotCard(card))
        }

        // Minor Arkana kartlarını yükle
        val minorArcana = jsonObject.getJSONObject("minor_arcana")
        val suits = listOf("wands", "cups", "swords", "pentacles")
        suits.forEach { suit ->
            val suitCards = minorArcana.getJSONArray(suit)
            for (i in 0 until suitCards.length()) {
                val card = suitCards.getJSONObject(i)
                cards.add(parseTarotCard(card))
            }
        }

        return cards
    }

    private fun parseTarotCard(json: JSONObject): TarotCard {
        return TarotCard(
            id = json.getString("id"),
            name = json.getString("name"),
            number = json.optString("number"),
            type = json.getString("type"),
            suit = json.optString("suit"),
            imageResName = json.getString("imageResName"),
            meaningUpright = json.getString("meaningUpright"),
            meaningReversed = json.getString("meaningReversed"),
            description = json.getString("description"),
            keywords = json.getJSONArray("keywords").let { array ->
                List(array.length()) { array.getString(it) }
            },
            zodiacSigns = json.optString("zodiacSigns", null),
            predictions = if (json.has("predictions")) {
                json.getJSONArray("predictions").let { array ->
                    List(array.length()) { array.getString(it) }
                }
            } else null
        )
    }
} 