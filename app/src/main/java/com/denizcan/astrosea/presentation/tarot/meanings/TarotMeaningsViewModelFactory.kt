package com.denizcan.astrosea.presentation.tarot.meanings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.denizcan.astrosea.util.JsonLoader

class TarotMeaningsViewModelFactory(
    private val jsonLoader: JsonLoader
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TarotMeaningsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TarotMeaningsViewModel(jsonLoader) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 