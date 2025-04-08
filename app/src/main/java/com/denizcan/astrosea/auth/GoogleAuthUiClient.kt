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
                FirebaseFirestore.getInstance().collection("users")
                    .document(firebaseUser.uid)
                    .set(
                        mapOf(
                            "email" to firebaseUser.email,
                            "displayName" to firebaseUser.displayName,
                            "photoUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
                            "auth_type" to "google",
                            "last_login" to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()
                    )
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