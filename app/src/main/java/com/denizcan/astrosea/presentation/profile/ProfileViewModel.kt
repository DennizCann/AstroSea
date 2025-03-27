package com.denizcan.astrosea.presentation.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {
    var profileState by mutableStateOf(ProfileState())
        private set

    init {
        loadProfile()
        startListeningToProfileChanges()
    }

    private fun loadProfile() {
        profileState = profileState.copy(isLoading = true)
        
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
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

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .set(profileState.profileData)
            .addOnSuccessListener {
                loadProfile()
                onSuccess()
            }
            .addOnFailureListener { e ->
                profileState = profileState.copy(
                    error = e.localizedMessage,
                    isLoading = false
                )
                Log.e("ProfileViewModel", "Error saving profile", e)
            }
    }

    fun startListeningToProfileChanges() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        FirebaseFirestore.getInstance()
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
}

data class ProfileState(
    val profileData: ProfileData = ProfileData(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false
) 