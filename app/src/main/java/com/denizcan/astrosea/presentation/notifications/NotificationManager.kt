package com.denizcan.astrosea.presentation.notifications

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Firestore'daki bildirimleri yöneten sınıf.
 * Push notification gönderimi artık DailyNotificationReceiver tarafından yapılıyor.
 */
class NotificationManager(private val context: Context) {
    
    private val firestore = FirebaseFirestore.getInstance()
    
    companion object {
        private const val TAG = "NotificationManager"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
        private const val DISPLAY_WINDOW_DAYS = 7L
        private const val RETENTION_DAYS = 30L
        private const val MAX_NOTIFICATIONS = 50L
    }
    
    // ==================== FIRESTORE İŞLEMLERİ ====================
    
    /**
     * Firestore'a bildirim kaydeder (uygulama içi bildirim geçmişi için)
     */
    suspend fun saveNotificationToFirestore(
        userId: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.DAILY_TAROT
    ): String? {
        val now = System.currentTimeMillis()
        // Firestore'a kaydedilecek veri - HashMap olarak
        val notificationData = hashMapOf(
            "title" to title,
            "message" to message,
            "timestamp" to now,
            "expiresAt" to now + daysToMillis(RETENTION_DAYS),
            "isRead" to false,
            "type" to type.name  // Enum'u String olarak kaydet
        )
        
        return try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notificationData)
                .await()

            // Veri tabanini sisme riskine karsi, kayit sirasinda eski bildirimleri temizle.
            deleteOldNotifications(userId)
            
            Log.d(TAG, "Bildirim Firestore'a kaydedildi: ${docRef.id}, type: ${type.name}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Bildirim kaydetme hatası", e)
            null
        }
    }
    
    /**
     * Kullanıcının okunmamış bildirim sayısını getirir
     */
    suspend fun getUnreadNotificationCount(userId: String): Int {
        return try {
            val visibleWindowStart = System.currentTimeMillis() - daysToMillis(DISPLAY_WINDOW_DAYS)
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .whereGreaterThanOrEqualTo("timestamp", visibleWindowStart)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            snapshot.size()
        } catch (e: Exception) {
            Log.e(TAG, "Okunmamış bildirim sayısı alınamadı", e)
            0
        }
    }
    
    /**
     * Tüm bildirimleri getirir (en yeniden en eskiye)
     */
    suspend fun getAllNotifications(userId: String): List<Notification> {
        return try {
            Log.d(TAG, "Bildirimler yükleniyor... userId: $userId")
            val visibleWindowStart = System.currentTimeMillis() - daysToMillis(DISPLAY_WINDOW_DAYS)
            
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .whereGreaterThanOrEqualTo("timestamp", visibleWindowStart)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MAX_NOTIFICATIONS)
                .get()
                .await()
            
            Log.d(TAG, "Firestore'dan ${snapshot.documents.size} bildirim dökümanı alındı")
            
            val notifications = snapshot.documents.mapNotNull { doc ->
                try {
                    // Manuel olarak Notification oluştur (daha güvenilir)
                    val data = doc.data ?: return@mapNotNull null
                    
                    val notification = Notification(
                        id = doc.id,  // Firestore document ID
                        title = data["title"] as? String ?: "",
                        message = data["message"] as? String ?: "",
                        timestamp = (data["timestamp"] as? Long) ?: 0L,
                        isRead = data["isRead"] as? Boolean ?: false,
                        type = data["type"] as? String ?: NotificationType.GENERAL.name
                    )
                    
                    Log.d(TAG, "Bildirim parse edildi: id=${notification.id}, title=${notification.title}, isRead=${notification.isRead}")
                    notification
                } catch (e: Exception) {
                    Log.e(TAG, "Bildirim parse hatası: ${doc.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Toplam ${notifications.size} bildirim yüklendi")
            notifications
        } catch (e: Exception) {
            Log.e(TAG, "Bildirimler alınamadı", e)
            emptyList()
        }
    }
    
    /**
     * Bildirimi okundu olarak işaretler
     */
    suspend fun markNotificationAsRead(userId: String, notificationId: String) {
        if (notificationId.isEmpty()) {
            Log.e(TAG, "Bildirim ID boş, güncelleme yapılamadı!")
            return
        }
        
        try {
            Log.d(TAG, "Bildirim okundu işaretleniyor... userId: $userId, notificationId: $notificationId")
            
            firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            
            Log.d(TAG, "Bildirim başarıyla okundu işaretlendi: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Bildirim okundu işaretlenemedi: $notificationId", e)
            throw e  // Hatayı yukarı fırlat ki UI'da yakalansın
        }
    }
    
    /**
     * Tüm bildirimleri okundu olarak işaretler
     */
    suspend fun markAllNotificationsAsRead(userId: String) {
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            
            Log.d(TAG, "Tüm bildirimler okundu işaretlendi: ${snapshot.size()} bildirim")
        } catch (e: Exception) {
            Log.e(TAG, "Bildirimler okundu işaretlenemedi", e)
        }
    }
    
    /**
     * Eski bildirimleri siler (30 günden eski)
     */
    suspend fun deleteOldNotifications(userId: String) {
        try {
            val retentionStart = System.currentTimeMillis() - daysToMillis(RETENTION_DAYS)
            
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .whereLessThan("timestamp", retentionStart)
                .get()
                .await()
            
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            
            Log.d(TAG, "Eski bildirimler silindi: ${snapshot.size()} bildirim")
        } catch (e: Exception) {
            Log.e(TAG, "Eski bildirimler silinemedi", e)
        }
    }

    private fun daysToMillis(days: Long): Long {
        return days * 24 * 60 * 60 * 1000
    }
    
    // ==================== İZİN KONTROLLERİ ====================
    
    /**
     * Bildirim izinlerini kontrol eder
     */
    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13'ten önceki sürümlerde otomatik olarak izin verilir
        }
    }
    
    /**
     * Bildirim izinlerini ister
     */
    fun requestNotificationPermission(activity: android.app.Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}
