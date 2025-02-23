package com.denizcan.astrosea.presentation.profile

data class ProfileData(
    val name: String = "",
    val surname: String = "",
    val birthDate: String = "",
    val birthTime: String = "",
    val country: String = "",
    val city: String = ""
) {
    fun hasIncompleteFields(): Boolean {
        return name.isBlank() || 
               surname.isBlank() || 
               birthDate.isBlank() || 
               birthTime.isBlank() || 
               country.isBlank() || 
               city.isBlank()
    }

    // Firestore için boş constructor gerekli
    constructor() : this("", "", "", "", "", "")
} 