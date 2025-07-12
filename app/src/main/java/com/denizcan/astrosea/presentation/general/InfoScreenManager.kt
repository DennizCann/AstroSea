package com.denizcan.astrosea.presentation.general

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class InfoScreenManager(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("info_screen_manager", Context.MODE_PRIVATE)
    }
    
    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    /**
     * Belirli bir açılım için info ekranının bugün gösterilip gösterilmeyeceğini kontrol eder
     * @param readingType Açılım türü (örn: "İLİŞKİ AÇILIMI", "KARİYER AÇILIMI")
     * @return true eğer info ekranı bugün gösterilmemişse, false eğer gösterilmişse
     */
    fun shouldShowInfoScreen(readingType: String): Boolean {
        val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
        val currentDate = getCurrentDateString()
        val lastShownDate = sharedPreferences.getString("info_$normalizedReadingType", "")
        
        val shouldShow = lastShownDate != currentDate
        Log.d("InfoScreenManager", "Info screen for $readingType: shouldShow=$shouldShow, lastShown=$lastShownDate, current=$currentDate")
        
        return shouldShow
    }
    
    /**
     * Belirli bir açılım için info ekranının gösterildiğini kaydeder
     * @param readingType Açılım türü
     */
    fun markInfoScreenAsShown(readingType: String) {
        val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
        val currentDate = getCurrentDateString()
        
        sharedPreferences.edit()
            .putString("info_$normalizedReadingType", currentDate)
            .apply()
            
        Log.d("InfoScreenManager", "Marked info screen as shown for $readingType on $currentDate")
    }
    
    /**
     * Belirli bir açılım için info ekranı kaydını temizler (test için)
     * @param readingType Açılım türü
     */
    fun clearInfoScreenRecord(readingType: String) {
        val normalizedReadingType = readingType.trim().replace(" ", "_").replace("–", "_").replace("-", "_")
        
        sharedPreferences.edit()
            .remove("info_$normalizedReadingType")
            .apply()
            
        Log.d("InfoScreenManager", "Cleared info screen record for $readingType")
    }
    
    /**
     * Tüm info ekranı kayıtlarını temizler (test için)
     */
    fun clearAllInfoScreenRecords() {
        sharedPreferences.edit().clear().apply()
        Log.d("InfoScreenManager", "Cleared all info screen records")
    }
} 