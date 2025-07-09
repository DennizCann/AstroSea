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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

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
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

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
                // Günlük açılım özel durumu
                if (readingType.trim() == "GÜNLÜK AÇILIM") {
                    drawDailyCardForPosition(position)
                } else {
                    // Diğer açılımlar için normal mantık
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
                }

            } catch (e: Exception) {
                Log.e("GeneralReadingVM", "Error drawing card for position $position", e)
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun drawDailyCardForPosition(position: Int) {
        if (userId == null) return

        try {
            // Firebase'den günlük kartları kontrol et
            val userDoc = firestore.collection("users").document(userId!!).get().await()
            val currentDate = getCurrentDateString()
            val lastDrawDate = userDoc.getString("last_draw_date") ?: ""

            if (lastDrawDate != currentDate) {
                // Bugün henüz kart çekilmemiş, sadece tıklanan pozisyon için kart çek
                val randomCard = allTarotCards.shuffled().first()
                
                val cardsData = mapOf(
                    "card_${position}_id" to randomCard.id,
                    "last_draw_date" to currentDate,
                    "card_${position}_revealed" to true
                )

                firestore.collection("users").document(userId!!)
                    .set(cardsData, SetOptions.merge()).await()

                // Sadece tıklanan kartı ekle
                val newCardState = ReadingCardState(
                    index = position,
                    card = randomCard,
                    isRevealed = true
                )
                
                val updatedList = (drawnCards + newCardState).sortedBy { it.index }
                drawnCards = updatedList
            } else {
                // Bugün kartlar zaten çekilmiş, sadece tıklanan pozisyonu kontrol et
                val cardId = userDoc.getString("card_${position}_id") ?: ""
                val isRevealed = userDoc.getBoolean("card_${position}_revealed") ?: false
                
                if (cardId.isNotEmpty() && !isRevealed) {
                    // Kart çekilmiş ama açılmamış, şimdi aç
                    val card = allTarotCards.find { it.id == cardId }
                    if (card != null) {
                        val newCardState = ReadingCardState(
                            index = position,
                            card = card,
                            isRevealed = true
                        )
                        
                        val updatedList = (drawnCards + newCardState).sortedBy { it.index }
                        drawnCards = updatedList

                        // Firebase'e güncelleme
                        firestore.collection("users").document(userId!!)
                            .update("card_${position}_revealed", true)
                            .await()
                    }
                } else if (cardId.isNotEmpty() && isRevealed) {
                    // Kart zaten açılmış, yükle
                    val card = allTarotCards.find { it.id == cardId }
                    if (card != null) {
                        val newCardState = ReadingCardState(
                            index = position,
                            card = card,
                            isRevealed = true
                        )
                        
                        val updatedList = (drawnCards + newCardState).sortedBy { it.index }
                        drawnCards = updatedList
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("GeneralReadingVM", "Error drawing daily card for position $position", e)
        }
    }

    private suspend fun loadDailyCardsFromFirebase() {
        if (userId == null) return

        try {
            val userDoc = firestore.collection("users").document(userId!!).get().await()
            val loadedCards = mutableListOf<ReadingCardState>()

            for (i in 0 until 3) {
                val cardId = userDoc.getString("card_${i}_id") ?: ""
                val isRevealed = userDoc.getBoolean("card_${i}_revealed") ?: false

                val card = allTarotCards.find { it.id == cardId }
                if (card != null && isRevealed) {
                    // Sadece açılmış kartları yükle
                    loadedCards.add(
                        ReadingCardState(
                            index = i,
                            card = card,
                            isRevealed = true
                        )
                    )
                }
            }

            drawnCards = loadedCards.sortedBy { it.index }
        } catch (e: Exception) {
            Log.e("GeneralReadingVM", "Error loading daily cards from Firebase", e)
        }
    }

    fun resetAndDrawNew(readingType: String) {
        drawnCards = emptyList()
        isCardsDrawn = false
        
        // Günlük açılım için Firebase'den temizle
        if (readingType.trim() == "GÜNLÜK AÇILIM") {
            clearDailyCardsFromFirebase()
        } else {
            // Diğer açılımlar için SharedPreferences'tan temizle
            clearReadingState(readingType)
        }
    }

    private fun clearDailyCardsFromFirebase() {
        if (userId == null) return

        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId!!)
                    .update(
                        mapOf(
                            "last_draw_date" to "",
                            "card_0_id" to "",
                            "card_1_id" to "",
                            "card_2_id" to "",
                            "card_0_revealed" to false,
                            "card_1_revealed" to false,
                            "card_2_revealed" to false
                        )
                    ).await()
            } catch (e: Exception) {
                Log.e("GeneralReadingVM", "Error clearing daily cards from Firebase", e)
            }
        }
    }

    fun loadReadingState(readingType: String) {
        // Günlük açılım için Firebase'den yükle
        if (readingType.trim() == "GÜNLÜK AÇILIM") {
            viewModelScope.launch {
                loadDailyCardsFromFirebase()
            }
            return
        }

        // Diğer açılımlar için SharedPreferences'tan yükle
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
        // Günlük açılım için Firebase kullanılıyor, SharedPreferences'a kaydetmeye gerek yok
        if (readingType.trim() == "GÜNLÜK AÇILIM") {
            return
        }

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

    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
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