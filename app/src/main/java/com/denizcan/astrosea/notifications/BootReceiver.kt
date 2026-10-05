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
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Telefon yeniden başlatıldığında alarmları tekrar kurar.
 * Bu sayede telefon kapatılıp açılsa bile bildirimler çalışmaya devam eder.
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) {
            Log.d(TAG, "Cihaz yeniden başlatıldı, alarmlar kontrol ediliyor...")
            
            // Günlük bildirim alarmını tekrar kur
            if (FirebaseAuth.getInstance().currentUser != null &&
                DailyNotificationScheduler.areDailyNotificationsEnabled(context)) {
                DailyNotificationScheduler.scheduleDailyNotification(context)
                Log.d(TAG, "Günlük bildirim alarmı yeniden kuruldu")
            }
            
            // Premium hatırlatmaları kontrol et ve tekrar kur
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        val isPremium = withTimeoutOrNull(8_000) { checkIsPremium(userId) } ?: return@launch
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
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
    
    private suspend fun checkIsPremium(userId: String): Boolean {
        return try {
            if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid != userId) return true
            com.denizcan.astrosea.billing.MembershipRepository.refresh().hasAccess
        } catch (e: Exception) {
            Log.e(TAG, "Premium kontrol hatası", e)
            true
        }
    }
}
