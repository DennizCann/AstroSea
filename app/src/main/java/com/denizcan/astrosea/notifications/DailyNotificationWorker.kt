package com.denizcan.astrosea.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.denizcan.astrosea.MainActivity
import com.denizcan.astrosea.R
import com.denizcan.astrosea.util.LanguageManager
import com.denizcan.astrosea.presentation.notifications.NotificationManager as AppNotificationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DailyNotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    companion object {
        const val CHANNEL_ID = "daily_tarot_channel"
        const val TAG = "daily-tarot-delivery"
        private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        fun enqueue(context: Context, uid: String, index: Int) {
            val date = today()
            val request = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
                .setInputData(workDataOf("uid" to uid, "date" to date, "index" to index))
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(TAG)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork("$TAG-$uid-$date-$index", ExistingWorkPolicy.KEEP, request)
        }
    }

    override suspend fun doWork(): Result {
        val uid = inputData.getString("uid") ?: return Result.failure()
        val date = inputData.getString("date") ?: return Result.failure()
        val index = inputData.getInt("index", -1)
        val context = LanguageManager.wrap(applicationContext)
        fun canDeliver() = !isStopped && FirebaseAuth.getInstance().currentUser?.uid == uid &&
            DailyNotificationScheduler.areDailyNotificationsEnabled(context) &&
            DailyReminderPolicy.isCurrentSlot(index, date, today(), Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        if (!canDeliver()) return Result.success()
        val prefs = context.getSharedPreferences("daily_notification_delivery", Context.MODE_PRIVATE)
        val deliveryKey = "$uid-$index"
        if (prefs.getString(deliveryKey, null) == date) return Result.success()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID,
                context.getString(R.string.notif_channel_daily_name), NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(R.string.notif_channel_daily_desc)
                enableVibration(true)
            })
        }
        if (!AppNotificationManager(context).checkNotificationPermission()) return Result.success()
        try {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)
            if (index > 0) {
                val user = withTimeoutOrNull(6_000) { userRef.get(Source.SERVER).await() }
                    ?: return retryLimited()
                if (!user.exists() || DailyReminderPolicy.completed(user.getString("last_draw_date"), date,
                        (0..2).map { user.getBoolean("card_${it}_revealed") == true })) return Result.success()
            }
            if (!canDeliver()) return Result.success()
            val (titleId, bodyId) = if (index == 0) listOf(
                R.string.notif_daily_title_1 to R.string.notif_daily_body_1,
                R.string.notif_daily_title_2 to R.string.notif_daily_body_2,
                R.string.notif_daily_title_3 to R.string.notif_daily_body_3,
                R.string.notif_daily_title_4 to R.string.notif_daily_body_4,
                R.string.notif_daily_title_5 to R.string.notif_daily_body_5
            ).random() else R.string.notif_daily_reminder_title to listOf(
                R.string.notif_daily_reminder_body_1, R.string.notif_daily_reminder_body_2,
                R.string.notif_daily_reminder_body_3, R.string.notif_daily_reminder_body_4
            )[index - 1]
            val title = context.getString(titleId)
            val message = context.getString(bodyId)
            val openApp = PendingIntent.getActivity(context, 2001,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "home")
                }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            manager.notify(2001 + index, NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_tarot)
                .setContentTitle(title).setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER).setContentIntent(openApp).build())
            prefs.edit().putString(deliveryKey, date).apply()
            val now = System.currentTimeMillis()
            withTimeoutOrNull(6_000) {
                userRef.collection("notifications").document("daily-$date-$index").set(mapOf(
                    "title" to title, "message" to message, "timestamp" to now,
                    "expiresAt" to now + TimeUnit.DAYS.toMillis(30), "isRead" to false, "type" to "DAILY_TAROT"
                )).await()
            }
            return Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return retryLimited()
        }
    }

    private fun retryLimited(): Result = if (runAttemptCount < 3) Result.retry() else Result.failure()
}
