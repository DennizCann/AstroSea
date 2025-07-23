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
import com.denizcan.astrosea.presentation.home.DailyTarotViewModel
import com.denizcan.astrosea.presentation.notifications.NotificationManager
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

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
    
    // Günlük açılım için DailyTarotViewModel referansı
    private var dailyTarotViewModel: DailyTarotViewModel? = null

    init {
        Log.d("GeneralReadingViewModel", "ViewModel initialized.")
        // Kullanıcı değişikliklerini dinle
        checkUserChange()
    }
    
    // DailyTarotViewModel'i set etmek için fonksiyon
    fun setDailyTarotViewModel(dailyViewModel: DailyTarotViewModel) {
        dailyTarotViewModel = dailyViewModel
        Log.d("GeneralReadingViewModel", "DailyTarotViewModel set")
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
            
            val usedCardIds = drawnCards.map { it.card?.id }
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
                    "cardId" to it.card?.id,
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
            Log.d("GeneralReadingVM", "=== Drawing daily card for position $position ===")
            
            // DailyTarotViewModel varsa onu kullan
            if (dailyTarotViewModel != null) {
                Log.d("GeneralReadingVM", "Using DailyTarotViewModel for position $position")
                
                // Önce kartları çek (eğer henüz çekilmemişse)
                if (!dailyTarotViewModel!!.hasDrawnToday) {
                    Log.d("GeneralReadingVM", "Cards not drawn today, drawing new cards")
                    dailyTarotViewModel!!.drawDailyCards()
                    // Kartların çekilmesini bekle
                    delay(1500)
                }
                
                // Sonra kartı aç
                Log.d("GeneralReadingVM", "Revealing card at position $position")
                dailyTarotViewModel!!.revealCard(position)
                
                // Kartın açılmasını bekle
                delay(1000)
                
                // DailyTarotViewModel'den güncel durumu al
                updateDrawnCardsFromDailyViewModel()
                
                Log.d("GeneralReadingVM", "Updated from DailyTarotViewModel for position $position")
            } else {
                // Fallback: Eski mantık
                Log.d("GeneralReadingVM", "DailyTarotViewModel not available, using fallback logic")
                drawDailyCardForPositionFallback(position)
            }

        } catch (e: Exception) {
            Log.e("GeneralReadingVM", "Error drawing daily card for position $position", e)
        }
    }
    
    private suspend fun drawDailyCardForPositionFallback(position: Int) {
        val userDoc = firestore.collection("users").document(userId!!).get().await()
        val currentDate = getCurrentDateString()
        val lastDrawDate = userDoc.getString("last_draw_date") ?: ""
        
        if (lastDrawDate != currentDate) {
            // Yeni gün - önce mevcut kartları kontrol et
            Log.d("GeneralReadingVM", "New day, checking if cards already drawn")
            
            val existingCard0 = userDoc.getString("card_0_id")
            val existingCard1 = userDoc.getString("card_1_id")
            val existingCard2 = userDoc.getString("card_2_id")
            
            if (existingCard0?.isNotEmpty() == true && existingCard1?.isNotEmpty() == true && existingCard2?.isNotEmpty() == true) {
                // Kartlar zaten çekilmiş, mevcut kartları kullan
                Log.d("GeneralReadingVM", "Cards already drawn, using existing cards")
                
                val allCards = mutableListOf<ReadingCardState>()
                for (i in 0 until 3) {
                    val cardId = userDoc.getString("card_${i}_id") ?: ""
                    val isRevealed = userDoc.getBoolean("card_${i}_revealed") ?: false
                    
                    val card = allTarotCards.find { it.id == cardId }
                    if (card != null) {
                        val newRevealed = if (i == position) true else isRevealed
                        allCards.add(
                            ReadingCardState(
                                index = i,
                                card = card,
                                isRevealed = newRevealed
                            )
                        )
                        Log.d("GeneralReadingVM", "Using existing card $i: ${card.name}, revealed: $newRevealed")
                    }
                }
                
                drawnCards = allCards.sortedBy { it.index }
                
                // Firebase'e güncelleme yap
                firestore.collection("users").document(userId!!)
                    .update("card_${position}_revealed", true)
                    .await()
                    
                Log.d("GeneralReadingVM", "Updated Firebase for position $position")
                
            } else {
                // Kartlar henüz çekilmemiş, 3 kart çek
                Log.d("GeneralReadingVM", "No existing cards, drawing 3 new cards")
                
                val randomCards = allTarotCards.shuffled().take(3)
                Log.d("GeneralReadingVM", "Drew cards: ${randomCards.mapIndexed { index, card -> "$index:${card.name}" }}")
                
                // Firebase'e kaydet
                val userRef = firestore.collection("users").document(userId!!)
                val cardsData = randomCards.mapIndexed { index, card ->
                    "card_${index}_id" to card.id
                }.toMap() + mapOf(
                    "last_draw_date" to currentDate,
                    "card_0_revealed" to (position == 0),
                    "card_1_revealed" to (position == 1),
                    "card_2_revealed" to (position == 2)
                )
                
                Log.d("GeneralReadingVM", "Saving to Firebase: $cardsData")
                userRef.set(cardsData, SetOptions.merge()).await()
                
                // Kartları güncelle - tıklanan kart açık, diğerleri kapalı
                drawnCards = randomCards.mapIndexed { index, card ->
                    ReadingCardState(
                        index = index,
                        card = card,
                        isRevealed = (index == position)
                    )
                }.sortedBy { it.index }
                
                isCardsDrawn = true
                Log.d("GeneralReadingVM", "Cards set: ${drawnCards.map { "${it.index}:${it.card?.name}:${it.isRevealed}" }}")
            }
            
        } else {
            // Bugün kartlar zaten çekilmiş - sadece tıklanan kartı aç
            Log.d("GeneralReadingVM", "Today's cards already drawn, revealing position $position")
            
            val allCards = mutableListOf<ReadingCardState>()
            
            for (i in 0 until 3) {
                val cardId = userDoc.getString("card_${i}_id") ?: ""
                val isRevealed = userDoc.getBoolean("card_${i}_revealed") ?: false
                
                val card = allTarotCards.find { it.id == cardId }
                if (cardId.isNotEmpty() && card != null) {
                    val newRevealed = if (i == position) true else isRevealed
                    allCards.add(
                        ReadingCardState(
                            index = i,
                            card = card,
                            isRevealed = newRevealed
                        )
                    )
                    Log.d("GeneralReadingVM", "Card $i: ${card.name}, revealed: $newRevealed")
                }
            }
            
            drawnCards = allCards.sortedBy { it.index }
            
            // Firebase'e güncelleme yap
            firestore.collection("users").document(userId!!)
                .update("card_${position}_revealed", true)
                .await()
                
            Log.d("GeneralReadingVM", "Updated Firebase for position $position")
        }
    }
    
    private fun updateDrawnCardsFromDailyViewModel() {
        dailyTarotViewModel?.let { dailyVM ->
            val dailyCards = dailyVM.dailyCards.sortedBy { it.index }
            if (dailyCards.isNotEmpty()) {
                drawnCards = dailyCards.map { dailyCardState ->
                    ReadingCardState(
                        index = dailyCardState.index,
                        card = dailyCardState.card,
                        isRevealed = dailyCardState.isRevealed
                    )
                }
                isCardsDrawn = dailyVM.hasDrawnToday
                
                Log.d("GeneralReadingVM", "Updated drawnCards from DailyTarotViewModel: ${drawnCards.map { "${it.index}:${it.card?.name}:${it.isRevealed}" }}")
            } else {
                Log.d("GeneralReadingVM", "DailyTarotViewModel has no cards yet")
            }
        } ?: run {
            Log.d("GeneralReadingVM", "DailyTarotViewModel is null")
        }
    }

    private suspend fun loadDailyCardsFromFirebase() {
        if (userId == null) return

        try {
            // DailyTarotViewModel varsa onu kullan
            if (dailyTarotViewModel != null) {
                Log.d("GeneralReadingVM", "Using DailyTarotViewModel to load daily cards")
                dailyTarotViewModel!!.refreshCards()
                
                // Kartların yüklenmesini bekle
                delay(1000)
                
                updateDrawnCardsFromDailyViewModel()
                Log.d("GeneralReadingVM", "Successfully loaded daily cards from DailyTarotViewModel")
            } else {
                // Fallback: Eski mantık
                Log.d("GeneralReadingVM", "DailyTarotViewModel not available, using fallback logic")
                loadDailyCardsFromFirebaseFallback()
            }

        } catch (e: Exception) {
            Log.e("GeneralReadingVM", "Error loading daily cards from Firebase", e)
        }
    }
    
    private suspend fun loadDailyCardsFromFirebaseFallback() {
        val userDoc = firestore.collection("users").document(userId!!).get().await()
        val loadedCards = mutableListOf<ReadingCardState>()

        for (i in 0 until 3) {
            val cardId = userDoc.getString("card_${i}_id") ?: ""
            val isRevealed = userDoc.getBoolean("card_${i}_revealed") ?: false
            
            Log.d("GeneralReadingVM", "Firebase data for index $i: cardId=$cardId, isRevealed=$isRevealed")

            val card = allTarotCards.find { it.id == cardId }
            if (cardId.isNotEmpty() && card != null) {
                // Kart çekilmiş, açık veya kapalı olarak göster
                loadedCards.add(
                    ReadingCardState(
                        index = i,
                        card = card,
                        isRevealed = isRevealed
                    )
                )
                Log.d("GeneralReadingVM", "Card $i loaded: ${card.name} (ID: ${card.id}), revealed: $isRevealed")
            } else {
                // Kart henüz çekilmemiş
                loadedCards.add(
                    ReadingCardState(
                        index = i,
                        card = null,
                        isRevealed = false
                    )
                )
                Log.d("GeneralReadingVM", "Card $i not drawn yet or card not found for ID: $cardId")
            }
        }
        
        val sortedCards = loadedCards.sortedBy { it.index }
        Log.d("GeneralReadingVM", "Final cards order:")
        sortedCards.forEach { cardState ->
            Log.d("GeneralReadingVM", "Index ${cardState.index}: ${cardState.card?.name ?: "null"} (ID: ${cardState.card?.id ?: "null"}), revealed: ${cardState.isRevealed}")
        }
        
        // Sadece kartlar gerçekten değiştiyse güncelle
        val cardsChanged = drawnCards.size != sortedCards.size || 
            drawnCards.zip(sortedCards).any { (old, new) -> 
                old.card?.id != new.card?.id || old.isRevealed != new.isRevealed 
            }
        
        if (cardsChanged) {
            Log.d("GeneralReadingVM", "Cards changed, updating state")
            drawnCards = sortedCards
        } else {
            Log.d("GeneralReadingVM", "Cards unchanged, keeping current state")
        }

        Log.d("GeneralReadingVM", "Loaded ${loadedCards.size} daily cards from Firebase")
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
                Log.d("GeneralReadingVM", "=== Loading daily reading state ===")
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
    val card: TarotCard?,
    val isRevealed: Boolean
) 