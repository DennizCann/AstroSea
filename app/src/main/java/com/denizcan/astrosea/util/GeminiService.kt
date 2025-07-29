package com.denizcan.astrosea.util

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.denizcan.astrosea.model.TarotCard
import com.denizcan.astrosea.model.ReadingFormat

class GeminiService {
    private var generativeModel: GenerativeModel? = null
    
    companion object {
        private const val TAG = "GeminiService"
        // API key'i güvenli bir şekilde saklamak için BuildConfig kullanılabilir
        private const val API_KEY = "AIzaSyCrlcEsUsAEOGs6VJ3RzcJ3PVZhaa1V634" // TODO: API key'i ekleyin
    }
    
    init {
        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-2.5-flash",
                apiKey = API_KEY
            )
            Log.d(TAG, "Gemini model başarıyla başlatıldı")
        } catch (e: Exception) {
            Log.e(TAG, "Gemini model başlatılamadı", e)
        }
    }
    
    suspend fun generateTarotReading(
        readingType: String,
        drawnCards: List<TarotCard>,
        readingFormat: ReadingFormat
    ): String = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                Log.e(TAG, "Gemini model null, varsayılan yorum döndürülüyor")
                return@withContext generateDefaultReading(readingType, drawnCards, readingFormat)
            }
            
            val prompt = buildTarotPrompt(readingType, drawnCards, readingFormat)
            
            val response = generativeModel!!.generateContent(prompt)
            val generatedText = response.text
            
            if (generatedText.isNullOrEmpty()) {
                Log.w(TAG, "Gemini'den boş yanıt alındı, varsayılan yorum döndürülüyor")
                return@withContext generateDefaultReading(readingType, drawnCards, readingFormat)
            }
            
            Log.d(TAG, "Gemini'den başarılı yanıt alındı")
            return@withContext generatedText
            
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API çağrısında hata", e)
            return@withContext generateDefaultReading(readingType, drawnCards, readingFormat)
        }
    }
    
    private fun buildTarotPrompt(
        readingType: String,
        drawnCards: List<TarotCard>,
        readingFormat: ReadingFormat
    ): String {
        // Kartları pozisyonlarla eşleştir
        val cardDetails = drawnCards.mapIndexed { index, card ->
            val position = if (index < readingFormat.positions.size) readingFormat.positions[index] else null
            val positionName = position?.name ?: "Kart ${index + 1}"
            val cardDisplayName = card.turkishName ?: card.name
            
            """
            ${positionName}: ${cardDisplayName} (${card.name})
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
        Hayatınızda yeni fırsatlar ve gelişmeler beklenmektedir. Pozitif düşüncelerle ilerleyin ve sezgilerinize güvenin.
        """.trimIndent()
    }
    
    fun isAvailable(): Boolean {
        return generativeModel != null && API_KEY != "YOUR_GEMINI_API_KEY"
    }
} 