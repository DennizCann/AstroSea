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
    object TarotCards : Screen("tarot_cards")
    object YesNo : Screen("yes_no")
    object TarotSpreads : Screen("tarot_spreads")
    object CustomSpread : Screen("custom_spread")
    object TarotCardDetail : Screen("tarot_card_detail/{cardId}") {
        fun createRoute(cardId: String) = "tarot_card_detail/$cardId"
    }
} 