package com.denizcan.astrosea.presentation.general

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.denizcan.astrosea.model.TarotCard
import com.denizcan.astrosea.util.JsonLoader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class GeneralReadingViewModel(private val context: Context) : ViewModel() {

    var drawnCards by mutableStateOf<List<ReadingCardState>>(emptyList())
        private set

    var isCardsDrawn by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    private val allTarotCards: List<TarotCard> by lazy {
        JsonLoader(context).loadTarotCards()
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("tarot_readings", Context.MODE_PRIVATE)
    }

    private val gson = Gson()

    init {
        Log.d("GeneralReadingViewModel", "ViewModel initialized.")
    }

    private fun getCardCountForReading(readingType: String): Int {
        return getReadingInfo(readingType).cardCount
    }

    fun drawCardForPosition(readingType: String, position: Int) {
        if (drawnCards.any { it.index == position } || isLoading) return

        viewModelScope.launch {
            isLoading = true
            try {
                val usedCardIds = drawnCards.map { it.card.id }
                val availableCards = allTarotCards.filter { it.id !in usedCardIds }

                if (availableCards.isEmpty()) {
                    Log.w("GeneralReadingVM", "No more unique cards to draw.")
                    return@launch
                }

                val randomCard = availableCards.shuffled().first()

                val newCardState = ReadingCardState(
                    index = position,
                    card = randomCard,
                    isRevealed = true
                )

                val updatedList = (drawnCards + newCardState).sortedBy { it.index }
                drawnCards = updatedList

                if (drawnCards.size == getCardCountForReading(readingType)) {
                    isCardsDrawn = true
                }

                // Durumu kaydet
                saveReadingState(readingType)

            } catch (e: Exception) {
                Log.e("GeneralReadingVM", "Error drawing card for position $position", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun resetAndDrawNew(readingType: String) {
        drawnCards = emptyList()
        isCardsDrawn = false
        // Durumu temizle
        clearReadingState(readingType)
    }

    fun loadReadingState(readingType: String) {
        val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
        val isDrawnKey = "is_drawn_$normalizedReadingType"
        val cardsKey = "cards_$normalizedReadingType"
        
        try {
            val isDrawn = sharedPreferences.getBoolean(isDrawnKey, false)
            val cardsJson = sharedPreferences.getString(cardsKey, null)
            
            if (cardsJson != null) {
                val type = object : TypeToken<List<ReadingCardState>>() {}.type
                val loadedCards = gson.fromJson<List<ReadingCardState>>(cardsJson, type)
                drawnCards = loadedCards ?: emptyList()
                isCardsDrawn = isDrawn
                
                Log.d("GeneralReadingViewModel", "Loaded reading state for $readingType. Cards: ${drawnCards.size}, isDrawn: $isCardsDrawn")
            } else {
                // İlk kez açılıyorsa temiz durum
                drawnCards = emptyList()
                isCardsDrawn = false
            }
        } catch (e: Exception) {
            Log.e("GeneralReadingViewModel", "Error loading reading state", e)
            drawnCards = emptyList()
            isCardsDrawn = false
        }
    }

    private fun saveReadingState(readingType: String) {
        val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
        val isDrawnKey = "is_drawn_$normalizedReadingType"
        val cardsKey = "cards_$normalizedReadingType"
        
        try {
            sharedPreferences.edit().apply {
                putBoolean(isDrawnKey, isCardsDrawn)
                putString(cardsKey, gson.toJson(drawnCards))
                apply()
            }
            
            Log.d("GeneralReadingViewModel", "Saved reading state for $readingType. Cards: ${drawnCards.size}, isDrawn: $isCardsDrawn")
        } catch (e: Exception) {
            Log.e("GeneralReadingViewModel", "Error saving reading state", e)
        }
    }

    private fun clearReadingState(readingType: String) {
        val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
        val isDrawnKey = "is_drawn_$normalizedReadingType"
        val cardsKey = "cards_$normalizedReadingType"
        
        try {
            sharedPreferences.edit().apply {
                remove(isDrawnKey)
                remove(cardsKey)
                apply()
            }
            
            Log.d("GeneralReadingViewModel", "Cleared reading state for $readingType")
        } catch (e: Exception) {
            Log.e("GeneralReadingViewModel", "Error clearing reading state", e)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GeneralReadingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GeneralReadingViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class ReadingCardState(
    val index: Int,
    val card: TarotCard,
    val isRevealed: Boolean
) 