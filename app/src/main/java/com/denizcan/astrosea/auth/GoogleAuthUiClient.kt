package com.denizcan.astrosea.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.denizcan.astrosea.presentation.notifications.NotificationManager

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signIn(): IntentSender? {
        try {
            // Mevcut oturumları temizle
            oneTapClient.signOut().await()
            auth.signOut()
            
            // Google Play Services'taki tüm hesapları temizle
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("675910266017-51es5ap1lrnum18onkhpb0eh5tl8ocpj.apps.googleusercontent.com")
                    .requestEmail()
                    .build()
            ).signOut().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            null
        }
        
        // Sadece IntentSender döndürüyoruz, şu anda kullanıcı bilgilerine erişim yok
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            
            // Kullanıcı giriş yaptığında Firestore'a bilgileri kaydet
            user?.let { firebaseUser ->
                val firestore = FirebaseFirestore.getInstance()
                val userRef = firestore.collection("users").document(firebaseUser.uid)
                
                // Mevcut kullanıcı bilgilerini kontrol et
                val userDoc = userRef.get().await()
                val isFirstTime = !userDoc.exists() || userDoc.getLong("login_count") == null
                
                android.util.Log.d("GoogleAuth", "User exists: ${userDoc.exists()}, isFirstTime: $isFirstTime")
                
                // Kullanıcı bilgilerini güncelle
                val updateData = mutableMapOf<String, Any>(
                    "email" to (firebaseUser.email ?: ""),
                    "displayName" to (firebaseUser.displayName ?: ""),
                    "photoUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
                    "auth_type" to "google",
                    "last_login" to FieldValue.serverTimestamp()
                )
                
                if (isFirstTime) {
                    // İlk kez giriş yapıyorsa login_count'u 1 yap
                    updateData["login_count"] = 1L
                    android.util.Log.d("GoogleAuth", "First time user, setting login_count to 1")
                } else {
                    // Daha önce giriş yapmışsa login_count'u artır
                    val currentCount = userDoc.getLong("login_count") ?: 0L
                    updateData["login_count"] = currentCount + 1L
                    android.util.Log.d("GoogleAuth", "Returning user, login_count: $currentCount -> ${currentCount + 1}")
                }
                
                userRef.set(updateData, SetOptions.merge()).await()
                android.util.Log.d("GoogleAuth", "User data saved to Firestore")
                
                // İlk kez kullanıcı ise bildirim gönder
                if (isFirstTime) {
                    android.util.Log.d("GoogleAuth", "Sending first daily reading notification")
                    try {
                        val notificationManager = NotificationManager(context)
                        notificationManager.sendFirstDailyReadingNotification(firebaseUser.uid)
                        android.util.Log.d("GoogleAuth", "First daily reading notification sent successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("GoogleAuth", "Error sending first daily reading notification", e)
                        e.printStackTrace()
                    }
                } else {
                    android.util.Log.d("GoogleAuth", "Not a first time user, skipping notification")
                }
            }
            
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        email = this.email
                    )
                },
                errorMessage = null
            )
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("675910266017-51es5ap1lrnum18onkhpb0eh5tl8ocpj.apps.googleusercontent.com")
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }
}

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val email: String?
) 