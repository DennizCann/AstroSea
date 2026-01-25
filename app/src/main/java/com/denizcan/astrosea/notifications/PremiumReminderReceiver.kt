package com.denizcan.astrosea.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.denizcan.astrosea.MainActivity
import com.denizcan.astrosea.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Premium hatırlatma bildirimleri için BroadcastReceiver.
 * Kullanıcı premium değilse bildirim gösterir.
 */
class PremiumReminderReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "PremiumReminderReceiver"
        private const val CHANNEL_ID = "premium_reminder_channel"
        private const val CHANNEL_NAME = "Premium Hatırlatmaları"
        private const val CHANNEL_DESCRIPTION = "Premium üyelik hatırlatma bildirimleri"
        private const val NOTIFICATION_ID = 3001
    }
    
    override fun onReceive(context: Context, intent: Intent?) {
        val reminderType = intent?.getStringExtra("reminder_type") ?: "unknown"
        Log.d(TAG, "Premium hatırlatma alındı: $reminderType")
        
        // Coroutine ile async kontrol
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Kullanıcı giriş yapmış mı?
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId == null) {
                    Log.d(TAG, "Kullanıcı giriş yapmamış, bildirim gönderilmeyecek")
                    return@launch
                }
                
                // Kullanıcı premium mu?
                val isPremium = checkIsPremium(userId)
                if (isPremium) {
                    Log.d(TAG, "Kullanıcı zaten premium, bildirim gönderilmeyecek")
                    // Premium olduysa tüm hatırlatmaları iptal et
                    PremiumReminderScheduler.cancelAllReminders(context)
                    return@launch
                }
                
                // Bildirim kanalını oluştur ve bildirimi göster
                createNotificationChannel(context)
                showNotification(context, reminderType)
                
                // Sonraki bildirimi zamanla
                scheduleNextReminder(context, reminderType)
                
                // Hatırlatma sayısını artır
                PremiumReminderScheduler.incrementReminderCount(context)
                
                // Firestore'a bildirim kaydet
                saveNotificationToFirestore(context, userId, reminderType)
                
            } catch (e: Exception) {
                Log.e(TAG, "Premium hatırlatma hatası", e)
            }
        }
    }
    
    private suspend fun checkIsPremium(userId: String): Boolean {
        return try {
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()
            
            userDoc.getBoolean("isPremium") ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Premium kontrol hatası", e)
            false
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification(context: Context, reminderType: String) {
        // Premium sayfasına yönlendiren intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "premium")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Bildirim mesajları - türe göre seç
        val (title, message) = getNotificationContent(reminderType)
        
        // Renkli logo için bitmap
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.astrosea_icon)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.astrosea_icon)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setCategory(NotificationCompat.CATEGORY_PROMO)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        Log.d(TAG, "Premium bildirim gönderildi: $title")
    }
    
    private fun getNotificationContent(reminderType: String): Pair<String, String> {
        return when (reminderType) {
            "instant" -> Pair(
                "Premium ile Daha Fazlasını Keşfet! ✨",
                "Tüm tarot yorumlarının kilidini aç ve geleceğine tam bir bakış at!"
            )
            "24hour" -> Pair(
                "Sınırsız Tarot Deneyimi Seni Bekliyor! 🔮",
                "Premium üyelikle günlük açılımlarının tam yorumunu oku, sınırsız açılım yap!"
            )
            "5day" -> Pair(
                "Premium Fırsatını Kaçırma! 💫",
                "Detaylı AI yorumları, sınırsız açılımlar ve daha fazlası premium üyelikte!"
            )
            "weekly" -> Pair(
                "AstroSea Premium'a Geç! ⭐",
                "Tarot deneyimini tamamla! Premium ile tüm özelliklere eriş."
            )
            else -> Pair(
                "Premium Üyelik Fırsatı! ✨",
                "AstroSea'nin tüm özelliklerini keşfet!"
            )
        }
    }
    
    private fun scheduleNextReminder(context: Context, currentType: String) {
        val reminderCount = PremiumReminderScheduler.getReminderCount(context)
        
        when (currentType) {
            "instant" -> {
                // Anında bildirimden sonra 24 saat hatırlatma
                PremiumReminderScheduler.markInstantReminderSent(context)
                PremiumReminderScheduler.schedule24HourReminder(context)
                Log.d(TAG, "24 saat hatırlatma zamanlandı")
            }
            "24hour" -> {
                // 24 saat sonra 5 gün hatırlatma
                PremiumReminderScheduler.schedule5DayReminder(context)
                Log.d(TAG, "5 gün hatırlatma zamanlandı")
            }
            "5day", "weekly" -> {
                // 5 gün veya haftalık sonra tekrar haftalık
                if (reminderCount < 10) { // Maksimum 10 hatırlatma
                    PremiumReminderScheduler.scheduleWeeklyReminder(context)
                    Log.d(TAG, "Haftalık hatırlatma zamanlandı (${reminderCount + 1}/10)")
                } else {
                    Log.d(TAG, "Maksimum hatırlatma sayısına ulaşıldı")
                }
            }
        }
    }
    
    private suspend fun saveNotificationToFirestore(context: Context, userId: String, reminderType: String) {
        try {
            val (title, message) = getNotificationContent(reminderType)
            
            val notificationManager = com.denizcan.astrosea.presentation.notifications.NotificationManager(context)
            notificationManager.saveNotificationToFirestore(
                userId = userId,
                title = title,
                message = message,
                type = com.denizcan.astrosea.presentation.notifications.NotificationType.GENERAL
            )
            
            Log.d(TAG, "Premium bildirim Firestore'a kaydedildi")
        } catch (e: Exception) {
            Log.e(TAG, "Firestore kayıt hatası", e)
        }
    }
}
