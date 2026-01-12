package com.denizcan.astrosea.presentation.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileViewModel : ViewModel() {
    var profileState by mutableStateOf(ProfileState())
        private set

    private val auth = FirebaseAuth.getInstance()
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var profileListener: ListenerRegistration? = null

    init {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d("ProfileViewModel", "Auth state changed: User ${user.uid} logged in")
                loadProfile(user.uid)
                startListeningToProfileChanges(user.uid)
            } else {
                Log.d("ProfileViewModel", "Auth state changed: User logged out")
                profileState = ProfileState()
                stopListeningToProfileChanges()
            }
        }
        
        auth.addAuthStateListener(authStateListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        authStateListener?.let { auth.removeAuthStateListener(it) }
        stopListeningToProfileChanges()
    }

    private fun loadProfile(userId: String) {
        profileState = profileState.copy(isLoading = true)
        Log.d("ProfileViewModel", "Loading profile for userId: $userId")
        
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                // Raw data'yı logla (debug için)
                val rawData = document.data
                Log.d("ProfileViewModel", "Raw Firestore data: $rawData")
                Log.d("ProfileViewModel", "Raw isPremium value: ${rawData?.get("isPremium")}")
                
                // Manuel parse - Timestamp ve String formatlarını destekle
                val profileData = parseProfileData(rawData)
                profileState = profileState.copy(
                    profileData = profileData,
                    isLoading = false
                )
                Log.d("ProfileViewModel", "Profile loaded: $profileData")
                Log.d("ProfileViewModel", "isPremium after parse: ${profileData.isPremium}")
            }
            .addOnFailureListener { e ->
                profileState = profileState.copy(
                    error = e.message,
                    isLoading = false
                )
                Log.e("ProfileViewModel", "Error loading profile", e)
            }
    }

    fun startEditing() {
        profileState = profileState.copy(isEditing = true)
    }

    fun stopEditing() {
        profileState = profileState.copy(isEditing = false)
    }

    fun onNameChange(newName: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(name = newName)
        )
    }

    fun onSurnameChange(newSurname: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(surname = newSurname)
        )
    }

    fun onBirthDateChange(newBirthDate: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(birthDate = newBirthDate)
        )
    }

    fun onBirthTimeChange(newBirthTime: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(birthTime = newBirthTime)
        )
    }

    fun onCountryChange(newCountry: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(country = newCountry)
        )
    }

    fun onCityChange(newCity: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(city = newCity)
        )
    }

    fun toggleEditing() {
        profileState = profileState.copy(isEditing = !profileState.isEditing)
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        profileState = profileState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // SADECE profil bilgilerini güncelle, isPremium ve premium alanlarına DOKUNMA!
                val profileOnlyData = mapOf(
                    "name" to profileState.profileData.name,
                    "surname" to profileState.profileData.surname,
                    "birthDate" to profileState.profileData.birthDate,
                    "birthTime" to profileState.profileData.birthTime,
                    "country" to profileState.profileData.country,
                    "city" to profileState.profileData.city
                    // isPremium, premiumStartDate, premiumEndDate EKLEME!
                    // Günlük kart bilgileri de ayrı yönetilmeli
                )
                
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .set(profileOnlyData, SetOptions.merge())
                    .await()
                loadProfile(userId)
                onSuccess()
            } catch (e: Exception) {
                profileState = profileState.copy(
                    error = e.localizedMessage,
                    isLoading = false
                )
                Log.e("ProfileViewModel", "Error saving profile", e)
            }
        }
    }

    fun startListeningToProfileChanges(userId: String) {
        stopListeningToProfileChanges()
        
        profileListener = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ProfileViewModel", "Error listening to profile changes", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // Raw data'yı logla (debug için)
                    val rawData = snapshot.data
                    Log.d("ProfileViewModel", "Raw Firestore data: $rawData")
                    Log.d("ProfileViewModel", "Raw isPremium value: ${rawData?.get("isPremium")}")
                    
                    // Manuel parse - Timestamp ve String formatlarını destekle
                    val profileData = parseProfileData(rawData)
                    profileState = profileState.copy(
                        profileData = profileData,
                        isLoading = false
                    )
                    Log.d("ProfileViewModel", "Profile updated: $profileData")
                    Log.d("ProfileViewModel", "isPremium after parse: ${profileData.isPremium}")
                }
            }
    }

    private fun stopListeningToProfileChanges() {
        profileListener?.remove()
        profileListener = null
    }
    
    /**
     * Firestore verisini manuel olarak parse eder
     * Timestamp ve String formatlarını destekler
     */
    private fun parseProfileData(data: Map<String, Any>?): ProfileData {
        if (data == null) return ProfileData()
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        
        // Timestamp veya String'i String'e çeviren yardımcı fonksiyon
        fun parseDate(value: Any?): String? {
            return when (value) {
                is Timestamp -> dateFormat.format(value.toDate())
                is String -> value
                else -> null
            }
        }
        
        return ProfileData(
            name = data["name"] as? String ?: "",
            surname = data["surname"] as? String ?: "",
            birthDate = data["birthDate"] as? String ?: "",
            birthTime = data["birthTime"] as? String ?: "",
            country = data["country"] as? String ?: "",
            city = data["city"] as? String ?: "",
            isPremium = data["isPremium"] as? Boolean ?: false,
            premiumStartDate = parseDate(data["premiumStartDate"]),
            premiumEndDate = parseDate(data["premiumEndDate"]),
            premiumProductId = data["premiumProductId"] as? String,
            card_0_id = data["card_0_id"] as? String,
            card_0_revealed = data["card_0_revealed"] as? Boolean,
            card_1_id = data["card_1_id"] as? String,
            card_1_revealed = data["card_1_revealed"] as? Boolean,
            card_2_id = data["card_2_id"] as? String,
            card_2_revealed = data["card_2_revealed"] as? Boolean,
            last_draw_date = data["last_draw_date"] as? String
        )
    }

    // ==================== PREMIUM ÜYELİK FONKSİYONLARI ====================

    /**
     * Kullanıcının premium üye olup olmadığını döndürür (cache'den)
     */
    val isPremium: Boolean
        get() = profileState.profileData.isPremium

    /**
     * Premium durumunu kontrol eder ve boolean döndürür (cache'den)
     */
    fun checkPremiumStatus(): Boolean {
        return profileState.profileData.isPremium
    }

    /**
     * Firestore'dan ANLIK olarak premium durumunu kontrol eder
     * Her kritik işlemde (buton tıklama, ekran açılma) bu fonksiyon kullanılmalı
     */
    suspend fun checkPremiumStatusFromFirestore(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        
        return try {
            val document = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()
            
            val isPremiumValue = document.getBoolean("isPremium") ?: false
            Log.d("ProfileViewModel", "Firestore'dan anlık premium kontrolü: $isPremiumValue")
            
            // Local state'i de güncelle
            if (isPremiumValue != profileState.profileData.isPremium) {
                profileState = profileState.copy(
                    profileData = profileState.profileData.copy(isPremium = isPremiumValue)
                )
                Log.d("ProfileViewModel", "Local state güncellendi: isPremium = $isPremiumValue")
            }
            
            isPremiumValue
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Firestore'dan premium kontrolü hatası", e)
            false
        }
    }

    /**
     * Kullanıcıyı premium üye yapar (ödeme başarılı olduktan sonra çağrılacak)
     * @param startDate Premium başlangıç tarihi
     * @param endDate Premium bitiş tarihi (null = süresiz)
     */
    fun upgradeToPremium(startDate: String, endDate: String? = null, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "isPremium" to true,
                            "premiumStartDate" to startDate,
                            "premiumEndDate" to endDate
                        )
                    )
                    .await()
                
                Log.d("ProfileViewModel", "User upgraded to premium: $userId")
                onSuccess()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error upgrading to premium", e)
                onError(e.message ?: "Premium yükseltme hatası")
            }
        }
    }

    /**
     * Kullanıcının premium üyeliğini iptal eder
     */
    fun cancelPremium(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "isPremium" to false,
                            "premiumEndDate" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                        )
                    )
                    .await()
                
                Log.d("ProfileViewModel", "User premium cancelled: $userId")
                onSuccess()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error cancelling premium", e)
                onError(e.message ?: "Premium iptal hatası")
            }
        }
    }
}

data class ProfileState(
    val profileData: ProfileData = ProfileData(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false
) 