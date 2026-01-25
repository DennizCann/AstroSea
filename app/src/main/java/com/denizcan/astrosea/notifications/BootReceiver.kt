package com.denizcan.astrosea.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Telefon yeniden başlatıldığında alarmları tekrar kurar.
 * Bu sayede telefon kapatılıp açılsa bile bildirimler çalışmaya devam eder.
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Cihaz yeniden başlatıldı, alarmlar kontrol ediliyor...")
            
            // Günlük bildirim alarmını tekrar kur
            if (DailyNotificationScheduler.isAlarmEnabled(context)) {
                DailyNotificationScheduler.scheduleDailyNotification(context)
                Log.d(TAG, "Günlük bildirim alarmı yeniden kuruldu")
            }
            
            // Premium hatırlatmaları kontrol et ve tekrar kur
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        val isPremium = checkIsPremium(userId)
                        if (!isPremium) {
                            // Premium değilse haftalık hatırlatmayı kur
                            val reminderCount = PremiumReminderScheduler.getReminderCount(context)
                            if (reminderCount > 0 && reminderCount < 10) {
                                PremiumReminderScheduler.scheduleWeeklyReminder(context)
                                Log.d(TAG, "Premium haftalık hatırlatma yeniden kuruldu")
                            }
                        } else {
                            // Premium olduysa hatırlatmaları iptal et
                            PremiumReminderScheduler.cancelAllReminders(context)
                            Log.d(TAG, "Kullanıcı premium, hatırlatmalar iptal edildi")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Premium kontrol hatası", e)
                }
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
}
