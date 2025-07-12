package com.denizcan.astrosea.workers

import android.content.Context
import androidx.work.*
import com.denizcan.astrosea.presentation.notifications.NotificationManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DailyNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val notificationManager = NotificationManager(applicationContext)
                
                // Günlük kartların yenilenip yenilenmediğini kontrol et
                val shouldRenew = notificationManager.shouldRenewDailyCards(userId)
                
                if (shouldRenew) {
                    // Günlük kartlar yenilendi, bildirim gönder
                    val notification = com.denizcan.astrosea.presentation.notifications.Notification(
                        id = "",
                        title = "Yeni Günlük Açılımınız Hazır",
                        message = "Yeni gününüz için 3 kart çekildi. Günlük yorumunuzu okumak için kartlarınızı açın.",
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        type = com.denizcan.astrosea.presentation.notifications.NotificationType.DAILY_TAROT
                    )
                    
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val docRef = firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .add(notification)
                        .await()
                    
                    android.util.Log.d("DailyNotificationWorker", "Daily renewal notification sent: ${docRef.id}")
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("DailyNotificationWorker", "Error in worker", e)
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "daily_notification_worker"

        fun scheduleDailyNotification(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Her gün saat 00:00'da çalışacak şekilde ayarla
            val currentTime = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentTime
            
            // Yarının 00:00'ını hesapla
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            val delay = calendar.timeInMillis - currentTime

            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    dailyWorkRequest
                )
        }

        fun cancelDailyNotification(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
} 