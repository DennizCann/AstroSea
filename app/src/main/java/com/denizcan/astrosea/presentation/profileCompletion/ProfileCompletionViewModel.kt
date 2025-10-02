package com.denizcan.astrosea.presentation.profileCompletion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.astrosea.presentation.profile.ProfileData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                // Kullanıcı belgesi yoksa oluştur
                val newProfile = ProfileData()
                firestore.collection("users").document(userId).set(newProfile).await()
                return ProfileCompletionStatus.INCOMPLETE_NAME
            }
            
            val profile = doc.toObject(ProfileData::class.java) ?: return ProfileCompletionStatus.INCOMPLETE_NAME
            
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
}

enum class ProfileCompletionStatus {
    NOT_LOGGED_IN,
    INCOMPLETE_NAME,
    INCOMPLETE_BIRTH,
    INCOMPLETE_LOCATION,
    COMPLETE,
    ERROR
}

