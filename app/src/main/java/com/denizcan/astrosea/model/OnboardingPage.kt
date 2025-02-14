package com.denizcan.astrosea.model

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int = 0 // Varsayılan değer ekledik, artık kullanılmıyor
) 