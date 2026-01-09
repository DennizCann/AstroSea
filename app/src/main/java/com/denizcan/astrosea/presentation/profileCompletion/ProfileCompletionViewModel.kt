package com.denizcan.astrosea.presentation.profileCompletion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.astrosea.presentation.profile.ProfileData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileCompletionViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName

    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName

    private val _birthDate = MutableStateFlow("")
    val birthDate: StateFlow<String> = _birthDate

    private val _birthTime = MutableStateFlow("")
    val birthTime: StateFlow<String> = _birthTime

    private val _birthCountry = MutableStateFlow("")
    val birthCountry: StateFlow<String> = _birthCountry

    private val _birthCity = MutableStateFlow("")
    val birthCity: StateFlow<String> = _birthCity

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun updateFirstName(value: String) {
        _firstName.value = value
    }

    fun updateLastName(value: String) {
        _lastName.value = value
    }

    fun updateBirthDate(value: String) {
        _birthDate.value = value
    }

    fun updateBirthTime(value: String) {
        _birthTime.value = value
    }

    fun updateBirthCountry(value: String) {
        _birthCountry.value = value
    }

    fun updateBirthCity(value: String) {
        _birthCity.value = value
    }

    suspend fun saveNameData(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        _isLoading.value = true
        return try {
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "name" to _firstName.value.trim(),
                        "surname" to _lastName.value.trim()
                    )
                ).await()
            _isLoading.value = false
            true
        } catch (e: Exception) {
            _isLoading.value = false
            false
        }
    }

    suspend fun saveBirthData(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        _isLoading.value = true
        return try {
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "birthDate" to _birthDate.value.trim(),
                        "birthTime" to _birthTime.value.trim()
                    )
                ).await()
            _isLoading.value = false
            true
        } catch (e: Exception) {
            _isLoading.value = false
            false
        }
    }

    suspend fun saveLocationData(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        _isLoading.value = true
        return try {
            firestore.collection("users").document(userId)
                .update(
                    mapOf(
                        "country" to _birthCountry.value.trim(),
                        "city" to _birthCity.value.trim()
                    )
                ).await()
            _isLoading.value = false
            true
        } catch (e: Exception) {
            _isLoading.value = false
            false
        }
    }

    suspend fun checkProfileCompletion(): ProfileCompletionStatus {
        val userId = auth.currentUser?.uid ?: return ProfileCompletionStatus.NOT_LOGGED_IN
        
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            
            if (!doc.exists()) {
                // Kullanıcı belgesi yoksa, SADECE profil alanlarını oluştur
                // isPremium gibi kritik alanları EKLEME - auth sırasında zaten ayarlanmış olmalı
                val initialProfileData = mapOf(
                    "name" to "",
                    "surname" to "",
                    "birthDate" to "",
                    "birthTime" to "",
                    "country" to "",
                    "city" to ""
                    // isPremium EKLEME! - eğer belge yoksa ve premium gerekiyorsa, bu ayrı yönetilmeli
                )
                firestore.collection("users").document(userId).set(initialProfileData, SetOptions.merge()).await()
                return ProfileCompletionStatus.INCOMPLETE_NAME
            }
            
            // Manuel parse - Timestamp ve String formatlarını destekle
            val profile = parseProfileData(doc.data) ?: return ProfileCompletionStatus.INCOMPLETE_NAME
            
            // Adım adım kontrol et
            if (profile.name.isEmpty() || profile.surname.isEmpty()) {
                return ProfileCompletionStatus.INCOMPLETE_NAME
            }
            
            if (profile.birthDate.isEmpty() || profile.birthTime.isEmpty()) {
                return ProfileCompletionStatus.INCOMPLETE_BIRTH
            }
            
            if (profile.country.isEmpty() || profile.city.isEmpty()) {
                return ProfileCompletionStatus.INCOMPLETE_LOCATION
            }
            
            ProfileCompletionStatus.COMPLETE
        } catch (e: Exception) {
            ProfileCompletionStatus.ERROR
        }
    }
    
    /**
     * Firestore verisini manuel olarak parse eder
     * Timestamp ve String formatlarını destekler
     */
    private fun parseProfileData(data: Map<String, Any>?): ProfileData? {
        if (data == null) return null
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        
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
            card_0_id = data["card_0_id"] as? String,
            card_0_revealed = data["card_0_revealed"] as? Boolean,
            card_1_id = data["card_1_id"] as? String,
            card_1_revealed = data["card_1_revealed"] as? Boolean,
            card_2_id = data["card_2_id"] as? String,
            card_2_revealed = data["card_2_revealed"] as? Boolean,
            last_draw_date = data["last_draw_date"] as? String
        )
    }
}

enum class ProfileCompletionStatus {
    NOT_LOGGED_IN,
    INCOMPLETE_NAME,
    INCOMPLETE_BIRTH,
    INCOMPLETE_LOCATION,
    COMPLETE,
    ERROR
}

