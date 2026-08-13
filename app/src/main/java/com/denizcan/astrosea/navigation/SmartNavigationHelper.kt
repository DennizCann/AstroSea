package com.denizcan.astrosea.navigation

import android.content.Context
import androidx.navigation.NavController

class SmartNavigationHelper(
    private val context: Context,
    private val navController: NavController
) {
    
    fun navigateToReading(readingType: String) {
        navController.navigate(Screen.GeneralReadingDetail.createRoute(readingType)) {
            launchSingleTop = true
        }
    }
    
    fun navigateBackFromDetail(readingType: String) {
        val targetRoute = when (readingType.trim()) {
            "İLİŞKİ AÇILIMI",
            "UYUMLULUK AÇILIMI",
            "DETAYLI İLİŞKİ AÇILIMI",
            "MÜCADELELER AÇILIMI",
            "TAMAM MI, DEVAM MI" -> "relationship_readings"
            "KARİYER AÇILIMI",
            "GELECEĞİNE GİDEN YOL",
            "İŞ YERİNDEKİ PROBLEMLER",
            "FİNANSAL DURUM" -> "career_reading"
            else -> Screen.GeneralReadings.route
        }
        if (!navController.popBackStack(targetRoute, inclusive = false)) {
            navController.popBackStack()
        }
    }
}
