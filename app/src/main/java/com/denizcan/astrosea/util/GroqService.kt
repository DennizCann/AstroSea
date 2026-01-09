package com.denizcan.astrosea.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.denizcan.astrosea.model.TarotCard
import com.denizcan.astrosea.model.ReadingFormat
import com.denizcan.astrosea.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GroqService {
    
    companion object {
        private const val TAG = "GroqService"
        // API Key local.properties'ten BuildConfig üzerinden okunur
        private val API_KEY = BuildConfig.GROQ_API_KEY
        private const val API_URL = "https://api.groq.com/openai/v1/chat/completions"
        private const val MODEL = "llama-3.3-70b-versatile" // Groq'un en iyi modeli
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
    ): String = withContext(Dispatchers.IO) {
        try {
            val prompt = buildTarotPrompt(readingType, drawnCards, readingFormat)
            
            Log.d(TAG, "Groq API'ye istek gönderiliyor...")
            
            val response = callGroqAPI(prompt)
            
            if (response.isNotEmpty()) {
                Log.d(TAG, "Groq'dan başarılı yanıt alındı")
                return@withContext response
            } else {
                Log.w(TAG, "Groq'dan boş yanıt alındı, varsayılan yorum döndürülüyor")
                return@withContext generateDefaultReading(readingType, drawnCards, readingFormat)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Groq API çağrısında hata", e)
            return@withContext generateDefaultReading(readingType, drawnCards, readingFormat)
        }
    }
    
    private fun callGroqAPI(prompt: String): String {
        try {
            // System message - Türkçe dil tutarlılığı için kritik
            val systemMessage = """
                Sen profesyonel bir Türk tarot yorumcususun. 
                MUTLAKA ve SADECE Türkçe yanıt ver. 
                Hiçbir koşulda İngilizce veya başka bir dilde kelime kullanma.
                Tüm kart isimlerini Türkçe karşılıklarıyla yaz.
                Akıcı, anlaşılır ve etkileyici bir Türkçe kullan.
            """.trimIndent()
            
            val jsonBody = JSONObject().apply {
                put("model", MODEL)
                put("messages", JSONArray().apply {
                    // System message eklendi
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemMessage)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.65) // Daha tutarlı yanıtlar için düşürüldü
                put("max_tokens", 2048)
                put("top_p", 0.9) // Biraz düşürüldü
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
                if (!response.isSuccessful) {
                    Log.e(TAG, "Groq API hatası: ${response.code} - ${response.message}")
                    return ""
                }
                
                val responseBody = response.body?.string() ?: ""
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
        // Kartları pozisyonlarla eşleştir - sadece Türkçe isimler
        val cardDetails = drawnCards.mapIndexed { index, card ->
            val position = if (index < readingFormat.positions.size) readingFormat.positions[index] else null
            val positionName = position?.name ?: "Kart ${index + 1}"
            val cardDisplayName = card.turkishName ?: card.name
            
            """
            ${positionName}: ${cardDisplayName}
            Anlam: ${card.meaningUpright}
            Anahtar Kelimeler: ${card.keywords.joinToString(", ")}
            """.trimIndent()
        }.joinToString("\n\n")
        
        // Base prompt'u kullan ve kart detaylarını ekle
        var prompt = readingFormat.basePrompt
        
        // [KART_ADI] placeholder'larını gerçek kart adlarıyla değiştir
        drawnCards.forEachIndexed { index, card ->
            val placeholder = "[KART_${index + 1}_ADI]"
            val fallbackPlaceholder = "[KART_ADI]"
            val cardDisplayName = card.turkishName ?: card.name
            
            prompt = prompt.replace(placeholder, cardDisplayName)
            if (index == 0) {
                prompt = prompt.replace(fallbackPlaceholder, cardDisplayName)
            }
        }
        
        // Kart detaylarını ve dil talimatını ekle
        prompt += "\n\n--- Kart Detayları ---\n$cardDetails"
        prompt += "\n\n[ÖNEMLİ: Yanıtını SADECE Türkçe yaz. Hiçbir İngilizce kelime kullanma.]"
        
        return prompt
    }
    
    private fun generateDefaultReading(
        readingType: String,
        drawnCards: List<TarotCard>,
        readingFormat: ReadingFormat
    ): String {
        val cardDetails = drawnCards.mapIndexed { index, card ->
            val position = if (index < readingFormat.positions.size) readingFormat.positions[index] else null
            val positionName = position?.name ?: "Kart ${index + 1}"
            val cardDisplayName = card.turkishName ?: card.name
            "$positionName: $cardDisplayName\n${card.meaningUpright}"
        }.joinToString("\n\n")
        
        return """
        **${readingFormat.name} Yorumu**
        
        $cardDetails
        
        **Genel Yorum:**
        Bu açılım size hayatınızın bu alanında rehberlik etmek için tasarlanmıştır. Çektiğiniz kartların anlamlarını dikkatlice değerlendirin ve iç sesinizi dinleyin. Her kart size özel bir mesaj taşımaktadır.
        
        **Özet:**
        Hayatınızda yeni fırsatlar ve potansiyeller bulunmaktadır. Pozitif düşüncelerle ilerleyin ve sezgilerinize güvenin.
        """.trimIndent()
    }
    
    fun isAvailable(): Boolean {
        return API_KEY.isNotEmpty()
    }
}
