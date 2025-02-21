package com.denizcan.astrosea.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state = viewModel.profileState

    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.anamenu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
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
                                    contentDescription = if (state.isEditing) "Kaydet" else "Düzenle",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.6f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = state.profileData.name,
                            onValueChange = { if (state.isEditing) viewModel.onNameChange(it) },
                            label = { Text("Ad", color = Color.White) },
                            enabled = state.isEditing,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedBorderColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                disabledTextColor = Color.White,
                                disabledBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.profileData.surname,
                            onValueChange = { if (state.isEditing) viewModel.onSurnameChange(it) },
                            label = { Text("Soyad", color = Color.White) },
                            enabled = state.isEditing,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedBorderColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                disabledTextColor = Color.White,
                                disabledBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.profileData.birthDate,
                            onValueChange = { if (state.isEditing) viewModel.onBirthDateChange(it) },
                            label = { Text("Doğum Tarihi", color = Color.White) },
                            enabled = state.isEditing,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedBorderColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                disabledTextColor = Color.White,
                                disabledBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.profileData.birthTime,
                            onValueChange = { if (state.isEditing) viewModel.onBirthTimeChange(it) },
                            label = { Text("Doğum Saati", color = Color.White) },
                            enabled = state.isEditing,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedBorderColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                disabledTextColor = Color.White,
                                disabledBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.profileData.country,
                            onValueChange = { if (state.isEditing) viewModel.onCountryChange(it) },
                            label = { Text("Ülke", color = Color.White) },
                            enabled = state.isEditing,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedBorderColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                disabledTextColor = Color.White,
                                disabledBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.profileData.city,
                            onValueChange = { if (state.isEditing) viewModel.onCityChange(it) },
                            label = { Text("Şehir", color = Color.White) },
                            enabled = state.isEditing,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedBorderColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                disabledTextColor = Color.White,
                                disabledBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color.White
                    )
                }

                state.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
} 