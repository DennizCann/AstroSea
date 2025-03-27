package com.denizcan.astrosea.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Horoscope : Screen("horoscope")
    object TarotMeanings : Screen("tarot_meanings")
    object TarotSpreads : Screen("tarot_spreads")
    object BirthChart : Screen("birth_chart")
    object Motivation : Screen("motivation")
    object YesNo : Screen("yes_no")
} 