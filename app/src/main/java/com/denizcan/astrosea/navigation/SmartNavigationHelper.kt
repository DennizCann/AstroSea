package com.denizcan.astrosea.navigation

import android.content.Context
import androidx.navigation.NavController
import com.denizcan.astrosea.presentation.general.InfoScreenManager

class SmartNavigationHelper(
    private val context: Context,
    private val navController: NavController
) {
    
    private val infoScreenManager = InfoScreenManager(context)
    
    /**
     * Açılım sayfasına akıllı navigation yapar
     * Info ekranı bugün gösterilmemişse info ekranına, gösterilmişse direkt detay sayfasına gider
     */
    fun navigateToReading(readingType: String) {
        if (infoScreenManager.shouldShowInfoScreen(readingType)) {
            // Info ekranı bugün gösterilmemiş, info ekranına git
            navController.navigate(Screen.GeneralReadingInfo.createRoute(readingType))
        } else {
            // Info ekranı bugün gösterilmiş, direkt detay sayfasına git
            navController.navigate(Screen.GeneralReadingDetail.createRoute(readingType))
        }
    }
    
    /**
     * Info ekranından detay sayfasına geçerken info ekranının gösterildiğini kaydeder
     */
    fun navigateFromInfoToDetail(readingType: String) {
        infoScreenManager.markInfoScreenAsShown(readingType)
        navController.navigate(Screen.GeneralReadingDetail.createRoute(readingType))
    }
    
    /**
     * Detay sayfasından geri dönerken uygun sayfaya yönlendirir
     * Info ekranına değil, ana açılım sayfasına döner
     */
    fun navigateBackFromDetail(readingType: String) {
        // Hangi ana sayfadan geldiğini belirle ve o sayfaya dön
        when (readingType.trim()) {
            // İlişki açılımları sayfasındaki açılımlar
            "İLİŞKİ AÇILIMI",
            "UYUMLULUK AÇILIMI", 
            "DETAYLI İLİŞKİ AÇILIMI",
            "MÜCADELELER AÇILIMI",
            "TAMAM MI, DEVAM MI" -> {
                // İlişki açılımları sayfasına dön
                navController.navigate("relationship_readings") {
                    popUpTo("relationship_readings") { inclusive = true }
                }
            }
            // Kariyer açılımı sayfasındaki açılımlar
            "KARİYER AÇILIMI",
            "GELECEĞİNE GİDEN YOL",
            "İŞ YERİNDEKİ PROBLEMLER",
            "FİNANSAL DURUM" -> {
                // Kariyer açılımı sayfasına dön
                navController.navigate("career_reading") {
                    popUpTo("career_reading") { inclusive = true }
                }
            }
            // Genel açılımlar sayfasındaki açılımlar (günlük ve evet-hayır dahil)
            "GÜNLÜK AÇILIM",
            "EVET – HAYIR AÇILIMI",
            "TEK KART AÇILIMI",
            "GEÇMİŞ, ŞİMDİ, GELECEK",
            "DURUM, AKSİYON, SONUÇ" -> {
                // Genel açılımlar sayfasına dön
                navController.navigate(Screen.GeneralReadings.route) {
                    popUpTo(Screen.GeneralReadings.route) { inclusive = true }
                }
            }
            else -> {
                // Varsayılan olarak genel açılımlar sayfasına dön
                navController.navigate(Screen.GeneralReadings.route) {
                    popUpTo(Screen.GeneralReadings.route) { inclusive = true }
                }
            }
        }
    }
    
    /**
     * Test için info ekranı kayıtlarını temizler
     */
    fun clearInfoScreenRecords() {
        infoScreenManager.clearAllInfoScreenRecords()
    }
    
    /**
     * Belirli bir açılım için info ekranı kaydını temizler
     */
    fun clearInfoScreenRecord(readingType: String) {
        infoScreenManager.clearInfoScreenRecord(readingType)
    }
} 