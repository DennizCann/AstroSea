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
import com.denizcan.astrosea.presentation.notifications.NotificationManager as AppNotificationManager
import com.denizcan.astrosea.presentation.notifications.NotificationType
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AlarmManager tarafından tetiklenen BroadcastReceiver.
 * Her gün saat 10:00'da günlük bildirim gösterir.
 */
class DailyNotificationReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "DailyNotificationReceiver"
        private const val CHANNEL_ID = "daily_tarot_channel"
        private const val CHANNEL_NAME = "Günlük Tarot Açılımları"
        private const val CHANNEL_DESCRIPTION = "Günlük tarot kartı açılımları ve hatırlatmalar"
        private const val NOTIFICATION_ID = 2001
    }
    
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "Alarm tetiklendi! Bildirim gönderiliyor...")
        
        // Bildirim kanalını oluştur
        createNotificationChannel(context)
        
        // Bildirimi göster
        showNotification(context)
        
        // Bir sonraki günün alarmını kur (tekrarlayan alarm için)
        DailyNotificationScheduler.scheduleDailyNotification(context)
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
    
    private fun showNotification(context: Context) {
        // Uygulamayı açmak için intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "home") // Ana sayfaya yönlendir
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Bildirim mesajları - rastgele seç
        val messages = listOf(
            Pair("✨ Günlük Kartlarınız Hazır!", "Bugün için 3 kart çekildi. Günlük yorumunuzu keşfedin."),
            Pair("🌟 Yeni Bir Gün, Yeni Bir Açılım!", "Bugünün enerjisini öğrenmek için kartlarınızı açın."),
            Pair("🔮 Günlük Tarot Zamanı!", "Kartlarınız sizi bekliyor. Bugün size ne söylüyorlar?"),
            Pair("⭐ Günlük Açılımınız Hazır!", "Bugünün mesajlarını almak için uygulamayı açın."),
            Pair("🌙 Bugün Neler Olacak?", "Günlük tarot kartlarınız çekildi. Hemen keşfedin!")
        )
        
        val (title, message) = messages.random()
        
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
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        Log.d(TAG, "Bildirim gönderildi: $title")
        
        // Firestore'a da kaydet (uygulama içi bildirim listesi için)
        saveNotificationToFirestore(context, title, message)
    }
    
    /**
     * Bildirimi Firestore'a kaydeder
     */
    private fun saveNotificationToFirestore(context: Context, title: String, message: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.d(TAG, "Kullanıcı giriş yapmamış, Firestore'a kaydedilmedi")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appNotificationManager = AppNotificationManager(context)
                appNotificationManager.saveNotificationToFirestore(
                    userId = userId,
                    title = title,
                    message = message,
                    type = NotificationType.DAILY_TAROT
                )
                Log.d(TAG, "Bildirim Firestore'a kaydedildi")
            } catch (e: Exception) {
                Log.e(TAG, "Firestore kaydetme hatası", e)
            }
        }
    }
}
