package com.denizcan.astrosea.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Horoscope : Screen("horoscope")
    object DailyCard : Screen("daily_card")
    object TarotSpreads : Screen("tarot_spreads")
    object BirthChart : Screen("birth_chart")
    object Motivation : Screen("motivation")
    object YesNo : Screen("yes_no")
} 