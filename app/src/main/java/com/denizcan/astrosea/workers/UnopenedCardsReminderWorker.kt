package com.denizcan.astrosea.workers

import android.content.Context
import androidx.work.*
import com.denizcan.astrosea.presentation.notifications.NotificationManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class UnopenedCardsReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val notificationManager = NotificationManager(applicationContext)
                
                // Açılmamış kartlar var mı kontrol et
                val hasUnopened = notificationManager.hasUnopenedCards(userId)
                
                if (hasUnopened) {
                    // Son hatırlatma bildiriminin ne zaman gönderildiğini kontrol et
                    val lastReminderTime = getLastReminderTime(userId)
                    val currentTime = System.currentTimeMillis()
                    val timeSinceLastReminder = currentTime - lastReminderTime
                    
                    // Son hatırlatmadan en az 2 saat geçmişse yeni hatırlatma gönder
                    if (timeSinceLastReminder >= 2 * 60 * 60 * 1000) { // 2 saat
                        notificationManager.sendUnopenedCardsReminder(userId)
                        saveLastReminderTime(userId, currentTime)
                    }
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("UnopenedCardsReminderWorker", "Error in worker", e)
            Result.failure()
        }
    }

    private suspend fun getLastReminderTime(userId: String): Long {
        return try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            userDoc.getLong("last_reminder_time") ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private suspend fun saveLastReminderTime(userId: String, time: Long) {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("users")
                .document(userId)
                .update("last_reminder_time", time)
                .await()
        } catch (e: Exception) {
            android.util.Log.e("UnopenedCardsReminderWorker", "Error saving reminder time", e)
        }
    }

    companion object {
        private const val WORK_NAME = "unopened_cards_reminder_worker"

        fun scheduleUnopenedCardsReminder(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Her 30 dakikada bir çalışacak şekilde ayarla
            val reminderWorkRequest = PeriodicWorkRequestBuilder<UnopenedCardsReminderWorker>(
                30, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    reminderWorkRequest
                )
        }

        fun cancelUnopenedCardsReminder(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
} 