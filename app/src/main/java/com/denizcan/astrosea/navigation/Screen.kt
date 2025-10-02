package com.denizcan.astrosea.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object EmailValidation : Screen("email_validation/{email}/{password}") {
        fun createRoute(email: String, password: String) = "email_validation/$email/$password"
    }
    object TransitionToAuth : Screen("transition_to_auth")
    object ProfileCompletion1 : Screen("profile_completion_1")
    object ProfileCompletion2 : Screen("profile_completion_2")
    object ProfileCompletion3 : Screen("profile_completion_3")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Horoscope : Screen("horoscope")
    object TarotMeanings : Screen("tarot_meanings")
    object TarotSpreads : Screen("tarot_spreads")
    object BirthChart : Screen("birth_chart")
    object Motivation : Screen("motivation")
    object YesNo : Screen("yes_no")
    object GeneralReadings : Screen("general_readings")
    object Notifications : Screen("notifications")
    object GeneralReadingInfo : Screen("general_reading_info/{readingType}") {
        fun createRoute(readingType: String) = "general_reading_info/$readingType"
    }
    object GeneralReadingDetail : Screen("general_reading_detail/{readingType}") {
        fun createRoute(readingType: String) = "general_reading_detail/$readingType"
    }
}