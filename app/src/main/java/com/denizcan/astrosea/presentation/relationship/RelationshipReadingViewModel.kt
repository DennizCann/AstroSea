package com.denizcan.astrosea.presentation.relationship

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.denizcan.astrosea.util.JsonLoader
import com.denizcan.astrosea.model.TarotCard
import kotlinx.coroutines.launch

class RelationshipReadingViewModel(private val context: Context) : ViewModel() {
    
    var drawnCards by mutableStateOf<List<ReadingCardState>>(emptyList())
        private set
    
    var isCardsDrawn by mutableStateOf(false)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    private val allTarotCards: List<TarotCard> by lazy {
        JsonLoader(context).loadTarotCards()
    }
    
    fun drawCards(readingType: String) {
        if (isCardsDrawn) return
        
        viewModelScope.launch {
            try {
                isLoading = true
                
                // Açılım türüne göre kart sayısını belirle
                val cardCount = when (readingType.trim()) {
                    "İLİŞKİ AÇILIMI" -> 3
                    "UYUMLULUK AÇILIMI" -> 7
                    "DETAYLI İLİŞKİ AÇILIMI" -> 9
                    "MÜCADELELER AÇILIMI" -> 7
                    "TAMAM MI, DEVAM MI" -> 6
                    else -> 1
                }
                
                // Rastgele kartları çek (hiçbiri aynı olmasın)
                val randomCards = allTarotCards.shuffled().take(cardCount)
                
                // Kart durumlarını oluştur (hepsi kapalı başlar)
                drawnCards = randomCards.mapIndexed { index, card ->
                    ReadingCardState(
                        index = index,
                        card = card,
                        isRevealed = false
                    )
                }
                
                isCardsDrawn = true
                isLoading = false
                
                Log.d("RelationshipReadingViewModel", "Drew $cardCount cards for $readingType")
            } catch (e: Exception) {
                Log.e("RelationshipReadingViewModel", "Error drawing cards", e)
                isLoading = false
            }
        }
    }
    
    fun revealCard(index: Int) {
        if (index < 0 || index >= drawnCards.size) return
        
        val updatedCards = drawnCards.toMutableList()
        updatedCards[index] = updatedCards[index].copy(isRevealed = true)
        drawnCards = updatedCards
        
        Log.d("RelationshipReadingViewModel", "Revealed card at index $index")
    }
    
    fun revealAllCards() {
        drawnCards = drawnCards.map { it.copy(isRevealed = true) }
        Log.d("RelationshipReadingViewModel", "Revealed all cards")
    }
    
    fun resetReading() {
        drawnCards = emptyList()
        isCardsDrawn = false
        Log.d("RelationshipReadingViewModel", "Reset reading")
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RelationshipReadingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RelationshipReadingViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class ReadingCardState(
    val index: Int,
    val card: TarotCard?,
    val isRevealed: Boolean
) 