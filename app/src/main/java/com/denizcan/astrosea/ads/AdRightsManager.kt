package com.denizcan.astrosea.ads

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * Ücretsiz kullanıcının günlük reklam izleme haklarını Firestore'da takip eder.
 * Kullanıcı dokümanında: ad_unlock_date (yyyy-MM-dd), ad_unlock_count (int).
 */
class AdRightsManager {

    companion object {
        private const val TAG = "AdRightsManager"
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    /** Bugün kalan reklam hakkı sayısı */
    suspend fun getRemainingUnlocks(): Int {
        val uid = userId ?: return 0
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val savedDate = doc.getString("ad_unlock_date")
            val count = if (savedDate == today()) (doc.getLong("ad_unlock_count") ?: 0L).toInt() else 0
            (AdConfig.DAILY_AD_UNLOCK_LIMIT - count).coerceAtLeast(0)
        } catch (e: Exception) {
            Log.e(TAG, "Reklam hakkı okunamadı", e)
            0
        }
    }

    /**
     * Bir reklam hakkı kullanır. Hak kalmadıysa false döner.
     * Reklam başarıyla izlendikten SONRA çağrılmalıdır.
     */
    suspend fun consumeUnlock(): Boolean {
        val uid = userId ?: return false
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val savedDate = doc.getString("ad_unlock_date")
            val currentCount = if (savedDate == today()) (doc.getLong("ad_unlock_count") ?: 0L).toInt() else 0

            if (currentCount >= AdConfig.DAILY_AD_UNLOCK_LIMIT) {
                Log.w(TAG, "Günlük reklam hakkı dolu: $currentCount")
                return false
            }

            firestore.collection("users").document(uid)
                .set(
                    mapOf(
                        "ad_unlock_date" to today(),
                        "ad_unlock_count" to currentCount + 1
                    ),
                    SetOptions.merge()
                ).await()

            Log.d(TAG, "Reklam hakkı kullanıldı: ${currentCount + 1}/${AdConfig.DAILY_AD_UNLOCK_LIMIT}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Reklam hakkı kullanılamadı", e)
            false
        }
    }
}
