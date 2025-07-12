package com.denizcan.astrosea.presentation.notifications

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class NotificationManager(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Yeni kullanıcı için ilk günlük açılım bildirimi gönderir
     */
    suspend fun sendFirstDailyReadingNotification(userId: String) {
        val notification = Notification(
            id = "",
            title = "İlk Günlük Açılımınızı Yapın",
            message = "Hoş geldiniz! İlk günlük tarot açılımınızı yaparak gününüzün enerjilerini keşfedin.",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            type = NotificationType.DAILY_TAROT
        )
        
        try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notification)
                .await()
            
            android.util.Log.d("NotificationManager", "First daily reading notification sent successfully: ${docRef.id}")
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Error sending first daily reading notification", e)
            e.printStackTrace()
        }
    }
    
    /**
     * Günlük kartlar yenilendiğinde bildirim gönderir
     */
    suspend fun sendDailyCardsRenewedNotification(userId: String, cardNames: List<String>) {
        val notification = Notification(
            id = "",
            title = "Günlük Açılım Kartlarınız Yenilendi",
            message = "Bugün için ${cardNames.joinToString(", ")} kartları çekildi. Kartlarınızı açarak günlük yorumunuzu okuyabilirsiniz.",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            type = NotificationType.DAILY_TAROT
        )
        
        try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notification)
                .await()
            
            android.util.Log.d("NotificationManager", "Daily cards renewed notification sent successfully: ${docRef.id}")
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Error sending daily cards renewed notification", e)
            e.printStackTrace()
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
            e.printStackTrace()
            0
        }
    }
    
    /**
     * Tüm bildirimleri getirir
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
            e.printStackTrace()
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Kullanıcının ilk giriş yapıp yapmadığını kontrol eder
     */
    suspend fun isFirstTimeUser(userId: String): Boolean {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            // Kullanıcı dokümanı var mı ve daha önce giriş yapmış mı kontrol et
            val hasLoggedInBefore = userDoc.exists() && userDoc.getLong("login_count") != null
            !hasLoggedInBefore
        } catch (e: Exception) {
            e.printStackTrace()
            true // Hata durumunda ilk kez kullanıcı olarak kabul et
        }
    }
    
    /**
     * Günlük kartların yenilenip yenilenmediğini kontrol eder
     */
    suspend fun shouldRenewDailyCards(userId: String): Boolean {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val lastDrawDate = userDoc.getString("last_draw_date") ?: ""
            val currentDate = getCurrentDateString()
            
            lastDrawDate != currentDate
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }
    
    /**
     * Açılmamış günlük kartlar için hatırlatma bildirimi gönderir
     */
    suspend fun sendUnopenedCardsReminder(userId: String) {
        try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val lastDrawDate = userDoc.getString("last_draw_date") ?: ""
            val currentDate = getCurrentDateString()
            
            // Bugün kartlar çekilmiş mi kontrol et
            if (lastDrawDate == currentDate) {
                // Açılmamış kart sayısını kontrol et
                val unopenedCount = (0..2).count { index ->
                    !(userDoc.getBoolean("card_${index}_revealed") ?: false)
                }
                
                if (unopenedCount > 0) {
                    val notification = Notification(
                        id = "",
                        title = "Günlük Açılımınızı Tamamlayın",
                        message = "Bugün çektiğiniz ${unopenedCount} kart henüz açılmadı. Günlük yorumunuzu okumak için kartlarınızı açın.",
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        type = NotificationType.DAILY_TAROT
                    )
                    
                    val docRef = firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .add(notification)
                        .await()
                    
                    android.util.Log.d("NotificationManager", "Unopened cards reminder sent: $unopenedCount cards, notification ID: ${docRef.id}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Error sending unopened cards reminder", e)
            e.printStackTrace()
        }
    }
    
    /**
     * Kullanıcının açılmamış kartları olup olmadığını kontrol eder
     */
    suspend fun hasUnopenedCards(userId: String): Boolean {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val lastDrawDate = userDoc.getString("last_draw_date") ?: ""
            val currentDate = getCurrentDateString()
            
            // Bugün kartlar çekilmiş mi ve açılmamış kart var mı kontrol et
            if (lastDrawDate == currentDate) {
                val unopenedCount = (0..2).count { index ->
                    !(userDoc.getBoolean("card_${index}_revealed") ?: false)
                }
                unopenedCount > 0
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
} 