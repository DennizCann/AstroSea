package com.denizcan.astrosea.util

import android.content.Context
import android.util.Log
import com.denizcan.astrosea.model.ReadingFormat
import com.denizcan.astrosea.model.TarotCard
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GroqService(private val context: Context) {
    private val functions = FirebaseFunctions.getInstance("europe-west1")

    suspend fun generateTarotReading(
        readingType: String,
        drawnCards: List<TarotCard>,
        readingFormat: ReadingFormat
    ): String? {
        return try {
            val request = tarotReadingRequest(
                readingFormat.id,
                if (LanguageManager.isTurkish(context)) "tr" else "en",
                drawnCards.map { it.id },
                readingFormat.cardCount
            )
            val result = functions.getHttpsCallable("generateTarotReading").call(request).await()
            ((result.getData() as? Map<*, *>)?.get("reading") as? String)?.takeIf { it.isNotBlank() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("GroqService", "Tarot yorum isteği başarısız", e)
            null
        }
    }

    fun isAvailable(): Boolean = true
}
