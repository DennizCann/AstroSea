package com.denizcan.astrosea.presentation.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val profileData = document.toObject(ProfileData::class.java) ?: ProfileData()
                profileState = profileState.copy(
                    profileData = profileData,
                    isLoading = false
                )
                Log.d("ProfileViewModel", "Profile loaded: $profileData")
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
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .set(profileState.profileData)
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
                    val profileData = snapshot.toObject(ProfileData::class.java) ?: ProfileData()
                    profileState = profileState.copy(
                        profileData = profileData,
                        isLoading = false
                    )
                    Log.d("ProfileViewModel", "Profile updated: $profileData")
                }
            }
    }

    private fun stopListeningToProfileChanges() {
        profileListener?.remove()
        profileListener = null
    }
}

data class ProfileState(
    val profileData: ProfileData = ProfileData(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false
) 