package com.denizcan.astrosea.presentation.tarot.meanings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.denizcan.astrosea.R
import com.denizcan.astrosea.util.JsonLoader
import com.denizcan.astrosea.model.TarotCard
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TarotMeaningsViewModel(
    private val jsonLoader: JsonLoader
) : ViewModel() {
    private val _cards = mutableStateOf<List<TarotCard>>(emptyList())
    val cards: List<TarotCard> get() = _cards.value

    var filteredCards by mutableStateOf<List<TarotCard>>(emptyList())
        private set

    var selectedCategory by mutableStateOf(TarotCategory.ALL)
        private set

    var selectedTab by mutableStateOf(0)
        private set

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        _cards.value = jsonLoader.loadTarotCards()
    }

    private fun filterCards() {
        filteredCards = when (selectedTab) {
            0 -> { // TAROT sekmesi
                when (selectedCategory) {
                    TarotCategory.ALL -> cards
                    TarotCategory.MAJOR -> cards.filter { it.type == "major" }
                    TarotCategory.CUPS -> cards.filter { it.type == "cups" }
                    TarotCategory.SWORDS -> cards.filter { it.type == "swords" }
                    TarotCategory.WANDS -> cards.filter { it.type == "wands" }
                    TarotCategory.PENTACLES -> cards.filter { it.type == "pentacles" }
                }
            }
            1 -> { // RÜN sekmesi
                emptyList() // Şimdilik boş, rün kartları eklendiğinde güncellenecek
            }
            else -> emptyList()
        }
    }

    fun onTabSelected(tabIndex: Int) {
        selectedTab = tabIndex
        filterCards()
    }

    fun onCategorySelected(category: TarotCategory) {
        selectedCategory = category
        filterCards()
    }

    fun onCardClick(card: TarotCard) {
        // Kart tıklama işlemi
    }
}

enum class TarotCategory {
    ALL, MAJOR, CUPS, SWORDS, WANDS, PENTACLES
} 