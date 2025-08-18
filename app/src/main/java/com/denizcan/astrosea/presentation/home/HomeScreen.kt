package com.denizcan.astrosea.presentation.home

import android.util.Log
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import com.denizcan.astrosea.presentation.notifications.NotificationManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// MenuItem data class'ını ekle
data class MenuItem(
    val title: String,
    val icon: Int,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateToProfile: () -> Unit,
    onNavigateToHoroscope: () -> Unit,
    onNavigateToTarotMeanings: () -> Unit,
    onNavigateToBirthChart: () -> Unit,
    onNavigateToMotivation: () -> Unit,
    onNavigateToYesNo: () -> Unit,
    onNavigateToRelationshipReadings: () -> Unit,
    onNavigateToCareerReading: () -> Unit,
    onNavigateToMore: () -> Unit,
    onNavigateToGeneralReadings: () -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    onNavigateToDailyReadingInfo: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onSignOut: () -> Unit
) {
    val profileState = viewModel.profileState
    val context = LocalContext.current
    val dailyTarotViewModel: DailyTarotViewModel = viewModel(factory = DailyTarotViewModel.Factory(context))
    
    // Bildirim yöneticisi ve sayacı
    val notificationManager = remember { NotificationManager(context) }
    var unreadNotificationCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    // Çıkış yapma animasyonu için state
    var showLogoutAnimation by remember { mutableStateOf(false) }
    
    // Çıkış yapma işleyicisi
    val handleSignOut: () -> Unit = {
        showLogoutAnimation = true
        scope.launch {
            delay(1000) // 1 saniye animasyon
            onSignOut()
        }
    }
    
    // Pulse animasyonu için
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Okunmamış bildirim sayısını yükle ve periyodik olarak güncelle
    LaunchedEffect(Unit) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            scope.launch {
                try {
                    unreadNotificationCount = notificationManager.getUnreadNotificationCount(userId)
                    android.util.Log.d("HomeScreen", "Initial notification count: $unreadNotificationCount")
                } catch (e: Exception) {
                    android.util.Log.e("HomeScreen", "Error loading initial notification count", e)
                }
            }
        } else {
            android.util.Log.d("HomeScreen", "No user logged in")
        }
    }
    
    // Bildirim sayacını periyodik olarak güncelle (her 5 saniyede bir)
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000) // 5 saniye
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                scope.launch {
                    try {
                        val newCount = notificationManager.getUnreadNotificationCount(userId)
                        if (newCount != unreadNotificationCount) {
                            android.util.Log.d("HomeScreen", "Notification count updated: $unreadNotificationCount -> $newCount")
                            unreadNotificationCount = newCount
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error updating notification count", e)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // viewModel init bloğunda loadProfile() çağrılıyor
    }

    // Günlük kartları sadece bir kez yükle
    LaunchedEffect(Unit) {
        // Sadece bir kez yükle, sürekli refresh yapma
        dailyTarotViewModel.refreshCards()
    }

    // Menü seçeneklerini güncelliyoruz
    val menuItems = listOf(
        MenuItem(
            title = "Tüm Anlamlar",
            icon = R.drawable.tarotacilimlariimage,
            route = "tarot_meanings"
        ),
        MenuItem(
            title = "İlişki Açılımları",
            icon = R.drawable.gununkartiimaji,
            route = "relationship_readings"
        ),
        MenuItem(
            title = "Genel Açılımlar",
            icon = R.drawable.tarot,
            route = "general_readings"
        ),
        MenuItem(
            title = "Kariyer Açılımı",
            icon = R.drawable.kariyer,
            route = "career_reading"
        ),
        MenuItem(
            title = "Evet - Hayır",
            icon = R.drawable.evet_hayir,
            route = "yes_no"
        ),
        MenuItem(
            title = "Daha Fazlası",
            icon = R.drawable.zodiac,
            route = "more"
        )
    )

    var parentContainerSize by remember { mutableStateOf(IntSize.Zero) }

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
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                onClick = onNavigateToProfile,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profil",
                                    tint = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = onNavigateToNotifications
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Bildirimler",
                                        tint = Color.White
                                    )
                                }
                                
                                // Okunmamış bildirim sayacı
                                if (unreadNotificationCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD700))
                                            .align(Alignment.TopEnd)
                                            .offset(x = 8.dp, y = (-4).dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                            color = Color.Black,
                                            fontSize = 10.sp,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily(Font(R.font.cinzel_bold))
                                            )
                                        )
                                    }
                                    
                                    // Animasyonlu pulse efekti
                                    if (unreadNotificationCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFD700).copy(alpha = 0.3f))
                                                .align(Alignment.TopEnd)
                                                .offset(x = 6.dp, y = (-6).dp)
                                                .graphicsLayer {
                                                    scaleX = pulseScale
                                                    scaleY = pulseScale
                                                }
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = handleSignOut,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Çıkış Yap",
                                    tint = Color.White
                                )
                            }
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
                            // Profil bilgileri eksikse boş bir alan bırak
                            Spacer(modifier = Modifier.height(32.dp))
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
                var showSearchAlert by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { showSearchAlert = true },
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
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
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
                        singleLine = true,
                        enabled = false
                    )
                }

                // Arama uyarı dialog'u
                if (showSearchAlert) {
                    AlertDialog(
                        onDismissRequest = { showSearchAlert = false },
                        title = {
                            Text(
                                "Yakında Hizmetinizde",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                    fontSize = 20.sp
                                ),
                                color = Color.Black
                            )
                        },
                        text = {
                            Text(
                                "Bu özellik şu anda geliştirme aşamasındadır. Yakında aklınızdaki sorulara anında cevap alabileceksiniz!",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                    fontSize = 16.sp
                                ),
                                color = Color.Black
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { showSearchAlert = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1A2236)
                                )
                            ) {
                                Text(
                                    "Tamam",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_bold))
                                    )
                                )
                            }
                        },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Günlük Açılım başlığı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .clickable { 
                            // Kartlar zaten doğru state'te, tekrar yüklemeye gerek yok
                            onNavigateToDailyReadingInfo() 
                        },
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .onGloballyPositioned { coordinates ->
                            parentContainerSize = coordinates.size
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val sortedCards = dailyTarotViewModel.dailyCards.sortedBy { it.index }
                        Log.d("HomeScreen", "Rendering cards: ${sortedCards.map { "${it.index}:${it.card?.name ?: "null"}" }}")
                        
                        sortedCards.forEach { cardState ->
                            AnimatedCardReveal(
                                cardState = cardState,
                                onCardClick = {
                                    // Kart zaten açıksa, günlük açılım info sayfasına git
                                    onNavigateToDailyReadingInfo()
                                },
                                onCardDetailClick = { cardId ->
                                    onNavigateToCardDetail(cardId)
                                },
                                onDrawCard = {
                                    if (!cardState.isRevealed) {
                                        // Önce kartları çek (eğer henüz çekilmemişse)
                                        if (!dailyTarotViewModel.hasDrawnToday) {
                                            dailyTarotViewModel.drawDailyCards()
                                        }
                                        // Sonra kartı aç
                                        dailyTarotViewModel.revealCard(cardState.index)
                                        // Bildirim sayısını güncelle (arka planda)
                                        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                        if (userId != null) {
                                            scope.launch {
                                                try {
                                                    unreadNotificationCount = notificationManager.getUnreadNotificationCount(userId)
                                                } catch (e: Exception) {
                                                    android.util.Log.e("HomeScreen", "Error updating notification count after card reveal", e)
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .height(160.dp)
                                    .width(95.dp),
                                parentSize = parentContainerSize
                            )
                        }
                    }
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
                            title = menuItems[0].title,
                            onClick = onNavigateToTarotMeanings,
                            modifier = Modifier.weight(1f),
                            imageResId = menuItems[0].icon
                        )
                        ServiceCard(
                            title = menuItems[1].title,
                            onClick = onNavigateToRelationshipReadings,
                            modifier = Modifier.weight(1f),
                            imageResId = menuItems[1].icon
                        )
                    }

                    // İkinci sıra
                    Row(
                        modifier = Modifier.height(160.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ServiceCard(
                            title = menuItems[2].title,
                            onClick = onNavigateToGeneralReadings,
                            modifier = Modifier.weight(1f),
                            imageResId = menuItems[2].icon
                        )
                        ServiceCard(
                            title = menuItems[3].title,
                            onClick = onNavigateToCareerReading,
                            modifier = Modifier.weight(1f),
                            imageResId = menuItems[3].icon
                        )
                    }

                    // Üçüncü sıra
                    Row(
                        modifier = Modifier.height(160.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ServiceCard(
                            title = menuItems[4].title,
                            onClick = onNavigateToYesNo,
                            modifier = Modifier.weight(1f),
                            imageResId = menuItems[4].icon
                        )
                        ServiceCard(
                            title = menuItems[5].title,
                            onClick = onNavigateToMore,
                            modifier = Modifier.weight(1f),
                            imageResId = menuItems[5].icon
                        )
                    }
                }
            }
        }
        
        // Çıkış yapma animasyonu overlay'i
        if (showLogoutAnimation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1000f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.acilimlararkaplan),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Çıkış yapılıyor...",
                        color = Color.White,
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
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
    enabled: Boolean = true,
    imageResId: Int? = null,
    imageColor: Color? = null
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
                        id = imageResId ?: when (title) {
                            "Burç Yorumu" -> R.drawable.zodiac
                            "Evet / Hayır" -> R.drawable.tarot
                            "Doğum Haritası" -> R.drawable.birthchart
                            else -> R.drawable.birthchart
                        }
                    ),
                    contentDescription = title,
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = imageColor?.let { ColorFilter.tint(it) }
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
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                ),
                textAlign = TextAlign.Center,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
            )
        }
    }
}
