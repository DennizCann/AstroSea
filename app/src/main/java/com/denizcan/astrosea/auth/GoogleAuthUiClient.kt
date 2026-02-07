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
import com.google.android.gms.common.api.ApiException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.denizcan.astrosea.presentation.notifications.NotificationManager

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = FirebaseAuth.getInstance()

    init {
        logFirebaseOptions()
    }

    private fun logFirebaseOptions() {
        try {
            val opts = com.google.firebase.FirebaseApp.getInstance().options
            android.util.Log.d(
                "FB",
                "projectId=${opts.projectId}, storage=${opts.storageBucket}, apiKey=${opts.apiKey}, appId=${opts.applicationId}, dbUrl=${opts.databaseUrl}"
            )
        } catch (e: Exception) {
            android.util.Log.e("FB", "Failed to log Firebase options", e)
        }
    }

    suspend fun signIn(): IntentSender? {
        try {
            // Mevcut oturumları temizle
            oneTapClient.signOut().await()
            auth.signOut()
            
            // Google Play Services'taki tüm hesapları temizle
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(com.denizcan.astrosea.R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            ).signOut().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        android.util.Log.d("GoogleAuth", "One Tap: beginSignIn...")
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            android.util.Log.w("GoogleAuth", "One Tap: beginSignIn failed: ${e.message}")
            null
        }
        
        // Sadece IntentSender döndürüyoruz, şu anda kullanıcı bilgilerine erişim yok
        val sender = result?.pendingIntent?.intentSender
        if (sender == null) {
            android.util.Log.w("GoogleAuth", "One Tap: no intent sender, will need fallback")
        } else {
            android.util.Log.d("GoogleAuth", "One Tap: intent sender ready")
        }
        return sender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        // One Tap dene; başarısızsa GoogleSignIn fallback'ı işle
        var googleIdToken: String? = null
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            googleIdToken = credential.googleIdToken
            android.util.Log.d("GoogleAuth", "One Tap: credential received, token null? ${googleIdToken==null}")
        } catch (e: Exception) {
            // yoksay, fallback denenecek
            android.util.Log.w("GoogleAuth", "One Tap: no credential from intent, trying GoogleSignIn: ${e.message}")
        }

        if (googleIdToken == null) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(intent)
                    .getResult(ApiException::class.java)
                googleIdToken = account.idToken
                android.util.Log.d("GoogleAuth", "GoogleSignIn: account received, token null? ${googleIdToken==null}")
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("GoogleAuth", "GoogleSignIn: failed to extract account: ${e.message}")
            }
        }

        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            
            var isFirstTimeUser = false
            var kvkkAlreadyAccepted = false
            
            // Kullanıcı giriş yaptığında Firestore'a bilgileri kaydet
            user?.let { firebaseUser ->
                val firestore = FirebaseFirestore.getInstance()
                val userRef = firestore.collection("users").document(firebaseUser.uid)
                
                // Mevcut kullanıcı bilgilerini kontrol et
                val userDoc = userRef.get().await()
                // Belge yoksa ilk kez, varsa mevcut kullanıcı (login_count'a bakma, premium kullanıcıları ezer!)
                isFirstTimeUser = !userDoc.exists()
                kvkkAlreadyAccepted = userDoc.getBoolean("kvkk_accepted") ?: false
                
                android.util.Log.d("GoogleAuth", "User exists: ${userDoc.exists()}, isFirstTime: $isFirstTimeUser, kvkkAccepted: $kvkkAlreadyAccepted")
                
                // Kullanıcı bilgilerini güncelle
                val updateData = mutableMapOf<String, Any>(
                    "email" to (firebaseUser.email ?: ""),
                    "displayName" to (firebaseUser.displayName ?: ""),
                    "photoUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
                    "auth_type" to "google",
                    "last_login" to FieldValue.serverTimestamp()
                )
                
                if (isFirstTimeUser) {
                    // İlk kez giriş yapıyorsa login_count'u 1 yap ve standart üye olarak başlat
                    updateData["login_count"] = 1L
                    updateData["isPremium"] = false  // SADECE YENİ kullanıcı standart üye olarak başlar
                    updateData["name"] = ""
                    updateData["surname"] = ""
                    updateData["birthDate"] = ""
                    updateData["birthTime"] = ""
                    updateData["country"] = ""
                    updateData["city"] = ""
                    updateData["kvkk_accepted"] = false  // KVKK henüz kabul edilmedi
                    android.util.Log.d("GoogleAuth", "First time user, setting login_count to 1, isPremium to false")
                } else {
                    // Daha önce giriş yapmışsa login_count'u artır, isPREMIUM'A DOKUNMA!
                    val currentCount = userDoc.getLong("login_count") ?: 0L
                    updateData["login_count"] = currentCount + 1L
                    // Mevcut isPremium değerini koru - updateData'ya ekleme!
                    val currentPremium = userDoc.getBoolean("isPremium") ?: false
                    android.util.Log.d("GoogleAuth", "Returning user, login_count: $currentCount -> ${currentCount + 1}, isPremium preserved: $currentPremium")
                }
                
                userRef.set(updateData, SetOptions.merge()).await()
                android.util.Log.d("GoogleAuth", "User data saved to Firestore")
                
                // İlk kez kullanıcı ise Firestore'a hoşgeldin bildirimi kaydet
                if (isFirstTimeUser) {
                    android.util.Log.d("GoogleAuth", "Saving welcome notification for first time user")
                    try {
                        val notificationManager = NotificationManager(context)
                        notificationManager.saveNotificationToFirestore(
                            userId = firebaseUser.uid,
                            title = "Hoş Geldiniz! ✨",
                            message = "AstroSea'ye hoş geldiniz! İlk günlük tarot açılımınızı yaparak gününüzün enerjilerini keşfedin."
                        )
                        android.util.Log.d("GoogleAuth", "Welcome notification saved successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("GoogleAuth", "Error saving welcome notification", e)
                    }
                } else {
                    android.util.Log.d("GoogleAuth", "Not a first time user, skipping welcome notification")
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
                errorMessage = null,
                isFirstTime = isFirstTimeUser,
                kvkkAccepted = kvkkAlreadyAccepted
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
    
    /**
     * KVKK onayını Firestore'a kaydet
     */
    suspend fun saveKvkkConsent(userId: String) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val userRef = firestore.collection("users").document(userId)
            userRef.update(
                mapOf(
                    "kvkk_accepted" to true,
                    "kvkk_accepted_at" to FieldValue.serverTimestamp()
                )
            ).await()
            android.util.Log.d("GoogleAuth", "KVKK consent saved for user: $userId")
        } catch (e: Exception) {
            android.util.Log.e("GoogleAuth", "Error saving KVKK consent", e)
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
                    .setServerClientId(context.getString(com.denizcan.astrosea.R.string.default_web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }

    fun getFallbackSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.denizcan.astrosea.R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }
}

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?,
    val isFirstTime: Boolean = false,
    val kvkkAccepted: Boolean = false
)

data class UserData(
    val userId: String,
    val username: String?,
    val email: String?
) 