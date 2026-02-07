package com.denizcan.astrosea.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * Premium hatırlatma bildirimlerini AlarmManager ile zamanlar.
 * 
 * Plan:
 * - Anında: Uygulama kapandıktan 30 dakika sonra (ilk kez kullanıcı için)
 * - 1. Hatırlatma: 24 saat sonra
 * - 2. Hatırlatma: 5 gün sonra
 * - Sonra: Haftada 1
 */
object PremiumReminderScheduler {
    
    private const val TAG = "PremiumReminderScheduler"
    
    // Request kodları
    private const val INSTANT_REMINDER_REQUEST_CODE = 2001
    private const val DAILY_REMINDER_REQUEST_CODE = 2002
    private const val WEEKLY_REMINDER_REQUEST_CODE = 2003
    
    // Zaman sabitleri (milisaniye)
    private const val THIRTY_MINUTES = 30 * 60 * 1000L
    private const val TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000L
    private const val FIVE_DAYS = 5 * 24 * 60 * 60 * 1000L
    private const val ONE_WEEK = 7 * 24 * 60 * 60 * 1000L
    
    // Bildirim saati (18:00)
    private const val REMINDER_HOUR = 18
    private const val REMINDER_MINUTE = 0
    
    /**
     * İlk kez kullanıcı için anında bildirim zamanlar (30 dakika sonra)
     */
    fun scheduleInstantReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, PremiumReminderReceiver::class.java).apply {
            action = "com.denizcan.astrosea.PREMIUM_INSTANT_REMINDER"
            putExtra("reminder_type", "instant")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            INSTANT_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = System.currentTimeMillis() + THIRTY_MINUTES
        
        scheduleAlarm(alarmManager, triggerTime, pendingIntent)
        saveReminderState(context, "instant_scheduled", true)
        Log.d(TAG, "Anında premium hatırlatma zamanlandı: 30 dakika sonra")
    }
    
    /**
     * 24 saat sonra bildirim zamanlar
     */
    fun schedule24HourReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, PremiumReminderReceiver::class.java).apply {
            action = "com.denizcan.astrosea.PREMIUM_24H_REMINDER"
            putExtra("reminder_type", "24hour")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 24 saat sonra saat 18:00
        val triggerTime = getNextReminderTime(TWENTY_FOUR_HOURS)
        
        scheduleAlarm(alarmManager, triggerTime, pendingIntent)
        Log.d(TAG, "24 saat premium hatırlatma zamanlandı")
    }
    
    /**
     * 5 gün sonra bildirim zamanlar
     */
    fun schedule5DayReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, PremiumReminderReceiver::class.java).apply {
            action = "com.denizcan.astrosea.PREMIUM_5DAY_REMINDER"
            putExtra("reminder_type", "5day")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 5 gün sonra saat 18:00
        val triggerTime = getNextReminderTime(FIVE_DAYS)
        
        scheduleAlarm(alarmManager, triggerTime, pendingIntent)
        Log.d(TAG, "5 gün premium hatırlatma zamanlandı")
    }
    
    /**
     * Haftalık bildirim zamanlar
     */
    fun scheduleWeeklyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, PremiumReminderReceiver::class.java).apply {
            action = "com.denizcan.astrosea.PREMIUM_WEEKLY_REMINDER"
            putExtra("reminder_type", "weekly")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WEEKLY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 1 hafta sonra saat 18:00
        val triggerTime = getNextReminderTime(ONE_WEEK)
        
        scheduleAlarm(alarmManager, triggerTime, pendingIntent)
        Log.d(TAG, "Haftalık premium hatırlatma zamanlandı")
    }
    
    /**
     * Tüm premium hatırlatmalarını iptal eder
     */
    fun cancelAllReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        listOf(
            INSTANT_REMINDER_REQUEST_CODE to "com.denizcan.astrosea.PREMIUM_INSTANT_REMINDER",
            DAILY_REMINDER_REQUEST_CODE to "com.denizcan.astrosea.PREMIUM_24H_REMINDER",
            WEEKLY_REMINDER_REQUEST_CODE to "com.denizcan.astrosea.PREMIUM_WEEKLY_REMINDER"
        ).forEach { (requestCode, action) ->
            val intent = Intent(context, PremiumReminderReceiver::class.java).apply {
                this.action = action
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        
        clearReminderState(context)
        Log.d(TAG, "Tüm premium hatırlatmaları iptal edildi")
    }
    
    /**
     * Kullanıcının premium olup olmadığını kontrol eder
     */
    suspend fun isUserPremium(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        
        return try {
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()
            
            userDoc.getBoolean("isPremium") ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Premium kontrolü hatası", e)
            false
        }
    }
    
    /**
     * Kullanıcının ilk kez giriş yapıp yapmadığını kontrol eder
     */
    fun isFirstTimeUser(context: Context): Boolean {
        val prefs = context.getSharedPreferences("premium_reminder_prefs", Context.MODE_PRIVATE)
        return !prefs.getBoolean("instant_reminder_sent", false)
    }
    
    /**
     * Anında bildirimin gönderildiğini kaydet
     */
    fun markInstantReminderSent(context: Context) {
        context.getSharedPreferences("premium_reminder_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("instant_reminder_sent", true)
            .apply()
    }
    
    /**
     * Hatırlatma sayısını artır
     */
    fun incrementReminderCount(context: Context) {
        val prefs = context.getSharedPreferences("premium_reminder_prefs", Context.MODE_PRIVATE)
        val currentCount = prefs.getInt("reminder_count", 0)
        prefs.edit().putInt("reminder_count", currentCount + 1).apply()
    }
    
    /**
     * Hatırlatma sayısını al
     */
    fun getReminderCount(context: Context): Int {
        return context.getSharedPreferences("premium_reminder_prefs", Context.MODE_PRIVATE)
            .getInt("reminder_count", 0)
    }
    
    // ==================== PRIVATE HELPERS ====================
    
    private fun scheduleAlarm(alarmManager: AlarmManager, triggerTime: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    private fun getNextReminderTime(delayMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis() + delayMillis
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    
    private fun saveReminderState(context: Context, key: String, value: Boolean) {
        context.getSharedPreferences("premium_reminder_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean(key, value)
            .apply()
    }
    
    /**
     * Sadece alarm state'ini temizler, gönderim geçmişini KORUR.
     * Bu sayede çıkış/giriş yapınca 30 dk timer sıfırlanmaz.
     */
    private fun clearReminderState(context: Context) {
        val prefs = context.getSharedPreferences("premium_reminder_prefs", Context.MODE_PRIVATE)
        
        // Önemli: instant_reminder_sent ve reminder_count'u KORU!
        // Sadece alarm zamanlamasıyla ilgili state'leri temizle
        val instantReminderSent = prefs.getBoolean("instant_reminder_sent", false)
        val reminderCount = prefs.getInt("reminder_count", 0)
        
        prefs.edit()
            .clear()
            .putBoolean("instant_reminder_sent", instantReminderSent)  // Koru
            .putInt("reminder_count", reminderCount)  // Koru
            .apply()
        
        Log.d(TAG, "Alarm state temizlendi (instant_reminder_sent: $instantReminderSent, count: $reminderCount korundu)")
    }
}
