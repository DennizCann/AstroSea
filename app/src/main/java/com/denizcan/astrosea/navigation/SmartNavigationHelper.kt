package com.denizcan.astrosea.navigation

import android.content.Context
import androidx.navigation.NavController
import com.denizcan.astrosea.presentation.general.InfoScreenManager

class SmartNavigationHelper(
    private val context: Context,
    private val navController: NavController
) {
    
    private val infoScreenManager = InfoScreenManager(context)
    
    fun navigateToReading(readingType: String) {
        if (infoScreenManager.shouldShowInfoScreen(readingType)) {
            navController.navigate(Screen.GeneralReadingInfo.createRoute(readingType)) {
                launchSingleTop = true
            }
        } else {
            navController.navigate(Screen.GeneralReadingDetail.createRoute(readingType)) {
                launchSingleTop = true
            }
        }
    }
    
    fun navigateFromInfoToDetail(readingType: String) {
        infoScreenManager.markInfoScreenAsShown(readingType)
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
    
    fun clearInfoScreenRecords() {
        infoScreenManager.clearAllInfoScreenRecords()
    }
    
    fun clearInfoScreenRecord(readingType: String) {
        infoScreenManager.clearInfoScreenRecord(readingType)
    }
}
