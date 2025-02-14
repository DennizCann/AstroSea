package com.denizcan.astrosea.presentation.profile

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
    }

    fun loadProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        profileState = profileState.copy(isLoading = true)

        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val data = document.toObject(ProfileData::class.java) ?: ProfileData()
                    profileState = profileState.copy(
                        profileData = data,
                        isLoading = false,
                        isEditing = !document.exists()
                    )
                }
                .addOnFailureListener { exception ->
                    profileState = profileState.copy(
                        error = exception.localizedMessage,
                        isLoading = false
                    )
                }
        } catch (e: Exception) {
            profileState = profileState.copy(
                error = e.localizedMessage,
                isLoading = false
            )
        }
    }

    fun onNameChange(name: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(name = name)
        )
    }

    fun onSurnameChange(surname: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(surname = surname)
        )
    }

    fun onBirthDateChange(birthDate: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(birthDate = birthDate)
        )
    }

    fun onBirthTimeChange(birthTime: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(birthTime = birthTime)
        )
    }

    fun onCountryChange(country: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(country = country)
        )
    }

    fun onCityChange(city: String) {
        profileState = profileState.copy(
            profileData = profileState.profileData.copy(city = city)
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
                profileState = profileState.copy(
                    isLoading = false,
                    isEditing = false
                )
                onSuccess()
            }
            .addOnFailureListener {
                profileState = profileState.copy(
                    error = it.localizedMessage,
                    isLoading = false
                )
            }
    }
}

data class ProfileState(
    val profileData: ProfileData = ProfileData(),
    val isLoading: Boolean = false,
    val isEditing: Boolean = true,
    val error: String? = null
) 