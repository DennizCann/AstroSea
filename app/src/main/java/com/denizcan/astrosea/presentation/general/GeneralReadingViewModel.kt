package com.denizcan.astrosea.presentation.general

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.denizcan.astrosea.model.TarotCard
import com.denizcan.astrosea.util.JsonLoader
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

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid
    


    init {
        Log.d("GeneralReadingViewModel", "ViewModel initialized.")
        // Kullanıcı değişikliklerini dinle
        checkUserChange()
    }
    
    private fun checkUserChange() {
        // Kullanıcı değiştiğinde verileri temizle
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            Log.d("GeneralReadingViewModel", "User changed: ${currentUser?.uid}")
            
            // Kullanıcı değiştiğinde state'i temizle
            if (currentUser != null) {
                drawnCards = emptyList()
                isCardsDrawn = false
                isLoading = false
            }
        }
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
                    // Diğer açılımlar için Firebase kullan
                    drawOtherReadingCardForPosition(readingType, position)
                }

            } catch (e: Exception) {
                Log.e("GeneralReadingVM", "Error drawing card for position $position", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    private suspend fun drawOtherReadingCardForPosition(readingType: String, position: Int) {
        if (userId == null) return
        
        try {
            // Firebase'den mevcut açılımı kontrol et
            val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
            val userDoc = firestore.collection("users").document(userId!!).get().await()
            val readingData = userDoc.get("reading_$normalizedReadingType") as? Map<String, Any>
            
            val usedCardIds = drawnCards.map { it.card.id }
            val availableCards = allTarotCards.filter { it.id !in usedCardIds }

            if (availableCards.isEmpty()) {
                Log.w("GeneralReadingVM", "No more unique cards to draw.")
                return
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

            // Firebase'e kaydet
            val readingMap = readingData?.toMutableMap() ?: mutableMapOf()
            readingMap["cards"] = drawnCards.map { 
                mapOf(
                    "index" to it.index,
                    "cardId" to it.card.id,
                    "isRevealed" to it.isRevealed
                )
            }
            readingMap["isDrawn"] = isCardsDrawn
            
            firestore.collection("users").document(userId!!)
                .update("reading_$normalizedReadingType", readingMap)
                .await()
                
        } catch (e: Exception) {
            Log.e("GeneralReadingVM", "Error drawing other reading card", e)
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
                // Bugün henüz kart çekilmemiş, 3 kartı birden çek
                val randomCards = allTarotCards.shuffled().take(3)
                
                val cardsData = randomCards.mapIndexed { index, card ->
                    "card_${index}_id" to card.id
                }.toMap() + mapOf(
                    "last_draw_date" to currentDate,
                    "card_0_revealed" to false,
                    "card_1_revealed" to false,
                    "card_2_revealed" to false
                )

                firestore.collection("users").document(userId!!)
                    .set(cardsData, SetOptions.merge()).await()

                // Sadece tıklanan kartı aç
                val clickedCard = randomCards[position]
                val newCardState = ReadingCardState(
                    index = position,
                    card = clickedCard,
                    isRevealed = true
                )
                
                val updatedList = (drawnCards + newCardState).sortedBy { it.index }
                drawnCards = updatedList

                // Firebase'e güncelleme
                firestore.collection("users").document(userId!!)
                    .update("card_${position}_revealed", true)
                    .await()
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
            Log.d("GeneralReadingVM", "Loaded ${loadedCards.size} daily cards from Firebase")
        } catch (e: Exception) {
            Log.e("GeneralReadingVM", "Error loading daily cards from Firebase", e)
        }
    }

    fun resetAndDrawNew(readingType: String) {
        drawnCards = emptyList()
        isCardsDrawn = false
        
        // Tüm açılımlar için Firebase'den temizle
        clearReadingFromFirebase(readingType)
    }

    private fun clearReadingFromFirebase(readingType: String) {
        if (userId == null) return

        viewModelScope.launch {
            try {
                val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
                
                if (readingType.trim() == "GÜNLÜK AÇILIM") {
                    // Günlük açılım için özel temizleme
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
                } else {
                    // Diğer açılımlar için temizleme
                    firestore.collection("users").document(userId!!)
                        .update("reading_$normalizedReadingType", null)
                        .await()
                }
                
                Log.d("GeneralReadingViewModel", "Cleared reading state for $readingType from Firebase")
            } catch (e: Exception) {
                Log.e("GeneralReadingViewModel", "Error clearing reading state from Firebase", e)
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

        // Diğer açılımlar için Firebase'den yükle
        viewModelScope.launch {
            loadOtherReadingFromFirebase(readingType)
        }
    }
    
    private suspend fun loadOtherReadingFromFirebase(readingType: String) {
        if (userId == null) return
        
        try {
            val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
            val userDoc = firestore.collection("users").document(userId!!).get().await()
            val readingData = userDoc.get("reading_$normalizedReadingType") as? Map<String, Any>
            
            if (readingData != null) {
                val cardsData = readingData["cards"] as? List<Map<String, Any>>
                val isDrawn = readingData["isDrawn"] as? Boolean ?: false
                
                if (cardsData != null) {
                    val loadedCards = mutableListOf<ReadingCardState>()
                    
                    for (cardData in cardsData) {
                        val index = (cardData["index"] as? Long)?.toInt() ?: 0
                        val cardId = cardData["cardId"] as? String ?: ""
                        val isRevealed = cardData["isRevealed"] as? Boolean ?: false
                        
                        val card = allTarotCards.find { it.id == cardId }
                        if (card != null) {
                            loadedCards.add(
                                ReadingCardState(
                                    index = index,
                                    card = card,
                                    isRevealed = isRevealed
                                )
                            )
                        }
                    }
                    
                    drawnCards = loadedCards.sortedBy { it.index }
                    isCardsDrawn = isDrawn
                    
                    Log.d("GeneralReadingViewModel", "Loaded reading state for $readingType from Firebase. Cards: ${drawnCards.size}, isDrawn: $isCardsDrawn")
                } else {
                    drawnCards = emptyList()
                    isCardsDrawn = false
                }
            } else {
                drawnCards = emptyList()
                isCardsDrawn = false
            }
        } catch (e: Exception) {
            Log.e("GeneralReadingViewModel", "Error loading reading state from Firebase", e)
            drawnCards = emptyList()
            isCardsDrawn = false
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