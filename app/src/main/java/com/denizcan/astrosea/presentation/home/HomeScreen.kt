package com.denizcan.astrosea.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.denizcan.astrosea.presentation.components.AstroTopBar
import com.denizcan.astrosea.presentation.components.AstroDrawer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import com.denizcan.astrosea.presentation.profile.ProfileData
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHoroscope: () -> Unit,
    onNavigateToTarot: () -> Unit,
    onNavigateToRunes: () -> Unit,
    onNavigateToBirthChart: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var selectedCard by remember { mutableStateOf<Int?>(null) }
    var profileData by remember { mutableStateOf<ProfileData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Kullanıcı bilgilerini yükle
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    profileData = document.toObject(ProfileData::class.java)
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    AstroDrawer(
        drawerState = drawerState,
        scope = scope,
        onSignOut = onSignOut,
        onNavigateToProfile = onNavigateToProfile
    ) {
        Scaffold(
            topBar = {
                AstroTopBar(
                    title = "Ana Sayfa",
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Hoş Geldin Yazısı
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (profileData?.name?.isNotBlank() == true) {
                                "Hoş Geldin, ${profileData?.name}!"
                            } else {
                                "Hoş Geldin!"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )

                        if (profileData?.name?.isBlank() != false) {
                            Button(
                                onClick = onNavigateToProfile,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Profil Bilgilerini Gir")
                            }
                        }
                    }
                }

                // Günün Kartı Seçimi
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    Text(
                        text = "Günün Kartı",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            TarotCard(
                                number = index + 1,
                                isSelected = selectedCard == index,
                                onClick = { selectedCard = index }
                            )
                        }
                    }

                    if (selectedCard != null) {
                        Text(
                            text = "${selectedCard!! + 1}. kart seçildi",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Menu Cards
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MenuCard(
                            title = "Burç\nYorumu",
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            onClick = onNavigateToHoroscope
                        )
                        MenuCard(
                            title = "Tarot\nFalı",
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                            onClick = onNavigateToTarot
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MenuCard(
                            title = "Rün\nFalı",
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            onClick = onNavigateToRunes
                        )
                        MenuCard(
                            title = "Doğum\nHaritası",
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                            onClick = onNavigateToBirthChart
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TarotCard(
    number: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.tertiaryContainer
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.tertiary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "★",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
} 