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
import com.denizcan.astrosea.presentation.components.AstroDrawer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import com.denizcan.astrosea.presentation.profile.ProfileData
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import com.denizcan.astrosea.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHoroscope: () -> Unit,
    onNavigateToTarot: () -> Unit,
    onNavigateToRunes: () -> Unit,
    onNavigateToBirthChart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSignOut: () -> Unit
) {
    var profileData by remember { mutableStateOf<ProfileData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    
    // Profil bilgilerini yükle
    LaunchedEffect(userId) {
        userId?.let { uid ->
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    AstroDrawer(
        drawerState = drawerState,
        scope = scope,
        onSignOut = onSignOut,
        onNavigateToProfile = onNavigateToProfile
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Arka plan görseli
            Image(
                painter = painterResource(id = R.drawable.anamenu),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("AstroSea", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menü",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Hoş geldin kartı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            } else {
                                if (profileData?.name.isNullOrEmpty()) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "Profilinizi Tamamlayın",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            "Kişiselleştirilmiş deneyim için lütfen profil bilgilerinizi doldurun.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                        Button(
                                            onClick = onNavigateToProfile,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White.copy(alpha = 0.2f)
                                            )
                                        ) {
                                            Text(
                                                "Profile Git",
                                                color = Color.White
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        "Hoş Geldin, ${profileData?.name}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White
                                    )
                                    Text(
                                        "Yıldızların ve kadim bilgeliğin dünyasına hoş geldin.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // Arama kısmı
                    var searchQuery by remember { mutableStateOf("") }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            placeholder = {
                                Text(
                                    "Aklındaki sorunun cevabını hemen gör...",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            singleLine = true
                        )
                    }

                    // Günün Kartları
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Günün Kartları",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                DailyCard(title = "1")
                                DailyCard(title = "2")
                                DailyCard(title = "3")
                            }
                        }
                    }

                    // Alt kartlar için grid
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ServiceCard(
                                title = "Burç\nYorumu",
                                onClick = onNavigateToHoroscope,
                                modifier = Modifier.weight(1f)
                            )
                            ServiceCard(
                                title = "Tarot\nFalı",
                                onClick = onNavigateToTarot,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ServiceCard(
                                title = "Rün\nFalı",
                                onClick = onNavigateToRunes,
                                modifier = Modifier.weight(1f)
                            )
                            ServiceCard(
                                title = "Doğum\nHaritası",
                                onClick = onNavigateToBirthChart,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(
                painter = painterResource(
                    id = when (title) {
                        "Burç\nYorumu" -> R.drawable.zodiac
                        "Tarot\nFalı" -> R.drawable.tarot
                        "Rün\nFalı" -> R.drawable.rune
                        else -> R.drawable.birthchart
                    }
                ),
                contentDescription = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}

@Composable
private fun DailyCard(
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .width(100.dp)
            .clickable { /* Kart seçme işlemi gelecek */ },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(0.dp, Color.Transparent),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                contentDescription = "Tarot kartı $title",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
} 