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
        val notification = Notification(
            id = "",
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            type = type
        )
        
        return try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notification)
                .await()
            
            Log.d(TAG, "Bildirim Firestore'a kaydedildi: ${docRef.id}")
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
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("notifications")
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
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notification::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bildirimler alınamadı", e)
            emptyList()
        }
    }
    
    /**
     * Bildirimi okundu olarak işaretler
     */
    suspend fun markNotificationAsRead(userId: String, notificationId: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            
            Log.d(TAG, "Bildirim okundu işaretlendi: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Bildirim okundu işaretlenemedi", e)
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
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .whereLessThan("timestamp", thirtyDaysAgo)
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
