package com.denizcan.astrosea.presentation.home

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
import com.denizcan.astrosea.presentation.notifications.NotificationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class DailyTarotViewModel(private val context: Context) : ViewModel() {
    
    var dailyCards by mutableStateOf<List<DailyCardState>>(emptyList())
        private set
    
    var hasDrawnToday by mutableStateOf(false)
        set
    
    var isLoading by mutableStateOf(true)
        private set
    
    // Kartlar yüklendiğinde çağrılacak callback
    private var onCardsLoaded: (() -> Unit)? = null
    
    // Callback'i set etmek için fonksiyon
    fun setOnCardsLoadedCallback(callback: () -> Unit) {
        onCardsLoaded = callback
    }
    
    private val allTarotCards: List<TarotCard> by lazy {
        JsonLoader(context).loadTarotCards()
    }
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid
    private val notificationManager = NotificationManager(context)
    
    init {
        if (userId != null) {
            checkTodayDrawStatus()
        } else {
            // Kullanıcı giriş yapmamış, boş kartlar göster
            dailyCards = List(3) { index ->
                DailyCardState(
                    index = index,
                    card = null,
                    isRevealed = false
                )
            }
            isLoading = false
        }
    }
    
    private fun checkTodayDrawStatus() {
        viewModelScope.launch {
            try {
                isLoading = true
                val currentDate = getCurrentDateString()
                val userDoc = userId?.let {
                    firestore.collection("users").document(it).get().await()
                }
                
                val lastDrawDate = userDoc?.getString("last_draw_date") ?: ""
                
                if (lastDrawDate != currentDate) {
                    // Yeni günlük kartlar henüz çekilmemiş, arka yüzlerini göster
                    dailyCards = List(3) { index ->
                        DailyCardState(
                            index = index,
                            card = null,
                            isRevealed = false
                        )
                    }
                    hasDrawnToday = false
                    
                    // Günlük kartlar çekildi bildirimi gönder
                    if (userId != null) {
                        try {
                            // İlk günlük açılım bildirimi gönder
                            notificationManager.sendFirstDailyReadingNotification(userId!!)
                            Log.d("DailyTarotViewModel", "First daily reading notification sent")
                        } catch (e: Exception) {
                            Log.e("DailyTarotViewModel", "Error sending daily notification", e)
                        }
                    }
                } else {
                    // Bugün kartlar zaten çekilmiş, kaydedilen verileri yükle
                    loadSavedCards()
                    hasDrawnToday = true
                    Log.d("DailyTarotViewModel", "Today's cards already drawn, loading saved state")
                }
                
                isLoading = false
            } catch (e: Exception) {
                Log.e("DailyTarotViewModel", "Error checking today's draw status", e)
                isLoading = false
            }
        }
    }
    
    fun drawDailyCards() {
        if (hasDrawnToday || userId == null) return
        // Önce local state'i güncelle (anında UI güncellensin)
        val randomCards = allTarotCards.shuffled().take(3)
        dailyCards = randomCards.mapIndexed { index, card ->
            DailyCardState(
                index = index,
                card = card,
                isRevealed = false
            )
        }.sortedBy { it.index }
        hasDrawnToday = true
        
        // Sonra database'i güncelle (arka planda)
        viewModelScope.launch {
            try {
                val currentDate = getCurrentDateString()
                val userRef = firestore.collection("users").document(userId!!)
                val cardsData = randomCards.mapIndexed { index, card ->
                    "card_${index}_id" to card.id
                }.toMap() + mapOf(
                    "last_draw_date" to currentDate,
                    "card_0_revealed" to false,
                    "card_1_revealed" to false,
                    "card_2_revealed" to false
                )
                userRef.set(cardsData, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("DailyTarotViewModel", "Error updating database for daily cards", e)
            }
        }
    }
    
    fun revealCard(index: Int) {
        if (index < 0 || index >= dailyCards.size || userId == null) return
        
        // Önce local state'i güncelle (anında UI güncellensin)
        val currentCard = dailyCards[index].card
        if (currentCard != null) {
            val updatedCards = dailyCards.toMutableList()
            updatedCards[index] = DailyCardState(
                index = index,
                card = currentCard,
                isRevealed = true
            )
            dailyCards = updatedCards
            
            // Sonra database'i güncelle (arka planda)
            viewModelScope.launch {
                try {
                    firestore.collection("users").document(userId!!)
                        .update("card_${index}_revealed", true)
                        .await()
                } catch (e: Exception) {
                    Log.e("DailyTarotViewModel", "Error updating database for card $index", e)
                }
            }
        }
    }

    fun revealCardLocally(position: Int) {
        val updatedCards = dailyCards.map {
            if (it.index == position) it.copy(isRevealed = true) else it
        }
        dailyCards = updatedCards
        hasDrawnToday = true
    }

    fun revealCardInDatabase(position: Int) {
        viewModelScope.launch {
            val userId = userId ?: return@launch
            val cardKey = "card_${position}_revealed"
            firestore.collection("users").document(userId)
                .update(cardKey, true)
        }
    }
    
    private suspend fun loadSavedCards() {
        try {
            Log.d("DailyTarotViewModel", "loadSavedCards çağrıldı")
            val userDoc = userId?.let {
                firestore.collection("users").document(it).get().await()
            } ?: return
            
            val loadedCards = mutableListOf<DailyCardState>()
            
            for (i in 0 until 3) {
                val cardId = userDoc.getString("card_${i}_id") ?: ""
                val isRevealed = userDoc.getBoolean("card_${i}_revealed") ?: false
                
                Log.d("DailyTarotViewModel", "Firebase'den yüklenen kart $i: cardId=$cardId, isRevealed=$isRevealed")
                
                val card = allTarotCards.find { it.id == cardId }
                if (cardId.isNotEmpty() && card != null) {
                    loadedCards.add(
                        DailyCardState(
                            index = i,
                            card = card,
                            isRevealed = isRevealed
                        )
                    )
                    Log.d("DailyTarotViewModel", "Kart $i yüklendi: ${card.name}, isRevealed=$isRevealed")
                } else {
                    loadedCards.add(
                        DailyCardState(
                            index = i,
                            card = null,
                            isRevealed = false
                        )
                    )
                    Log.d("DailyTarotViewModel", "Kart $i boş olarak yüklendi")
                }
            }
            
            val sortedCards = loadedCards.sortedBy { it.index }
            dailyCards = sortedCards
            Log.d("DailyTarotViewModel", "dailyCards güncellendi. Toplam kart sayısı: ${dailyCards.size}")
            dailyCards.forEach { cardState ->
                Log.d("DailyTarotViewModel", "Kart ${cardState.index}: ${cardState.card?.name ?: "null"}, isRevealed=${cardState.isRevealed}")
            }
           
            // Kartlar yüklendiğinde callback'i çağır
            onCardsLoaded?.invoke()
            
        } catch (e: Exception) {
            Log.e("DailyTarotViewModel", "Error loading saved cards", e)
        }
    }
    
    // Günlük açılım detay sayfasından gelen güncellemeleri dinlemek için
    fun refreshCards() {
        if (userId != null) {
            viewModelScope.launch {
                Log.d("DailyTarotViewModel", "=== Refreshing cards ===")
                Log.d("DailyTarotViewModel", "Current state - hasDrawnToday: $hasDrawnToday, cards count: ${dailyCards.size}")
                
                // Önce günlük durumu kontrol et
                val currentDate = getCurrentDateString()
                val userDoc = firestore.collection("users").document(userId!!).get().await()
                val lastDrawDate = userDoc.getString("last_draw_date") ?: ""
                
                Log.d("DailyTarotViewModel", "Current date: $currentDate, Last draw date: $lastDrawDate")
                
                if (lastDrawDate == currentDate) {
                    // Bugün kartlar çekilmiş, yükle
                    Log.d("DailyTarotViewModel", "Today's cards already drawn, loading saved state")
                    loadSavedCards()
                    hasDrawnToday = true
                    Log.d("DailyTarotViewModel", "Refreshed cards for today")
                } else {
                    // Yeni gün, kartları sıfırla
                    Log.d("DailyTarotViewModel", "New day, resetting cards")
                    dailyCards = List(3) { index ->
                        DailyCardState(
                            index = index,
                            card = null,
                            isRevealed = false
                        )
                    }.sortedBy { it.index }
                    hasDrawnToday = false
                    Log.d("DailyTarotViewModel", "Reset cards for new day")
                }
            }
        }
    }
    
    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DailyTarotViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DailyTarotViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class DailyCardState(
    val index: Int,
    val card: TarotCard?,
    val isRevealed: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DailyCardState

        if (index != other.index) return false
        if (card?.id != other.card?.id) return false
        if (isRevealed != other.isRevealed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + (card?.id?.hashCode() ?: 0)
        result = 31 * result + isRevealed.hashCode()
        return result
    }
} 