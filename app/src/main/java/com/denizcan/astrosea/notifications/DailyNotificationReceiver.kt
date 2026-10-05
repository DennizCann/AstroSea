package com.denizcan.astrosea.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth

/** Keep the receiver short; network checks belong to persistent background work. */
class DailyNotificationReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_REMINDER_INDEX = "daily_reminder_index"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val index = intent?.getIntExtra(EXTRA_REMINDER_INDEX, -1) ?: return
        if (index !in DailyReminderPolicy.hours.indices ||
            !DailyNotificationScheduler.areDailyNotificationsEnabled(context)) return
        DailyNotificationScheduler.scheduleNextOccurrence(context, index)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        DailyNotificationWorker.enqueue(context, uid, index)
    }
}
