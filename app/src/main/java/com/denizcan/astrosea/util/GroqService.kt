package com.denizcan.astrosea.util

import android.content.Context
import android.util.Log
import com.denizcan.astrosea.model.ReadingFormat
import com.denizcan.astrosea.model.TarotCard
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class GroqService(private val context: Context) {

    companion object {
        private const val TAG = "GroqService"
        private val ENGLISH_BASE_PROMPT = """
            Role:
            You are a tarot guide who speaks the language of symbols and archetypes. You hold up a mirror to the querent, reflecting the energy and potential of the cards honestly, directly, and with quiet confidence. Your purpose is not to predict the future, but to read the present, create awareness, and help the querent reclaim their own power.

            Tone and Style:
            Write in clear, respectful, second-person English ("you"). Be evocative and literary without sounding generic, templated, or overly soft. Vary your sentence structure and verbs so the reading feels alive rather than repetitive. Avoid filler phrases such as "this may suggest", "it could mean", or "perhaps". State the card's message as grounded insight.

            Do not shy away from challenging cards such as The Tower or the Ten of Swords; name their shadow aspects and transformative power with courage. Keep each card reading to about 4-5 sentences. Weave the cards into a cohesive narrative using natural transitions.

            Task:
            Interpret the tarot spread described below.

            Instructions:
            - For each card, write a separate interpretation that honors its position in the spread.
            - Use the heading format: ### [Position]: [Card Name]
            - After all card readings, add ### Overall Reading with a synthesis paragraph and practical guidance for today.
            - Respond ONLY in English. Do not use any Turkish words.
        """.trimIndent()
    }

    private val functions = FirebaseFunctions.getInstance("europe-west1")

    suspend fun generateTarotReading(
        readingType: String,
        drawnCards: List<TarotCard>,
        readingFormat: ReadingFormat
    ): String? {
        try {
            val prompt = buildTarotPrompt(readingType, drawnCards, readingFormat)
            Log.d(TAG, "Sunucu üzerinden tarot yorumu isteniyor...")
            val result = functions
                .getHttpsCallable("generateTarotReading")
                .call(mapOf("prompt" to prompt, "isTurkish" to LanguageManager.isTurkish(context)))
                .await()
            @Suppress("UNCHECKED_CAST")
            val response = (result.data as? Map<String, Any?>)?.get("reading") as? String ?: ""
            if (response.isNotBlank()) {
                Log.d(TAG, "Yorum başarıyla alındı (${response.length} karakter)")
                response
            } else {
                Log.w(TAG, "Sunucu boş yorum döndürdü")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Tarot yorum isteği başarısız", e)
            null
        }
    }

    private fun buildTarotPrompt(
        readingType: String,
        drawnCards: List<TarotCard>,
        readingFormat: ReadingFormat
    ): String {
        return if (LanguageManager.isTurkish(context)) {
            buildTurkishPrompt(readingType, drawnCards, readingFormat)
        } else {
            buildEnglishPrompt(readingType, drawnCards)
        }
    }

    private fun buildTurkishPrompt(
        readingType: String,
        drawnCards: List<TarotCard>,
        readingFormat: ReadingFormat
    ): String {
        val cardDetails = drawnCards.mapIndexed { index, card ->
            val position = if (index < readingFormat.positions.size) readingFormat.positions[index] else null
            val positionName = position?.name ?: "Kart ${index + 1}"
            val cardDisplayName = card.turkishName ?: card.name

            """
            ${positionName}: ${cardDisplayName}
            Anlam: ${card.displayMeaningUpright()}
            Anahtar Kelimeler: ${card.keywords.joinToString(", ")}
            """.trimIndent()
        }.joinToString("\n\n")

        var prompt = readingFormat.basePrompt

        drawnCards.forEachIndexed { index, card ->
            val placeholder = "[KART_${index + 1}_ADI]"
            val fallbackPlaceholder = "[KART_ADI]"
            val cardDisplayName = card.turkishName ?: card.name

            prompt = prompt.replace(placeholder, cardDisplayName)
            if (index == 0) {
                prompt = prompt.replace(fallbackPlaceholder, cardDisplayName)
            }
        }

        prompt += "\n\n--- Kart Detayları ---\n$cardDetails"
        prompt += "\n\n[ÖNEMLİ: Yanıtını SADECE Türkçe yaz. Hiçbir İngilizce kelime kullanma.]"
        return prompt
    }

    private fun buildEnglishPrompt(
        readingType: String,
        drawnCards: List<TarotCard>
    ): String {
        val readingName = ReadingTexts.displayName(context, readingType)
        val slotNames = ReadingTexts.slotNames(context, readingType)

        val positionsBlock = drawnCards.mapIndexed { index, card ->
            val slot = slotNames.getOrNull(index) ?: "Card ${index + 1}"
            "$slot: ${card.name}"
        }.joinToString("\n")

        val cardDetails = drawnCards.mapIndexed { index, card ->
            val slot = slotNames.getOrNull(index) ?: "Card ${index + 1}"
            """
            $slot: ${card.name}
            Upright meaning: ${card.displayMeaningUpright()}
            Reversed meaning: ${card.displayMeaningReversed()}
            """.trimIndent()
        }.joinToString("\n\n")

        return """
            $ENGLISH_BASE_PROMPT

            Spread Name: $readingName

            Cards and Positions:
            $positionsBlock

            --- Card Reference ---
            $cardDetails

            [IMPORTANT: Write the entire reading ONLY in English. Translate all headings into English. Do not copy Turkish phrasing.]
        """.trimIndent()
    }

    fun isAvailable(): Boolean = true
}
