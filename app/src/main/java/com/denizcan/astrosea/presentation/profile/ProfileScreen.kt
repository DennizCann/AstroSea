package com.denizcan.astrosea.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.presentation.components.AstroTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state = viewModel.profileState

    Scaffold(
        topBar = {
            AstroTopBar(
                title = "Profil",
                onBackClick = onNavigateBack,
                actions = {
                    if (!state.isLoading) {
                        IconButton(
                            onClick = {
                                if (state.isEditing) {
                                    viewModel.saveProfile(onSuccess = {})
                                } else {
                                    viewModel.toggleEditing()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (state.isEditing) Icons.Default.Done else Icons.Default.Edit,
                                contentDescription = if (state.isEditing) "Kaydet" else "Düzenle"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (state.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Hata oluştu:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadProfile() }) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = state.profileData.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Adınız") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.isEditing
                    )

                    OutlinedTextField(
                        value = state.profileData.surname,
                        onValueChange = viewModel::onSurnameChange,
                        label = { Text("Soyadınız") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.isEditing
                    )

                    OutlinedTextField(
                        value = state.profileData.birthDate,
                        onValueChange = viewModel::onBirthDateChange,
                        label = { Text("Doğum Tarihi (GG/AA/YYYY)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.isEditing
                    )

                    OutlinedTextField(
                        value = state.profileData.birthTime,
                        onValueChange = viewModel::onBirthTimeChange,
                        label = { Text("Doğum Saati (SS:DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.isEditing
                    )

                    OutlinedTextField(
                        value = state.profileData.country,
                        onValueChange = viewModel::onCountryChange,
                        label = { Text("Doğduğunuz Ülke") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.isEditing
                    )

                    OutlinedTextField(
                        value = state.profileData.city,
                        onValueChange = viewModel::onCityChange,
                        label = { Text("Doğduğunuz Şehir") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.isEditing
                    )

                    if (state.isEditing) {
                        Button(
                            onClick = { viewModel.saveProfile(onSuccess = {}) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Kaydet")
                        }
                    }
                }
            }
        }
    }
} 