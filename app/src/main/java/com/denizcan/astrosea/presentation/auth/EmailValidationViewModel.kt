package com.denizcan.astrosea.presentation.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay

class EmailValidationViewModel : ViewModel() {
    
    var isEmailSent by mutableStateOf(false)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var isEmailVerified by mutableStateOf(false)
        private set
    
    var currentUser by mutableStateOf<FirebaseUser?>(null)
        private set
    
    // Geçici kullanıcı bilgileri
    private var tempEmail: String = ""
    private var tempPassword: String = ""
    
    fun setTempUserData(email: String, password: String) {
        tempEmail = email
        tempPassword = password
    }
    
    fun sendVerificationEmail() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            try {
                // Firebase'de geçici kullanıcı oluştur
                val auth = FirebaseAuth.getInstance()
                val result = auth.createUserWithEmailAndPassword(tempEmail, tempPassword).await()
                currentUser = result.user
                
                // Firebase'in kendi email verification sistemini kullan
                currentUser?.sendEmailVerification()?.await()
                isEmailSent = true
                
                Log.d("EmailValidation", "Firebase verification email sent to: $tempEmail")
                
            } catch (e: Exception) {
                errorMessage = when {
                    e.message?.contains("email") == true -> "Bu e-posta adresi zaten kullanımda"
                    else -> "Doğrulama emaili gönderilemedi: ${e.message}"
                }
                Log.e("EmailValidation", "Error sending verification email", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    fun checkEmailVerification(onVerified: () -> Unit) {
        viewModelScope.launch {
            try {
                // Kullanıcı bilgilerini yenile
                currentUser?.reload()?.await()
                val user = FirebaseAuth.getInstance().currentUser
                
                if (user?.isEmailVerified == true) {
                    // Email doğrulandı, Firestore'da kullanıcı belgesi oluştur veya güncelle
                    val userId = user.uid
                    val userDoc = FirebaseFirestore.getInstance().collection("users").document(userId)
                    
                    // Önce mevcut belgeyi kontrol et
                    val existingDoc = userDoc.get().await()
                    
                    if (existingDoc.exists()) {
                        // Mevcut kullanıcı - sadece email_verified güncelle, isPremium'a DOKUNMA!
                        userDoc.update(
                            mapOf(
                                "email_verified" to true
                            )
                        ).await()
                        Log.d("EmailValidation", "Existing user - only updated email_verified for: $tempEmail")
                    } else {
                        // Yeni kullanıcı - tüm alanları oluştur
                        userDoc.set(
                            mapOf(
                                "email" to tempEmail,
                                "created_at" to FieldValue.serverTimestamp(),
                                "auth_type" to "email",
                                "email_verified" to true,
                                "isPremium" to false,  // Yeni kullanıcı standart üye olarak başlar
                                "name" to "",
                                "surname" to "",
                                "birthDate" to "",
                                "birthTime" to "",
                                "country" to "",
                                "city" to ""
                            )
                        ).await()
                        Log.d("EmailValidation", "New user created for: $tempEmail")
                    }
                    
                    isEmailVerified = true
                    onVerified()
                    
                    Log.d("EmailValidation", "Email verified for: $tempEmail")
                } else {
                    Log.d("EmailValidation", "Email not verified yet")
                }
            } catch (e: Exception) {
                errorMessage = "Doğrulama kontrolü başarısız: ${e.message}"
                Log.e("EmailValidation", "Error checking email verification", e)
            }
        }
    }
    
    fun resendVerificationEmail() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            try {
                currentUser?.sendEmailVerification()?.await()
                isEmailSent = true
                
                Log.d("EmailValidation", "Verification email resent for: $tempEmail")
                
            } catch (e: Exception) {
                errorMessage = "Doğrulama emaili yeniden gönderilemedi: ${e.message}"
                Log.e("EmailValidation", "Error resending verification email", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    fun startPeriodicVerificationCheck(onVerified: () -> Unit) {
        viewModelScope.launch {
            while (!isEmailVerified) {
                delay(3000) // 3 saniyede bir kontrol et
                checkEmailVerification(onVerified)
            }
        }
    }
    
    fun clearError() {
        errorMessage = null
    }
    
    // Test amaçlı manuel doğrulama fonksiyonu (geliştirme aşamasında)
    fun manuallyVerifyEmail(onVerified: () -> Unit) {
        viewModelScope.launch {
            try {
                // Firebase'de email'i manuel olarak doğrula (sadece test için)
                // Gerçek uygulamada bu fonksiyon kaldırılmalı
                Log.d("EmailValidation", "Manual verification triggered for: $tempEmail")
                
                // Test amaçlı olarak doğrulama kontrolü yap
                checkEmailVerification(onVerified)
                
            } catch (e: Exception) {
                errorMessage = "Manuel doğrulama başarısız: ${e.message}"
                Log.e("EmailValidation", "Error manually verifying email", e)
            }
        }
    }
}
