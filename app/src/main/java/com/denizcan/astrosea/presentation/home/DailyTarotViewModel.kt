package com.denizcan.astrosea.presentation.home

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.denizcan.astrosea.R
import com.denizcan.astrosea.util.JsonLoader
import com.denizcan.astrosea.model.TarotCard
import com.denizcan.astrosea.presentation.notifications.NotificationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class DailyTarotViewModel(context: Context) : ViewModel() {
    private val context = com.denizcan.astrosea.util.LanguageManager.wrap(context.applicationContext)
    
    var dailyCards by mutableStateOf<List<DailyCardState>>(emptyList())
        private set
    
    var hasDrawnToday by mutableStateOf(false)
        set
    
    var isLoading by mutableStateOf(true)
        private set
    
    // Kartlar yüklendiğinde çağrılacak callback
    private var onCardsLoaded: (() -> Unit)? = null
    
    // Callback'i set etmek için fonksiyon
    fun setOnCardsLoadedCallback(callback: (() -> Unit)?) {
        onCardsLoaded = callback
    }
    
    private suspend fun allTarotCards(): List<TarotCard> = JsonLoader(context.applicationContext).loadTarotCards()
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid
    private val notificationManager = NotificationManager(context)
    private var refreshJob: Job? = null
    
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
            val uid = userId ?: return@launch
            val date = getCurrentDateString()
            var newDay = false
            try {
                withTimeoutOrNull(8_000) {
                    val userDoc = firestore.collection("users").document(uid).get().await()
                    if (userId != uid) return@withTimeoutOrNull
                    if (userDoc.getString("last_draw_date") == date) {
                        loadSavedCards(userDoc)
                        hasDrawnToday = true
                    } else {
                        dailyCards = List(3) { DailyCardState(index = it, card = null, isRevealed = false) }
                        hasDrawnToday = false
                        newDay = true
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("DailyTarotViewModel", "Unable to load daily cards", e)
            } finally {
                if (dailyCards.isEmpty()) {
                    dailyCards = List(3) { DailyCardState(index = it, card = null, isRevealed = false) }
                }
                isLoading = false
            }
            // Inbox maintenance must not hold up the cards or their loading state.
            if (newDay && userId == uid) {
                try {
                    withTimeoutOrNull(6_000) {
                        if (reserveDailyReadyNotification(uid, date)) {
                            notificationManager.saveNotificationToFirestore(
                                userId = uid,
                                title = context.getString(R.string.notif_daily_ready_title),
                                message = context.getString(R.string.notif_daily_ready_message)
                            )
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("DailyTarotViewModel", "Unable to save daily inbox notification", e)
                }
            }
        }
    }

    /** Aynı kullanıcı için aynı gün yalnızca bir "günlük kartlar hazır" kaydına izin verir. */
    private suspend fun reserveDailyReadyNotification(userId: String, currentDate: String): Boolean {
        val userRef = firestore.collection("users").document(userId)
        return firestore.runTransaction { transaction ->
            val userDocument = transaction.get(userRef)
            if (userDocument.getString("daily_ready_notification_date") == currentDate) {
                false
            } else {
                transaction.set(
                    userRef,
                    mapOf("daily_ready_notification_date" to currentDate),
                    SetOptions.merge()
                )
                true
            }
        }.await()
    }
    
    // Kart çekme ve açma işlemini birleştiren fonksiyon
    // Bu sayede race condition önlenir
    fun drawAndRevealCard(index: Int) {
        if (userId == null || isLoading) return
        refreshJob?.cancel()
        isLoading = true
        
        viewModelScope.launch {
            try {
                val currentDate = getCurrentDateString()
                val userDoc = firestore.collection("users").document(userId!!).get().await()
                val lastDrawDate = userDoc.getString("last_draw_date") ?: ""
                
                if (lastDrawDate == currentDate) {
                    // Bugün zaten çekilmiş! Mevcut kartları yükle
                    Log.d("DailyTarotViewModel", "Cards already drawn today, loading from Firestore")
                    
                    // Firestore'dan kartları yükle
                    val loadedCards = mutableListOf<DailyCardState>()
                    for (i in 0 until 3) {
                        val cardId = userDoc.getString("card_${i}_id") ?: ""
                        val isRevealed = userDoc.getBoolean("card_${i}_revealed") ?: false
                        val card = allTarotCards().find { it.id == cardId }
                        loadedCards.add(DailyCardState(index = i, card = card, isRevealed = isRevealed))
                    }
                    dailyCards = loadedCards.sortedBy { it.index }
                    hasDrawnToday = true
                    
                    // Şimdi kartı aç (eğer henüz açılmamışsa)
                    val cardState = dailyCards.getOrNull(index)
                    if (cardState != null && !cardState.isRevealed && cardState.card != null) {
                        // Local state güncelle
                        val updatedCards = dailyCards.toMutableList()
                        updatedCards[index] = cardState.copy(isRevealed = true)
                        dailyCards = updatedCards
                        
                        // Firestore'a kaydet
                        firestore.collection("users").document(userId!!)
                            .update("card_${index}_revealed", true)
                            .await()
                        Log.d("DailyTarotViewModel", "Card $index revealed and saved to Firestore")
                    }
                } else {
                    // Bugün çekilmemiş, yeni kartlar çek
                    Log.d("DailyTarotViewModel", "Drawing new daily cards")
                    val randomCards = allTarotCards().shuffled().take(3)
                    
                    // Local state'i güncelle - tıklanan kart açık olsun
                    dailyCards = randomCards.mapIndexed { i, card ->
                        DailyCardState(
                            index = i,
                            card = card,
                            isRevealed = (i == index) // Tıklanan kart açık
                        )
                    }.sortedBy { it.index }
                    hasDrawnToday = true
                    
                    // Firestore'a kaydet
                    val userRef = firestore.collection("users").document(userId!!)
                    val cardsData = randomCards.mapIndexed { i, card ->
                        "card_${i}_id" to card.id
                    }.toMap() + mapOf(
                        "last_draw_date" to currentDate,
                        "card_0_revealed" to (index == 0),
                        "card_1_revealed" to (index == 1),
                        "card_2_revealed" to (index == 2)
                    )
                    userRef.set(cardsData, SetOptions.merge()).await()
                    Log.d("DailyTarotViewModel", "Daily cards saved to Firestore with card $index revealed")
                }
                
                // Callback'i çağır
                onCardsLoaded?.invoke()
                
            } catch (e: Exception) {
                Log.e("DailyTarotViewModel", "Error in drawAndRevealCard", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    // Sadece mevcut kartı açmak için (kartlar zaten yüklüyse)
    fun revealCard(index: Int) {
        if (index < 0 || index >= dailyCards.size || userId == null) return
        
        val cardState = dailyCards.getOrNull(index) ?: return
        if (cardState.isRevealed || cardState.card == null) return
        
        // Local state'i güncelle
        val updatedCards = dailyCards.toMutableList()
        updatedCards[index] = cardState.copy(isRevealed = true)
        dailyCards = updatedCards
        
        // Firestore'a kaydet
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId!!)
                    .update("card_${index}_revealed", true)
                    .await()
                Log.d("DailyTarotViewModel", "Card $index revealed status saved to Firestore")
            } catch (e: Exception) {
                Log.e("DailyTarotViewModel", "Error saving revealed status for card $index", e)
            }
        }
    }
    
    // Eski fonksiyon - geriye uyumluluk için
    fun drawDailyCards() {
        drawAndRevealCard(0) // Varsayılan olarak ilk kartı aç
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
    
    private suspend fun loadSavedCards(userDoc: DocumentSnapshot) {
        try {
            Log.d("DailyTarotViewModel", "loadSavedCards çağrıldı")
            val loadedCards = mutableListOf<DailyCardState>()
            
            for (i in 0 until 3) {
                val cardId = userDoc.getString("card_${i}_id") ?: ""
                val isRevealed = userDoc.getBoolean("card_${i}_revealed") ?: false
                
                Log.d("DailyTarotViewModel", "Firebase'den yüklenen kart $i: cardId=$cardId, isRevealed=$isRevealed")
                
                val card = allTarotCards().find { it.id == cardId }
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
            
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DailyTarotViewModel", "Error loading saved cards", e)
        }
    }
    
    // Günlük açılım detay sayfasından gelen güncellemeleri dinlemek için
    fun refreshCards() {
        val uid = userId ?: return
        if (isLoading || refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            try {
                val userDoc = withTimeoutOrNull(8_000) {
                    firestore.collection("users").document(uid).get().await()
                } ?: return@launch
                if (userId != uid) return@launch
                if (userDoc.getString("last_draw_date") == getCurrentDateString()) {
                    loadSavedCards(userDoc)
                    hasDrawnToday = true
                } else {
                    dailyCards = List(3) { DailyCardState(index = it, card = null, isRevealed = false) }
                    hasDrawnToday = false
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep the displayed cards on transient network errors instead of crashing the screen.
                Log.e("DailyTarotViewModel", "Unable to refresh daily cards", e)
            }
        }
    }
    
    private fun getCurrentDateString(): String {
        // Sabit locale kullan - farklı cihazlarda tutarlı tarih formatı için
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
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
