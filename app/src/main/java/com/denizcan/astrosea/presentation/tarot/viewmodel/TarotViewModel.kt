package com.denizcan.astrosea.presentation.tarot.viewmodel

import TarotCard
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.astrosea.util.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TarotViewModel(application: Application) : AndroidViewModel(application) {
    private val jsonParser = JsonParser(application)
    private val _tarotCards = MutableStateFlow<List<TarotCard>>(emptyList())
    val tarotCards: StateFlow<List<TarotCard>> = _tarotCards.asStateFlow()

    init {
        loadTarotCards()
    }

    private fun loadTarotCards() {
        viewModelScope.launch {
            _tarotCards.value = jsonParser.loadTarotCards()
        }
    }

    fun getMajorArcana() = tarotCards.value.filter { it.type == "major" }
    fun getMinorArcana(suit: String) = tarotCards.value.filter { it.suit == suit }
} 