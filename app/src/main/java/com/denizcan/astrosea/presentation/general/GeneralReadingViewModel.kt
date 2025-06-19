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
import com.denizcan.astrosea.util.JsonLoader
import com.denizcan.astrosea.model.TarotCard
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
        Log.d("GeneralReadingViewModel", "ViewModel initialized. isCardsDrawn: $isCardsDrawn, drawnCards.size: ${drawnCards.size}")
    }
    
    fun loadReadingState(readingType: String) {
        val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
        val isDrawnKey = "is_drawn_$normalizedReadingType"
        val cardsKey = "cards_$normalizedReadingType"
        
        val isDrawn = sharedPreferences.getBoolean(isDrawnKey, false)
        val cardsJson = sharedPreferences.getString(cardsKey, null)
        
        if (isDrawn && cardsJson != null) {
            try {
                val type = object : TypeToken<List<ReadingCardState>>() {}.type
                val savedCards = gson.fromJson<List<ReadingCardState>>(cardsJson, type)
                drawnCards = savedCards
                isCardsDrawn = true
                Log.d("GeneralReadingViewModel", "Loaded saved reading state for $readingType. Cards: ${drawnCards.size}, isDrawn: $isCardsDrawn")
            } catch (e: Exception) {
                Log.e("GeneralReadingViewModel", "Error loading saved state", e)
                // Hata durumunda state'i temizle
                clearReadingState(readingType)
            }
        } else {
            Log.d("GeneralReadingViewModel", "No saved state found for $readingType")
        }
    }
    
    private fun saveReadingState(readingType: String) {
        val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
        val isDrawnKey = "is_drawn_$normalizedReadingType"
        val cardsKey = "cards_$normalizedReadingType"
        
        sharedPreferences.edit().apply {
            putBoolean(isDrawnKey, isCardsDrawn)
            putString(cardsKey, gson.toJson(drawnCards))
            apply()
        }
        
        Log.d("GeneralReadingViewModel", "Saved reading state for $readingType. Cards: ${drawnCards.size}, isDrawn: $isCardsDrawn")
    }
    
    private fun clearReadingState(readingType: String) {
        val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
        val isDrawnKey = "is_drawn_$normalizedReadingType"
        val cardsKey = "cards_$normalizedReadingType"
        
        sharedPreferences.edit().apply {
            remove(isDrawnKey)
            remove(cardsKey)
            apply()
        }
        
        Log.d("GeneralReadingViewModel", "Cleared reading state for $readingType")
    }
    
    fun drawCards(readingType: String) {
        if (isCardsDrawn) {
            Log.d("GeneralReadingViewModel", "Cards already drawn, skipping...")
            return
        }
        
        viewModelScope.launch {
            try {
                isLoading = true
                Log.d("GeneralReadingViewModel", "Starting to draw cards for: $readingType")
                
                // Açılım türüne göre kart sayısını belirle
                val cardCount = when (readingType.trim()) {
                    "GÜNLÜK AÇILIM" -> 3
                    "TEK KART AÇILIMI" -> 1
                    "EVET – HAYIR AÇILIMI" -> 1
                    "GEÇMİŞ, ŞİMDİ, GELECEK" -> 3
                    "DURUM, AKSİYON, SONUÇ" -> 3
                    "İLİŞKİ AÇILIMI" -> 3
                    "UYUMLULUK AÇILIMI" -> 7
                    "DETAYLI İLİŞKİ AÇILIMI" -> 9
                    "MÜCADELELER AÇILIMI" -> 7
                    "TAMAM MI, DEVAM MI" -> 6
                    "GELECEĞİNE GİDEN YOL" -> 5
                    "İŞ YERİNDEKİ PROBLEMLER" -> 6
                    "FİNANSAL DURUM" -> 6
                    else -> 1
                }
                
                // Rastgele kartları çek (hiçbiri aynı olmasın)
                val randomCards = allTarotCards.shuffled().take(cardCount)
                
                // Kart durumlarını oluştur (hepsi açık başlar)
                drawnCards = randomCards.mapIndexed { index, card ->
                    ReadingCardState(
                        index = index,
                        card = card,
                        isRevealed = true // Kartlar otomatik olarak açık başlar
                    )
                }
                
                isCardsDrawn = true
                isLoading = false
                
                // State'i kaydet
                saveReadingState(readingType)
                
                Log.d("GeneralReadingViewModel", "Successfully drew and revealed $cardCount cards for $readingType. isCardsDrawn: $isCardsDrawn")
            } catch (e: Exception) {
                Log.e("GeneralReadingViewModel", "Error drawing cards", e)
                isLoading = false
            }
        }
    }
    
    fun revealCard(index: Int) {
        if (index < 0 || index >= drawnCards.size) return
        
        val updatedCards = drawnCards.toMutableList()
        updatedCards[index] = updatedCards[index].copy(isRevealed = true)
        drawnCards = updatedCards
        
        // State'i kaydet
        saveReadingState(getCurrentReadingType())
        
        Log.d("GeneralReadingViewModel", "Revealed card at index $index. Total revealed: ${drawnCards.count { it.isRevealed }}")
    }
    
    fun revealAllCards() {
        drawnCards = drawnCards.map { it.copy(isRevealed = true) }
        
        // State'i kaydet
        saveReadingState(getCurrentReadingType())
        
        Log.d("GeneralReadingViewModel", "Revealed all cards. Total cards: ${drawnCards.size}")
    }
    
    fun resetReading() {
        drawnCards = emptyList()
        isCardsDrawn = false
        
        // State'i temizle
        clearReadingState(getCurrentReadingType())
        
        Log.d("GeneralReadingViewModel", "Reset reading. isCardsDrawn: $isCardsDrawn, drawnCards.size: ${drawnCards.size}")
    }
    
    private fun getCurrentReadingType(): String {
        // Bu fonksiyon ViewModel'in hangi açılım türü için kullanıldığını belirlemek için
        // SharedPreferences'tan mevcut açılım türünü bulur
        val allKeys = sharedPreferences.all.keys
        val readingTypeKeys = allKeys.filter { it.startsWith("is_drawn_") }
        
        if (readingTypeKeys.isNotEmpty()) {
            // En son kullanılan açılım türünü döndür
            val lastKey = readingTypeKeys.last()
            return lastKey.removePrefix("is_drawn_").replace("_", " ")
        }
        
        return ""
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