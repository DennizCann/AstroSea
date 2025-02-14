package com.denizcan.astrosea.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Horoscope : Screen("horoscope")
    object Tarot : Screen("tarot")
    object Runes : Screen("runes")
    object BirthChart : Screen("birth_chart")
} 