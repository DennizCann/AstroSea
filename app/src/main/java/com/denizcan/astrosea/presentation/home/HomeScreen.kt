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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import com.denizcan.astrosea.presentation.profile.ProfileData
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.profile.ProfileViewModel
import java.util.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateToHoroscope: () -> Unit,
    onNavigateToTarot: () -> Unit,
    onNavigateToRunes: () -> Unit,
    onNavigateToBirthChart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSignOut: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val profileState = viewModel.profileState

    LaunchedEffect(Unit) {
        // viewModel init bloğunda loadProfile() çağrılıyor
    }

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
                        title = { },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menü",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { /* Bildirimler için tıklama işlemi */ },
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Bildirimler",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier.height(64.dp)
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Profil Uyarı Kartı
                    if (!profileState.isLoading && profileState.profileData.hasIncompleteFields()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clickable { onNavigateToProfile() },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.6f)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))  // Altın sarısı border
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Uyarı",
                                    tint = Color(0xFFFFD700)
                                )
                                Text(
                                    text = "Profil bilgilerinizi tamamlayarak daha doğru astrolojik yorumlar alabilirsiniz.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Hoş geldin kartı yerine basit bir layout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (profileState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            if (profileState.profileData.name.isNullOrEmpty()) {
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
                                    text = buildAnnotatedString {
                                        append("H")
                                        withStyle(SpanStyle(
                                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_bold)),
                                            fontSize = 32.sp
                                        )) { append("oş ") }
                                        append("G")
                                        withStyle(SpanStyle(
                                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_bold)),
                                            fontSize = 32.sp
                                        )) { append("eldin, ") }
                                        profileState.profileData.name?.split(" ")?.forEach { word ->
                                            withStyle(SpanStyle(
                                                fontFamily = FontFamily(Font(R.font.cinzel_black)),
                                                fontSize = 32.sp
                                            )) { append(word.first().uppercase()) }
                                            withStyle(SpanStyle(
                                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_bold)),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 32.sp
                                            )) { 
                                                append(word.substring(1).lowercase())
                                                append(" ")
                                            }
                                        }
                                    },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 32.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 0.dp)
                                )
                                
                                Divider(
                                    modifier = Modifier
                                        .offset(y = (-4).dp)
                                        .padding(vertical = 0.dp),
                                    color = Color.White,
                                    thickness = 2.dp
                                )
                                
                                Text(
                                    text = java.time.LocalDate.now().format(
                                        java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale("tr"))
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
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