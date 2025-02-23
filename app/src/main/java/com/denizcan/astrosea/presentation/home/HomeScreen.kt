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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

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
                        .padding(top = 4.dp)
                        .verticalScroll(rememberScrollState()),
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
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 18.sp
                                    )
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontSize = 18.sp
                            ),
                            singleLine = true
                        )
                    }

                    // Günlük Açılım başlığı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.varlik2),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Text(
                                "Günlük Açılım",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_bold)),
                                    fontSize = 24.sp
                                ),
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            
                            Image(
                                painter = painterResource(id = R.drawable.varlik2),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Tarot kartları
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DailyCard(
                            title = "1",
                            modifier = Modifier
                                .height(160.dp)
                                .width(95.dp)
                        )
                        DailyCard(
                            title = "2",
                            modifier = Modifier
                                .height(160.dp)
                                .width(95.dp)
                        )
                        DailyCard(
                            title = "3",
                            modifier = Modifier
                                .height(160.dp)
                                .width(95.dp)
                        )
                    }

                    // Alt kartlar için grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // İlk sıra
                        Row(
                            modifier = Modifier.height(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ServiceCard(
                                title = "Evet / Hayır",
                                onClick = onNavigateToTarot,
                                modifier = Modifier.weight(1f)
                            )
                            ServiceCard(
                                title = "Burç\nYorumu",
                                onClick = onNavigateToHoroscope,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // İkinci sıra
                        Row(
                            modifier = Modifier.height(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                        // Üçüncü sıra
                        Row(
                            modifier = Modifier.height(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ServiceCard(
                                title = "Yakında",
                                onClick = { },
                                modifier = Modifier.weight(1f),
                                enabled = false
                            )
                            ServiceCard(
                                title = "Yakında",
                                onClick = { },
                                modifier = Modifier.weight(1f),
                                enabled = false
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
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color.Black.copy(alpha = 0.6f) 
                           else Color.Gray.copy(alpha = 0.3f)
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
            if (enabled) {
                Image(
                    painter = painterResource(
                        id = when (title) {
                            "Burç\nYorumu" -> R.drawable.zodiac
                            "Evet / Hayır" -> R.drawable.tarot
                            "Rün\nFalı" -> R.drawable.rune
                            else -> R.drawable.birthchart
                        }
                    ),
                    contentDescription = title,
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Yakında",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Center,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
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
        modifier = modifier.clickable { /* Kart seçme işlemi gelecek */ },  // Boyutları Row'dan alacak
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