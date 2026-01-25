package com.denizcan.astrosea.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

/**
 * Günlük bildirimleri AlarmManager ile zamanlar.
 * Uygulama kapalıyken bile çalışır.
 */
object DailyNotificationScheduler {
    
    private const val TAG = "DailyNotificationScheduler"
    private const val ALARM_REQUEST_CODE = 1001
    
    // Bildirim saati: 10:00
    private const val NOTIFICATION_HOUR = 10
    private const val NOTIFICATION_MINUTE = 0
    
    /**
     * Günlük 10:00 bildirimini zamanlar
     */
    fun scheduleDailyNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, DailyNotificationReceiver::class.java).apply {
            action = "com.denizcan.astrosea.DAILY_NOTIFICATION"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Bir sonraki 10:00'ı hesapla
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, NOTIFICATION_HOUR)
            set(Calendar.MINUTE, NOTIFICATION_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Eğer şu anki saat 10:00'dan sonraysa, yarın 10:00'a ayarla
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        // Android 12+ için exact alarm izni gerekli
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Günlük bildirim zamanlandı: ${calendar.time}")
            } else {
                // Exact alarm izni yoksa inexact alarm kullan
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Günlük bildirim zamanlandı (inexact): ${calendar.time}")
            }
        } else {
            // Android 11 ve altı
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d(TAG, "Günlük bildirim zamanlandı: ${calendar.time}")
        }
        
        // SharedPreferences'a kaydet (boot receiver için)
        saveAlarmState(context, true)
    }
    
    /**
     * Günlük bildirimi iptal eder
     */
    fun cancelDailyNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, DailyNotificationReceiver::class.java).apply {
            action = "com.denizcan.astrosea.DAILY_NOTIFICATION"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        saveAlarmState(context, false)
        Log.d(TAG, "Günlük bildirim iptal edildi")
    }
    
    /**
     * Alarm durumunu SharedPreferences'a kaydet
     */
    private fun saveAlarmState(context: Context, isEnabled: Boolean) {
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("daily_alarm_enabled", isEnabled)
            .apply()
    }
    
    /**
     * Alarm'ın etkin olup olmadığını kontrol et
     */
    fun isAlarmEnabled(context: Context): Boolean {
        return context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            .getBoolean("daily_alarm_enabled", false)
    }
}
