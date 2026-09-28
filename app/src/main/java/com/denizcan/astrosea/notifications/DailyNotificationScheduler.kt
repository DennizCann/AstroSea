package com.denizcan.astrosea.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

/**
 * Günlük açılım bildirimlerini AlarmManager ile zamanlar.
 * Uygulama kapalıyken bile çalışır.
 */
object DailyNotificationScheduler {
    
    private const val TAG = "DailyNotificationScheduler"
    private const val NOTIFICATION_PREFS = "notification_prefs"
    private const val DAILY_NOTIFICATIONS_ENABLED = "daily_notifications_enabled"
    /**
     * Günün ilk bildirimi mevcut sabah bildirimi olarak kalır. Diğer dört
     * zaman, kullanıcı henüz günlük açılımını yapmadıysa gönderilir.
     */
    private val reminderTimes = listOf(
        10 to 0,
        13 to 0,
        16 to 0,
        19 to 0,
        22 to 0
    )
    
    /**
     * Günlük beş bildirimi zamanlar.
     */
    fun scheduleDailyNotification(context: Context) {
        reminderTimes.indices.forEach { reminderIndex ->
            scheduleReminder(context, reminderIndex)
        }
        saveAlarmState(context, true)
    }

    /** Kullanıcının profil tercihinden günlük açılım bildirimlerini açar. */
    fun enableDailyNotifications(context: Context) {
        saveUserPreference(context, true)
        scheduleDailyNotification(context)
    }

    /** Kullanıcının profil tercihinden günlük açılım bildirimlerini kapatır. */
    fun disableDailyNotifications(context: Context) {
        saveUserPreference(context, false)
        cancelDailyNotification(context)
    }

    fun areDailyNotificationsEnabled(context: Context): Boolean =
        context.getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE)
            .getBoolean(DAILY_NOTIFICATIONS_ENABLED, true)

    /** Bir bildirim gönderildikten sonra aynı zaman dilimini ertesi gün için kurar. */
    fun scheduleNextOccurrence(context: Context, reminderIndex: Int) {
        scheduleReminder(context, reminderIndex, forceTomorrow = true)
        saveAlarmState(context, true)
    }

    private fun scheduleReminder(
        context: Context,
        reminderIndex: Int,
        forceTomorrow: Boolean = false
    ) {
        val (hour, minute) = reminderTimes[reminderIndex]
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyNotificationReceiver::class.java).apply {
            action = "com.denizcan.astrosea.DAILY_NOTIFICATION"
            putExtra(DailyNotificationReceiver.EXTRA_REMINDER_INDEX, reminderIndex)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderIndex,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (forceTomorrow || timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Exact alarm izni istemeden, cihaz boşta olsa da mümkün olan en yakın zamanda çalışır.
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Log.d(TAG, "Günlük bildirim $reminderIndex zamanlandı: ${calendar.time}")
    }
    
    /**
     * Günlük açılım bildirimlerini iptal eder.
     */
    fun cancelDailyNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        reminderTimes.indices.forEach { reminderIndex ->
            val intent = Intent(context, DailyNotificationReceiver::class.java).apply {
                action = "com.denizcan.astrosea.DAILY_NOTIFICATION"
                putExtra(DailyNotificationReceiver.EXTRA_REMINDER_INDEX, reminderIndex)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderIndex,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        saveAlarmState(context, false)
        Log.d(TAG, "Günlük açılım bildirimleri iptal edildi")
    }
    
    /**
     * Alarm durumunu SharedPreferences'a kaydet
     */
    private fun saveAlarmState(context: Context, isEnabled: Boolean) {
        context.getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("daily_alarm_enabled", isEnabled)
            .apply()
    }

    private fun saveUserPreference(context: Context, isEnabled: Boolean) {
        context.getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(DAILY_NOTIFICATIONS_ENABLED, isEnabled)
            .apply()
    }
    
    /**
     * Alarm'ın etkin olup olmadığını kontrol et
     */
    fun isAlarmEnabled(context: Context): Boolean {
        return context.getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE)
            .getBoolean("daily_alarm_enabled", false)
    }
}
