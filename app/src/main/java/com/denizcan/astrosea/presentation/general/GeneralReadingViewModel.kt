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
import com.denizcan.astrosea.util.GroqService
import com.denizcan.astrosea.model.ReadingFormat
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
    
    var isGeneratingReading by mutableStateOf(false)
        private set
    
    var generatedReading by mutableStateOf<String?>(null)
        private set
    
    var readingError by mutableStateOf<String?>(null)
        private set
    
    // Günlük açılım state'inin yüklenip yüklenmediğini kontrol etmek için
    private var dailyStateLoaded = false
    
    // Groq Service
    private val groqService = GroqService()
    
    // Reading Formats
    private val readingFormats by lazy {
        JsonLoader(context).loadReadingFormats()
    }

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
        Log.d("GeneralReadingVM", "setDailyTarotViewModel çağrıldı. dailyViewModel: $dailyViewModel")
        dailyTarotViewModel = dailyViewModel
        dailyStateLoaded = false // Yeni DailyTarotViewModel set edildiğinde flag'i sıfırla
        Log.d("GeneralReadingVM", "DailyTarotViewModel set edildi. dailyTarotViewModel: $dailyTarotViewModel")
        
        // DailyTarotViewModel'e callback set et
        dailyViewModel.setOnCardsLoadedCallback {
            Log.d("GeneralReadingVM", "DailyTarotViewModel kartları yüklendi, state güncelleniyor")
            updateDrawnCardsFromDailyViewModel()
            dailyStateLoaded = true
        }
        
        // Eğer kartlar zaten yüklüyse hemen güncelle
        if (dailyViewModel.dailyCards.isNotEmpty()) {
            Log.d("GeneralReadingVM", "DailyTarotViewModel kartları zaten yüklü, hemen güncelleniyor")
            updateDrawnCardsFromDailyViewModel()
            dailyStateLoaded = true
        }
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
        if (readingType.trim() == "GÜNLÜK AÇILIM") {
            drawDailyCardForPosition(position)
            return
        }
        if (drawnCards.any { it.index == position } || isLoading) return

        viewModelScope.launch {
            isLoading = true
            try {
                // Diğer açılımlar için Firebase kullan
                drawOtherReadingCardForPosition(readingType, position)

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

    private fun drawDailyCardForPosition(position: Int) {
        if (userId == null) return
        Log.d("GeneralReadingVM", "=== Drawing daily card for position $position ===")
        Log.d("GeneralReadingVM", "dailyTarotViewModel: $dailyTarotViewModel")
        dailyTarotViewModel?.let { dailyVM ->
            Log.d("GeneralReadingVM", "DailyTarotViewModel mevcut. hasDrawnToday: ${dailyVM.hasDrawnToday}")
            if (!dailyVM.hasDrawnToday) {
                Log.d("GeneralReadingVM", "Kartlar henüz çekilmemiş, çekiliyor...")
                dailyVM.drawDailyCards()
            }
            // Basit kart açma - diğer açılımlar gibi
            Log.d("GeneralReadingVM", "Kart açılıyor: position $position")
            dailyVM.revealCard(position)
            updateDrawnCardsFromDailyViewModel()
            Log.d("GeneralReadingVM", "Kart açma tamamlandı")
        } ?: run {
            Log.e("GeneralReadingVM", "DailyTarotViewModel null! Kart açılamıyor.")
        }
    }
    
    private fun updateDrawnCardsFromDailyViewModel() {
        Log.d("GeneralReadingVM", "updateDrawnCardsFromDailyViewModel çağrıldı")
        dailyTarotViewModel?.let { dailyVM ->
            Log.d("GeneralReadingVM", "DailyTarotViewModel mevcut. Kart sayısı: ${dailyVM.dailyCards.size}")
            Log.d("GeneralReadingVM", "DailyTarotViewModel kartları:")
            dailyVM.dailyCards.forEach { cardState ->
                Log.d("GeneralReadingVM", "  Kart ${cardState.index}: ${cardState.card?.name ?: "null"}, isRevealed=${cardState.isRevealed}")
            }
            val dailyCards = dailyVM.dailyCards.sortedBy { it.index }
            drawnCards = dailyCards.map { dailyCardState ->
                ReadingCardState(
                    index = dailyCardState.index,
                    card = dailyCardState.card,
                    isRevealed = dailyCardState.isRevealed
                )
            }
            // Kartların çekilip çekilmediğini kontrol et
            isCardsDrawn = dailyCards.isNotEmpty() && dailyCards.any { it.card != null }
            Log.d("GeneralReadingVM", "drawnCards güncellendi. Yeni kart sayısı: ${drawnCards.size}")
            Log.d("GeneralReadingVM", "GeneralReadingViewModel kartları:")
            drawnCards.forEach { cardState ->
                Log.d("GeneralReadingVM", "  Kart ${cardState.index}: ${cardState.card?.name ?: "null"}, isRevealed=${cardState.isRevealed}")
            }
        } ?: run {
            Log.e("GeneralReadingVM", "DailyTarotViewModel null!")
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
        if (readingType.trim() == "GÜNLÜK AÇILIM") {
            // Günlük açılım için state'i sadece bir kere yükle
            if (!dailyStateLoaded) {
                updateDrawnCardsFromDailyViewModel()
                dailyStateLoaded = true
                Log.d("GeneralReadingVM", "Günlük açılım state'i ilk kez yüklendi")
            } else {
                Log.d("GeneralReadingVM", "Günlük açılım state'i zaten yüklü, tekrar yüklenmiyor")
            }
        } else {
            viewModelScope.launch {
                loadOtherReadingFromFirebase(readingType)
            }
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

    fun revealCardLocally(readingType: String, position: Int) {
        if (readingType.trim() == "GÜNLÜK AÇILIM") {
            dailyTarotViewModel?.let { dailyVM ->
                dailyVM.revealCardLocally(position)
                updateDrawnCardsFromDailyViewModel()
            }
        }
        // diğer açılımlar için mevcut mantık
        if (readingType.trim() != "GÜNLÜK AÇILIM") {
            val index = drawnCards.indexOfFirst { it.index == position }
            if (index != -1) {
                drawnCards = drawnCards.toMutableList().apply {
                    this[index] = this[index].copy(isRevealed = true)
                }
            }
        }
    }

    suspend fun revealCardInDatabase(readingType: String, position: Int) {
        if (readingType.trim() == "GÜNLÜK AÇILIM") {
            dailyTarotViewModel?.let { dailyVM ->
                dailyVM.revealCardInDatabase(position)
            }
        }
        // diğer açılımlar için mevcut mantık
        if (readingType.trim() != "GÜNLÜK AÇILIM") {
            val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
            val userDoc = firestore.collection("users").document(userId!!).get().await()
            val readingData = userDoc.get("reading_$normalizedReadingType") as? Map<String, Any>

            val updatedCards = (readingData?.get("cards") as? List<Map<String, Any>>)?.mapIndexed { i, cardMap ->
                if (i == position) {
                    cardMap.toMutableMap().apply {
                        this["isRevealed"] = true
                    }
                } else {
                    cardMap
                }
            } ?: mutableListOf()

            val updatedReadingMap = readingData?.toMutableMap() ?: mutableMapOf()
            updatedReadingMap["cards"] = updatedCards
            updatedReadingMap["isDrawn"] = isCardsDrawn // Keep this as it's part of the reading state

            firestore.collection("users").document(userId!!)
                .update("reading_$normalizedReadingType", updatedReadingMap)
                .await()
        }
    }

    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    private fun normalizeReadingType(readingType: String): String {
        return readingType
            .replace(" ", "_")
            .replace("–", "_")  // en dash
            .replace("-", "_")  // hyphen
            .replace(",", "_")
            .replace("?", "_")
            .replace("!", "_")
            .replace(".", "_")
            .replace(":", "_")
            .replace(";", "_")
            .replace("'", "_")
            .replace("\"", "_")
            .replace("(", "_")
            .replace(")", "_")
            .replace("[", "_")
            .replace("]", "_")
            .replace("{", "_")
            .replace("}", "_")
            .replace("/", "_")
            .replace("\\", "_")
            .replace("|", "_")
            .replace("`", "_")
            .replace("~", "_")
            .replace("@", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("%", "_")
            .replace("^", "_")
            .replace("&", "_")
            .replace("*", "_")
            .replace("+", "_")
            .replace("=", "_")
            .replace("<", "_")
            .replace(">", "_")
            .replace("__", "_")  // Çift alt çizgileri tek alt çizgiye çevir
            .replace("__", "_")  // Tekrar kontrol et (3'lü alt çizgiler için)
            .trim()
    }
    
    fun generateReading(readingType: String) {
        // Kartların çekilip çekilmediğini kontrol et
        val hasCards = drawnCards.isNotEmpty() && drawnCards.any { it.card != null }
        if (!hasCards) {
            readingError = "Önce kartları çekmeniz gerekiyor."
            return
        }
        
        val revealedCards = drawnCards.filter { it.isRevealed && it.card != null }
        if (revealedCards.isEmpty()) {
            readingError = "En az bir kartı açmanız gerekiyor."
            return
        }
        
        viewModelScope.launch {
            try {
                isGeneratingReading = true
                readingError = null
                generatedReading = null
                
                // Reading format'ını al - readingType'ı JSON key formatına çevir
                val normalizedReadingType = normalizeReadingType(readingType)
                val format = readingFormats?.readingFormats?.get(normalizedReadingType)
                if (format == null) {
                    readingError = "Bu açılım türü için format bulunamadı: $normalizedReadingType"
                    return@launch
                }
                
                // Çekilen kartları TarotCard listesine dönüştür
                val tarotCards = revealedCards.mapNotNull { it.card }
                
                if (tarotCards.isEmpty()) {
                    readingError = "Geçerli kart bulunamadı."
                    return@launch
                }
                
                // Groq'dan yorum oluştur
                val reading = groqService.generateTarotReading(
                    readingType = readingType,
                    drawnCards = tarotCards,
                    readingFormat = format
                )
                
                generatedReading = reading
                Log.d("GeneralReadingViewModel", "Yorum başarıyla oluşturuldu")
                
            } catch (e: Exception) {
                Log.e("GeneralReadingViewModel", "Yorum oluşturulurken hata", e)
                readingError = "Yorum oluşturulurken bir hata oluştu: ${e.message}"
            } finally {
                isGeneratingReading = false
            }
        }
    }
    
    fun clearReading() {
        generatedReading = null
        readingError = null
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