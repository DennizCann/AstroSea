package com.denizcan.astrosea.util

import android.content.Context
import android.util.Log
import com.denizcan.astrosea.BuildConfig
import com.denizcan.astrosea.model.ReadingFormat
import com.denizcan.astrosea.model.TarotCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GroqService(private val context: Context) {

    companion object {
        private const val TAG = "GroqService"
        private val API_KEY = BuildConfig.GROQ_API_KEY
        private const val API_URL = "https://api.groq.com/openai/v1/chat/completions"
        private const val MODEL = "llama-3.3-70b-versatile"

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

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateTarotReading(
        readingType: String,
        drawnCards: List<TarotCard>,
        readingFormat: ReadingFormat
    ): String? = withContext(Dispatchers.IO) {
        if (API_KEY.isBlank()) {
            Log.e(TAG, "GROQ_API_KEY boş — local.properties dosyasını kontrol edin")
            return@withContext null
        }
        try {
            val prompt = buildTarotPrompt(readingType, drawnCards, readingFormat)
            Log.d(TAG, "Groq API'ye istek gönderiliyor...")
            val response = callGroqAPI(prompt, LanguageManager.isTurkish(context))
            if (response.isNotBlank()) {
                Log.d(TAG, "Groq'dan başarılı yanıt alındı (${response.length} karakter)")
                response
            } else {
                Log.w(TAG, "Groq'dan boş yanıt alındı")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Groq API çağrısında hata", e)
            null
        }
    }

    private fun callGroqAPI(prompt: String, isTurkish: Boolean): String {
        try {
            val systemMessage = if (isTurkish) {
                """
                Sen profesyonel bir Türk tarot yorumcususun.
                MUTLAKA ve SADECE Türkçe yanıt ver.
                Hiçbir koşulda İngilizce veya başka bir dilde kelime kullanma.
                Tüm kart isimlerini Türkçe karşılıklarıyla yaz.
                Akıcı, anlaşılır ve etkileyici bir Türkçe kullan.
                """.trimIndent()
            } else {
                """
                You are a professional English tarot reader.
                Respond ONLY in natural, fluent English.
                Do not use Turkish words, Turkish headings, or translated stock phrases.
                Use standard English tarot card names (e.g. The Fool, The Tower).
                Write as if composing an original reading for this specific spread—not a generic template.
                """.trimIndent()
            }

            val jsonBody = JSONObject().apply {
                put("model", MODEL)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemMessage)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", if (isTurkish) 0.65 else 0.72)
                put("max_tokens", 2048)
                put("top_p", 0.9)
                put("stream", false)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Groq API hatası: ${response.code} - ${response.message} — $responseBody")
                    return ""
                }

                val jsonResponse = JSONObject(responseBody)

                return jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Groq API çağrısında detaylı hata", e)
            return ""
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

    fun isAvailable(): Boolean = API_KEY.isNotBlank()
}
