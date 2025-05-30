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
        private set
    
    var isLoading by mutableStateOf(true)
        private set
    
    private val allTarotCards: List<TarotCard> by lazy {
        JsonLoader(context).loadTarotCards()
    }
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid
    
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
                } else {
                    // Bugün kartlar zaten çekilmiş, kaydedilen verileri yükle
                    loadSavedCards()
                    hasDrawnToday = true
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
        
        viewModelScope.launch {
            try {
                isLoading = true
                
                // Rastgele 3 farklı kart seç
                val randomCards = allTarotCards.shuffled().take(3)
                
                // Kart durumlarını güncelle (kartlar hala kapalı)
                dailyCards = randomCards.mapIndexed { index, card ->
                    DailyCardState(
                        index = index,
                        card = card,
                        isRevealed = false
                    )
                }
                
                // Firestore'a kaydet
                val userRef = firestore.collection("users").document(userId!!)
                
                val cardsData = randomCards.mapIndexed { index, card ->
                    "card_${index}_id" to card.id
                }.toMap() + mapOf(
                    "last_draw_date" to getCurrentDateString(),
                    "card_0_revealed" to false,
                    "card_1_revealed" to false,
                    "card_2_revealed" to false
                )
                
                userRef.set(cardsData, SetOptions.merge()).await()
                
                hasDrawnToday = true
                isLoading = false
            } catch (e: Exception) {
                Log.e("DailyTarotViewModel", "Error drawing daily cards", e)
                isLoading = false
            }
        }
    }
    
    fun revealCard(index: Int) {
        if (index < 0 || index >= dailyCards.size || userId == null) return
        
        viewModelScope.launch {
            try {
                val updatedCards = dailyCards.toMutableList()
                updatedCards[index] = updatedCards[index].copy(isRevealed = true)
                dailyCards = updatedCards
                
                // Firestore'a güncelleme
                firestore.collection("users").document(userId!!)
                    .update("card_${index}_revealed", true)
                    .await()
            } catch (e: Exception) {
                Log.e("DailyTarotViewModel", "Error revealing card", e)
            }
        }
    }
    
    private suspend fun loadSavedCards() {
        try {
            val userDoc = userId?.let {
                firestore.collection("users").document(it).get().await()
            } ?: return
            
            val loadedCards = mutableListOf<DailyCardState>()
            
            for (i in 0 until 3) {
                val cardId = userDoc.getString("card_${i}_id") ?: ""
                val isRevealed = userDoc.getBoolean("card_${i}_revealed") ?: false
                
                val card = allTarotCards.find { it.id == cardId }
                loadedCards.add(
                    DailyCardState(
                        index = i,
                        card = card,
                        isRevealed = isRevealed
                    )
                )
            }
            
            dailyCards = loadedCards
        } catch (e: Exception) {
            Log.e("DailyTarotViewModel", "Error loading saved cards", e)
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
) 