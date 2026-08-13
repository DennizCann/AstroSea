package com.denizcan.astrosea.presentation.general

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.R
import com.denizcan.astrosea.ads.AdConfig
import com.denizcan.astrosea.ads.AdRightsManager
import com.denizcan.astrosea.ads.RewardedAdManager
import com.denizcan.astrosea.presentation.components.AstroTopBar
import com.denizcan.astrosea.presentation.home.DailyTarotViewModel
import com.denizcan.astrosea.presentation.profile.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight

// Varsayılan yorum oluşturma fonksiyonu
fun generateDefaultInterpretation(readingType: String, drawnCards: List<ReadingCardState>, readingInfo: ReadingInfo): String {
    val revealedCards = drawnCards.filter { it.isRevealed }
    return if (revealedCards.isNotEmpty()) {
        val interpretation = StringBuilder()
        interpretation.append("$readingType Yorumu\n\n")
        
        // Eski format kullan (fallback)
        revealedCards.forEachIndexed { index, cardState ->
            val meaning = readingInfo.cardMeanings.getOrNull(index) ?: "Kart ${index + 1}"
            val cardName = cardState.card?.turkishName ?: cardState.card?.name ?: "Bilinmeyen Kart"
            val cardMeaning = cardState.card?.meaningUpright ?: "Bu kart henüz yorumlanmamış."
            
            interpretation.append("$meaning: $cardName\n")
            interpretation.append("$cardMeaning\n\n")
        }
        
        // Genel yorum ekle
        interpretation.append("Genel Yorum:\n")
        interpretation.append("Bu açılım size hayatınızın bu alanında rehberlik etmek için tasarlanmıştır. ")
        interpretation.append("Çektiğiniz kartların anlamlarını dikkatlice değerlendirin ve iç sesinizi dinleyin. ")
        interpretation.append("Her kart size özel bir mesaj taşımaktadır.\n\n")
        
        interpretation.toString()
    } else {
        "Henüz kart çekilmemiş. Lütfen önce kartlarınızı çekin."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralReadingDetailScreen(
    readingType: String,
    onNavigateBack: () -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    onNavigateToPremium: () -> Unit = {}  // Premium ekranına yönlendirme
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Premium kontrol için ProfileViewModel
    val profileViewModel: ProfileViewModel = viewModel()
    val viewModel: GeneralReadingViewModel = viewModel(
        key = "GeneralReadingViewModel_$readingType",
        factory = GeneralReadingViewModel.Factory(context)
    )
    
    // Günlük açılım için DailyTarotViewModel'i al
    val dailyTarotViewModel: DailyTarotViewModel = viewModel(
        key = "DailyTarotViewModel",
        factory = DailyTarotViewModel.Factory(context)
    )
    
    // Ekran durumları - rememberSaveable: kart detayına gidip geri dönünce ekran durumu korunsun
    var currentScreen by rememberSaveable { mutableStateOf("detail") } // "detail", "loading", "interpretation"
    
    // Premium dialog state
    var showPremiumDialog by remember { mutableStateOf(false) }
    // Günlük reklam hakkı bittiği için mi premium dialog gösteriliyor
    var adLimitReached by remember { mutableStateOf(false) }
    // Reklam izle dialogu state
    var showAdDialog by remember { mutableStateOf(false) }
    var remainingAdUnlocks by remember { mutableStateOf(0) }
    var isAdShowing by remember { mutableStateOf(false) }
    
    // Reklam yöneticileri
    val rewardedAdManager = remember { RewardedAdManager(context) }
    val adRightsManager = remember { AdRightsManager() }
    LaunchedEffect(Unit) { rewardedAdManager.preload() }
    
    // Günlük açılım mı kontrolü
    val isDailyReading = readingType.trim() == "GÜNLÜK AÇILIM"
    
    // Profil durumu (anlık kontrolden sonra güncellenir)
    val profileState = profileViewModel.profileState
    
    // Sayfa yüklendiğinde state'i yükle - kart çekmek herkese açık,
    // yorum reklam/premium ile açılır
    LaunchedEffect(readingType) {
        if (isDailyReading) {
            Log.d("GeneralReadingDetailScreen", "Günlük açılım için DailyTarotViewModel set ediliyor")
            viewModel.setDailyTarotViewModel(dailyTarotViewModel)
        } else {
            viewModel.loadReadingState(readingType)
        }
    }
    
    val readingInfo = remember(readingType) {
        getReadingInfo(readingType)
    }
    
    when (currentScreen) {
        "loading" -> {
            LoadingScreen(
                viewModel = viewModel,
                onLoadingComplete = {
                    currentScreen = "interpretation"
                }
            )
        }
        "interpretation" -> {
            GeneralReadingInterpretationScreen(
                readingType = readingType,
                onNavigateBack = {
                    currentScreen = "detail"
                },
                onNavigateToCardDetail = onNavigateToCardDetail
            )
        }
        else -> {
            // Ana detay ekranı
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.acilimlararkaplan),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Scaffold(
                    topBar = {
                        AstroTopBar(
                            title = readingType,
                            onBackClick = onNavigateBack
                        )
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Çerçeve ve Kartlar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.98f)
                                .weight(6.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.acilimlarsayfasitak),
                                contentDescription = "Çerçeve",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )

                            // Kartların yerleşeceği alan
                            var parentContainerSize by remember { mutableStateOf(IntSize.Zero) }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .onGloballyPositioned { coordinates ->
                                        parentContainerSize = coordinates.size
                                    }
                            ) {
                                CardLayoutContainer(
                                    readingInfo = readingInfo,
                                    drawnCardMap = viewModel.drawnCards.associateBy { it.index },
                                    onDrawCard = { index ->
                                        viewModel.drawCardForPosition(readingType, index)
                                    },
                                    onNavigateToCardDetail = onNavigateToCardDetail,
                                    parentSize = parentContainerSize
                                )
                            }
                        }
                        
                        // Kart Anlamları ve Butonlar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .weight(3.5f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Kart Anlamları - Her zaman görünür
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                itemsIndexed(readingInfo.cardMeanings) { index, meaning ->
                                    val card = viewModel.drawnCards.find { it.index == index }?.card
                                    val isCardDrawn = card != null
                                    val isCardRevealed = card != null && viewModel.drawnCards.find { it.index == index }?.isRevealed == true
                                    
                                    // Günlük açılım için özel mantık: kart açılmışsa ismini göster, açılmamışsa sadece anlamı göster
                                    val displayText = if (readingType.trim() == "GÜNLÜK AÇILIM") {
                                        if (isCardRevealed) {
                                            "${index + 1}. $meaning: ${card?.turkishName ?: card?.name ?: ""}"
                                        } else {
                                            "${index + 1}. $meaning"
                                        }
                                    } else {
                                        // Diğer açılımlar için normal mantık
                                        "${index + 1}. $meaning: ${card?.turkishName ?: card?.name ?: "..."}"
                                    }
                                    
                                    MeaningCard(
                                        text = displayText,
                                        onClick = { 
                                            if (isCardDrawn && isCardRevealed) {
                                                onNavigateToCardDetail(card.id)
                                            } else {
                                                // Kart henüz çekilmemişse veya açılmamışsa, tıklandığında çek
                                                viewModel.drawCardForPosition(readingType, index)
                                                // Günlük açılım için ek state güncellemesi kaldırıldı
                                            }
                                        },
                                        isSelected = isCardRevealed,
                                        enabled = true
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Yeniden Çek ve Yorumunu Gör Butonları - Yan yana
                            val isDailyReading = readingType.trim() == "GÜNLÜK AÇILIM"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Yeniden Çek Butonu
                                Button(
                                    onClick = { viewModel.resetAndDrawNew(readingType) },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isDailyReading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDailyReading) Color.Gray.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.6f)
                                    ),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = if (isDailyReading) "Günlük Açılım - Günde Bir Kez" else "Yeniden Çek",
                                        color = if (isDailyReading) Color.Gray else Color.White,
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 16.sp
                                    )
                                }
                                
                                // Yorumunu Gör Butonu
                                Button(
                                    onClick = { 
                                        scope.launch {
                                            try {
                                                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                                Log.d("GeneralReadingDetailScreen", "Yorumu Gör tıklandı - userId: $userId")
                                                
                                                if (userId == null) {
                                                    Log.e("GeneralReadingDetailScreen", "userId null! Premium dialog gösteriliyor")
                                                    adLimitReached = false
                                                    showPremiumDialog = true
                                                    return@launch
                                                }
                                                
                                                // Bu açılım için yorum zaten üretilmişse (reklam/premium ile
                                                // daha önce açılmış) doğrudan göster - tekrar hak istemez
                                                if (viewModel.generatedReading != null) {
                                                    currentScreen = "interpretation"
                                                    return@launch
                                                }
                                                
                                                // Firestore'dan direkt oku
                                                val document = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                    .collection("users")
                                                    .document(userId)
                                                    .get()
                                                    .await()
                                                
                                                val isPremium = document.getBoolean("isPremium") ?: false
                                                Log.d("GeneralReadingDetailScreen", "Firestore'dan okunan isPremium: $isPremium")
                                                
                                                if (isPremium) {
                                                    // Premium kullanıcı - yorum üret, 10 dk'da hazır olur
                                                    Log.d("GeneralReadingDetailScreen", "Premium kullanıcı - yorum oluşturuluyor")
                                                    viewModel.generateReading(readingType, AdConfig.PREMIUM_INTERPRETATION_DELAY_MILLIS)
                                                    currentScreen = "interpretation"
                                                } else {
                                                    // Ücretsiz kullanıcı - reklam hakkı kontrolü
                                                    val remaining = adRightsManager.getRemainingUnlocks()
                                                    Log.d("GeneralReadingDetailScreen", "Kalan reklam hakkı: $remaining")
                                                    if (remaining > 0) {
                                                        remainingAdUnlocks = remaining
                                                        showAdDialog = true
                                                    } else {
                                                        adLimitReached = true
                                                        showPremiumDialog = true
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e("GeneralReadingDetailScreen", "Yorum akışı hatası", e)
                                                adLimitReached = false
                                                showPremiumDialog = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = viewModel.drawnCards.filter { it.isRevealed }.size == readingInfo.cardCount,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (viewModel.drawnCards.filter { it.isRevealed }.size == readingInfo.cardCount) 
                                            Color.Black.copy(alpha = 0.6f) 
                                        else 
                                            Color.Gray.copy(alpha = 0.3f)
                                    ),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "Yorumunu Gör",
                                        color = if (viewModel.drawnCards.filter { it.isRevealed }.size == readingInfo.cardCount) Color.White else Color.Gray,
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Reklam İzle Dialogu - ücretsiz kullanıcı, günlük hakkı varsa
    if (showAdDialog) {
        AlertDialog(
            onDismissRequest = { if (!isAdShowing) showAdDialog = false },
            containerColor = Color(0xFF1A2236),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "🎬 Reklam İzle, Yorumunu Aç",
                    color = Color(0xFFD4AF37),
                    fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Kısa bir reklam izleyerek bu açılımın yapay zeka yorumunu ücretsiz açabilirsiniz. Yorumunuz 5 dakika içinde hazır olur.\n\nBugün kalan hakkınız: $remainingAdUnlocks/${AdConfig.DAILY_AD_UNLOCK_LIMIT}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    if (isAdShowing) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(color = Color(0xFFD4AF37), modifier = Modifier.size(28.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val activity = context as? android.app.Activity ?: return@Button
                        isAdShowing = true
                        rewardedAdManager.show(
                            activity = activity,
                            onRewarded = {
                                scope.launch {
                                    val consumed = adRightsManager.consumeUnlock()
                                    isAdShowing = false
                                    showAdDialog = false
                                    if (consumed) {
                                        viewModel.generateReading(readingType, AdConfig.AD_INTERPRETATION_DELAY_MILLIS)
                                        currentScreen = "interpretation"
                                    } else {
                                        adLimitReached = true
                                        showPremiumDialog = true
                                    }
                                }
                            },
                            onFailed = {
                                isAdShowing = false
                                Log.w("GeneralReadingDetailScreen", "Reklam gösterilemedi")
                            }
                        )
                    },
                    enabled = !isAdShowing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isAdShowing) "Yükleniyor..." else "Reklam İzle",
                        color = Color.Black,
                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdDialog = false
                        onNavigateToPremium()
                    },
                    enabled = !isAdShowing
                ) {
                    Text(
                        text = "Reklamsız: Premium'a Geç",
                        color = Color(0xFFD4AF37).copy(alpha = 0.8f),
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        fontSize = 14.sp
                    )
                }
            }
        )
    }
    
    // Premium Dialog
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            containerColor = Color(0xFF1A2236),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "🌟 Premium Özellik",
                    color = Color(0xFFD4AF37),
                    fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (adLimitReached) "Bugünlük Reklam Hakkınız Bitti" else "AI Destekli Tarot Yorumu",
                        color = Color.White,
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_bold)),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (adLimitReached) 
                            "Reklam izleyerek günde en fazla ${AdConfig.DAILY_AD_UNLOCK_LIMIT} yorum açabilirsiniz ve bugünkü haklarınızı kullandınız.\n\nPremium üye olarak reklamsız şekilde tüm açılımların yorumlarına erişebilirsiniz."
                        else 
                            "Yapay zeka destekli kişiselleştirilmiş tarot yorumlarına Premium üyelikle ya da reklam izleyerek erişebilirsiniz.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPremiumDialog = false
                        onNavigateToPremium()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Premium'a Geç",
                        color = Color.Black,
                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPremiumDialog = false }
                ) {
                    Text(
                        text = "Daha Sonra",
                        color = Color.White.copy(alpha = 0.7f),
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        fontSize = 14.sp
                    )
                }
            }
        )
    }
}

@Composable
fun GeneralReadingInterpretationScreen(
    readingType: String,
    onNavigateBack: () -> Unit,
    onNavigateToCardDetail: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: GeneralReadingViewModel = viewModel(
        key = "GeneralReadingViewModel_$readingType",
        factory = GeneralReadingViewModel.Factory(context)
    )
    
    // Günlük açılım için DailyTarotViewModel'i al
    val dailyTarotViewModel: DailyTarotViewModel = viewModel(
        key = "DailyTarotViewModel",
        factory = DailyTarotViewModel.Factory(context)
    )
    
    LaunchedEffect(readingType) {
        if (readingType.trim() == "GÜNLÜK AÇILIM") {
            viewModel.setDailyTarotViewModel(dailyTarotViewModel)
            // DailyTarotViewModel set edildikten sonra state'i yükle
            delay(200)
        }
        viewModel.loadReadingState(readingType)
    }
    
    val readingInfo = remember(readingType) {
        getReadingInfo(readingType)
    }
    
    // Gemini yorumunu veya varsayılan yorumu kullan
    val displayInterpretation = remember(viewModel.generatedReading, viewModel.readingError, viewModel.drawnCards, readingInfo) {
        when {
            viewModel.readingError != null -> {
                "Hata: ${viewModel.readingError}\n\n" +
                "Varsayılan yorum gösteriliyor:\n\n" +
                generateDefaultInterpretation(readingType, viewModel.drawnCards, readingInfo)
            }
            viewModel.generatedReading != null -> {
                viewModel.generatedReading!!
            }
            else -> {
                generateDefaultInterpretation(readingType, viewModel.drawnCards, readingInfo)
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.acilimlararkaplan),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Scaffold(
            topBar = {
                AstroTopBar(
                    title = readingType,
                    onBackClick = onNavigateBack
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Çerçeve ve Açık Kartlar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .weight(6.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.acilimlarsayfasitak),
                        contentDescription = "Çerçeve",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    // Açık kartların yerleşeceği alan
                    var parentContainerSize by remember { mutableStateOf(IntSize.Zero) }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coordinates ->
                                parentContainerSize = coordinates.size
                            }
                    ) {
                        // Açık kartları göster
                        val revealedCards = viewModel.drawnCards.filter { it.isRevealed }
                        CardLayoutContainer(
                            readingInfo = readingInfo,
                            drawnCardMap = revealedCards.associateBy { it.index },
                            onDrawCard = { /* Kartlar zaten açık */ },
                            onNavigateToCardDetail = onNavigateToCardDetail,
                            parentSize = parentContainerSize,
                            forceRevealed = true // Yorum ekranında kartları zorla açık göster
                        )
                    }
                }
                
                // Yorum Kutusu - Kaydırılabilir
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .weight(3.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Geri sayım: yorum hazır olma zamanı gelmediyse bekleme ekranı göster
                    val readyAt = viewModel.interpretationReadyAt
                    var now by remember { mutableStateOf(System.currentTimeMillis()) }
                    LaunchedEffect(readyAt) {
                        while (readyAt != null && System.currentTimeMillis() < readyAt) {
                            delay(1000)
                            now = System.currentTimeMillis()
                        }
                        now = System.currentTimeMillis()
                    }
                    val isReady = readyAt == null || now >= readyAt
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        if (!isReady && readyAt != null) {
                            // Bekleme ekranı - geri sayım + süreyi kısaltma reklamı
                            val remainingMillis = (readyAt - now).coerceAtLeast(0)
                            val minutes = remainingMillis / 60000
                            val seconds = (remainingMillis % 60000) / 1000
                            val canSpeedUp = remainingMillis > AdConfig.SPEED_UP_MILLIS
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "✨ Falınız hazırlanıyor...",
                                    color = Color(0xFFD4AF37),
                                    fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = String.format("%02d:%02d", minutes, seconds),
                                    color = Color.White,
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_bold)),
                                    fontSize = 32.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Kartlarınızın enerjisi okunuyor. Yorumunuz hazır olduğunda burada görünecek.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                                if (canSpeedUp) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    val activity = context as? android.app.Activity
                                    val speedUpAdManager = remember { com.denizcan.astrosea.ads.RewardedAdManager(context) }
                                    LaunchedEffect(Unit) { speedUpAdManager.preload() }
                                    var isSpeedUpAdShowing by remember { mutableStateOf(false) }
                                    
                                    Button(
                                        onClick = {
                                            if (activity != null) {
                                                isSpeedUpAdShowing = true
                                                speedUpAdManager.show(
                                                    activity = activity,
                                                    onRewarded = {
                                                        isSpeedUpAdShowing = false
                                                        viewModel.speedUpInterpretation(readingType)
                                                        now = System.currentTimeMillis()
                                                    },
                                                    onFailed = { isSpeedUpAdShowing = false }
                                                )
                                            }
                                        },
                                        enabled = !isSpeedUpAdShowing,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (isSpeedUpAdShowing) "Yükleniyor..." else "🎬 Reklam İzle, 5 Dk Erken Gelsin",
                                            color = Color.Black,
                                            fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    Text(
                                        text = displayInterpretation,
                                        color = Color.White,
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 18.sp,
                                        lineHeight = 28.sp,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(
    viewModel: GeneralReadingViewModel,
    onLoadingComplete: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        // Gemini yorum oluşturma durumunu kontrol et
        while (viewModel.isGeneratingReading) {
            delay(500)
        }
        
        // Hata varsa veya yorum oluşturulduysa loading'i bitir
        if (viewModel.readingError != null || viewModel.generatedReading != null) {
            delay(1000) // Kısa bir bekleme
            isLoading = false
            onLoadingComplete()
        } else {
            delay(2000) // Varsayılan bekleme
            isLoading = false
            onLoadingComplete()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.acilimlararkaplan),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(60.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Yorumunuz hazırlanıyor...",
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MeaningCard(
    text: String,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    enabled: Boolean = true
) {
    val borderColor = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f)
    val textColor = if (enabled) Color.White else Color.Gray
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = enabled),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier.padding(16.dp),
            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
            fontSize = 18.sp
        )
    }
}

@Composable
fun CardLayoutContainer(
    readingInfo: ReadingInfo,
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        when (readingInfo.layout) {
            CardLayout.SINGLE -> SingleCardLayout(drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize, forceRevealed)
            CardLayout.HORIZONTAL_3 -> HorizontalLayout(3, drawnCardMap, onDrawCard, onNavigateToCardDetail, maxWidthFraction = 0.65f, parentSize, forceRevealed)
            CardLayout.PYRAMID_3 -> Pyramid3Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize, forceRevealed)
            CardLayout.CROSS_5 -> Cross5Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize, forceRevealed)
            CardLayout.PYRAMID_6 -> Pyramid6Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize, forceRevealed)
            CardLayout.CROSS_7 -> Cross7Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize, forceRevealed)
            CardLayout.COMPATIBILITY_CROSS -> CompatibilityCrossLayout(drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize, forceRevealed)
            CardLayout.GRID_3x3 -> Grid3x3Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize, forceRevealed)
            CardLayout.PATH_5 -> Path5Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize, forceRevealed)
            CardLayout.WORK_PROBLEM_6 -> WorkProblemLayout(drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize, forceRevealed)
            CardLayout.FINANCIAL_4 -> FinancialLayout(drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize, forceRevealed)
            CardLayout.FINANCIAL_6 -> FinancialLayout(drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize, forceRevealed)
            // Diğer layout'lar için varsayılan
            else -> HorizontalLayout(readingInfo.cardCount, drawnCardMap, onDrawCard, onNavigateToCardDetail, parentSize = parentSize, forceRevealed = forceRevealed)
        }
    }
}

@Composable
private fun CardView(
    modifier: Modifier,
    cardState: ReadingCardState?,
    onDrawCard: () -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    Box(modifier = modifier) {
        if (cardState != null) {
            // forceRevealed true ise kartı zorla açık göster
            val modifiedCardState = if (forceRevealed) {
                cardState.copy(isRevealed = true)
            } else {
                cardState
            }
            
            AnimatedReadingCard(
                cardState = modifiedCardState,
                onCardClick = { onDrawCard() },
                onNavigateToCardDetail = onNavigateToCardDetail,
                modifier = Modifier.fillMaxSize(),
                parentSize = parentSize
            )
        } else {
            // Kart henüz çekilmemişse boş bir ReadingCardState ile AnimatedReadingCard kullan
            val emptyCardState = ReadingCardState(
                card = null,
                isRevealed = false,
                index = -1
            )
            AnimatedReadingCard(
                cardState = emptyCardState,
                onCardClick = { onDrawCard() },
                onNavigateToCardDetail = onNavigateToCardDetail,
                modifier = Modifier.fillMaxSize(),
                parentSize = parentSize
            )
        }
    }
}

@Composable
fun SingleCardLayout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    val cardModifier = Modifier
        .width(130.dp)
        .aspectRatio(0.7f)
    CardView(
        modifier = cardModifier,
        cardState = drawnCardMap[0],
        onDrawCard = { onDrawCard(0) },
        onNavigateToCardDetail = onNavigateToCardDetail,
        parentSize = parentSize,
        forceRevealed = forceRevealed
    )
}

@Composable
fun HorizontalLayout(
    cardCount: Int,
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    maxWidthFraction: Float = 1.0f,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(maxWidthFraction),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(cardCount) { index ->
            CardView(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.7f),
                cardState = drawnCardMap[index],
                onDrawCard = { onDrawCard(index) },
                onNavigateToCardDetail = onNavigateToCardDetail,
                parentSize = parentSize,
                forceRevealed = forceRevealed
            )
        }
    }
}

@Composable
fun Pyramid3Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    val cardModifier = Modifier
        .width(100.dp)
        .aspectRatio(0.7f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CardView(
            modifier = cardModifier,
            cardState = drawnCardMap[0],
            onDrawCard = { onDrawCard(0) },
            onNavigateToCardDetail = onNavigateToCardDetail,
            parentSize = parentSize,
            forceRevealed = forceRevealed
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardView(
                modifier = cardModifier,
                cardState = drawnCardMap[1],
                onDrawCard = { onDrawCard(1) },
                onNavigateToCardDetail = onNavigateToCardDetail,
                parentSize = parentSize,
                forceRevealed = forceRevealed
            )
            CardView(
                modifier = cardModifier,
                cardState = drawnCardMap[2],
                onDrawCard = { onDrawCard(2) },
                onNavigateToCardDetail = onNavigateToCardDetail,
                parentSize = parentSize,
                forceRevealed = forceRevealed
            )
        }
    }
}

@Composable
fun Cross5Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    val cardModifier = Modifier
        .width(85.dp)
        .aspectRatio(0.7f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail, parentSize, forceRevealed)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail, parentSize, forceRevealed)
            CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail, parentSize, forceRevealed)
            CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail, parentSize, forceRevealed)
        }
        CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail, parentSize, forceRevealed)
    }
}

@Composable
fun Pyramid6Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    Box(modifier = Modifier.padding(top = 16.dp), contentAlignment = Alignment.Center) {
        val cardModifier = Modifier
            .width(54.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. sıra (1 kart)
            CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail, parentSize, forceRevealed)
            // 2. sıra (2 kart)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail, parentSize, forceRevealed)
                CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail, parentSize, forceRevealed)
            }
            // 3. sıra (3 kart)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail, parentSize, forceRevealed)
                CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail, parentSize, forceRevealed)
                CardView(cardModifier, drawnCardMap[5], { onDrawCard(5) }, onNavigateToCardDetail, parentSize, forceRevealed)
            }
        }
    }
}

@Composable
fun Cross7Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    val cardModifier = Modifier
        .width(80.dp)
        .aspectRatio(0.7f)
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail, parentSize, forceRevealed)
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)) {
            CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail, parentSize, forceRevealed)
            CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail, parentSize, forceRevealed)
            CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail, parentSize, forceRevealed)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)) {
            CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail, parentSize, forceRevealed)
            CardView(cardModifier, drawnCardMap[5], { onDrawCard(5) }, onNavigateToCardDetail, parentSize, forceRevealed)
            CardView(cardModifier, drawnCardMap[6], { onDrawCard(6) }, onNavigateToCardDetail, parentSize, forceRevealed)
        }
    }
}

@Composable
fun CompatibilityCrossLayout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    Box(modifier = Modifier.fillMaxSize().padding(top = 24.dp), contentAlignment = Alignment.Center) {
        val cardModifier = Modifier
            .width(43.dp)
            .aspectRatio(0.7f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Row 1: Card 1 (index 0)
            CardView(
                cardModifier,
                drawnCardMap[0],
                { onDrawCard(0) },
                onNavigateToCardDetail,
                parentSize,
                forceRevealed
            )

            // Row 2: Cards 2 & 3 (indices 1, 2)
            Row(horizontalArrangement = Arrangement.spacedBy(90.dp)) {
                CardView(
                    cardModifier,
                    drawnCardMap[1],
                    { onDrawCard(1) },
                    onNavigateToCardDetail,
                    parentSize,
                    forceRevealed
                )
                CardView(
                    cardModifier,
                    drawnCardMap[2],
                    { onDrawCard(2) },
                    onNavigateToCardDetail,
                    parentSize,
                    forceRevealed
                )
            }

            // Row 3: Card 4 (index 3)
            CardView(
                cardModifier,
                drawnCardMap[3],
                { onDrawCard(3) },
                onNavigateToCardDetail,
                parentSize,
                forceRevealed
            )

            // Row 4: Cards 5 & 6 (indices 4, 5)
            Row(horizontalArrangement = Arrangement.spacedBy(90.dp)) {
                CardView(
                    cardModifier,
                    drawnCardMap[4],
                    { onDrawCard(4) },
                    onNavigateToCardDetail,
                    parentSize,
                    forceRevealed
                )
                CardView(
                    cardModifier,
                    drawnCardMap[5],
                    { onDrawCard(5) },
                    onNavigateToCardDetail,
                    parentSize,
                    forceRevealed
                )
            }

            // Row 5: Card 7 (index 6)
            CardView(
                cardModifier,
                drawnCardMap[6],
                { onDrawCard(6) },
                onNavigateToCardDetail,
                parentSize,
                forceRevealed
            )
        }
    }
}

@Composable
fun Grid3x3Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    Box(modifier = Modifier.padding(top = 28.dp)) {
        val cardModifier = Modifier
            .width(58.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(3) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(3) { col ->
                        val index = row * 3 + col
                        CardView(
                            modifier = cardModifier,
                            cardState = drawnCardMap[index],
                            onDrawCard = { onDrawCard(index) },
                            onNavigateToCardDetail = onNavigateToCardDetail,
                            parentSize = parentSize,
                            forceRevealed = forceRevealed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Path5Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    Box(modifier = Modifier.padding(top = 16.dp)) {
        val cardModifier = Modifier
            .width(62.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. sıra (1 kart)
            CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail, parentSize, forceRevealed)
            // 2. sıra (1 kart)
            CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail, parentSize, forceRevealed)
            // 3. sıra (3 kart)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail, parentSize, forceRevealed)
                CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail, parentSize, forceRevealed)
                CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail, parentSize, forceRevealed)
            }
        }
    }
}

@Composable
fun WorkProblemLayout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    Box(modifier = Modifier.padding(top = 16.dp)) {
        val cardModifier = Modifier
            .width(48.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. sıra: 1 kart
            CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail, parentSize, forceRevealed)
            
            // 2. sıra: 4 kart
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail, parentSize, forceRevealed)
                CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail, parentSize, forceRevealed)
                CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail, parentSize, forceRevealed)
                CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail, parentSize, forceRevealed)
            }
            
            // 3. sıra: 1 kart
            CardView(cardModifier, drawnCardMap[5], { onDrawCard(5) }, onNavigateToCardDetail, parentSize, forceRevealed)
        }
    }
}

@Composable
fun FinancialLayout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    parentSize: IntSize = IntSize.Zero,
    forceRevealed: Boolean = false
) {
    Box(modifier = Modifier.padding(top = 16.dp)) {
        val cardModifier = Modifier
            .width(58.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. sıra: 1 kart
            CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail, parentSize, forceRevealed)
            // 2. sıra: 3 kart
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail, parentSize, forceRevealed)
                CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail, parentSize, forceRevealed)
                CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail, parentSize, forceRevealed)
            }
            // 3. sıra: 2 kart
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail, parentSize, forceRevealed)
                CardView(cardModifier, drawnCardMap[5], { onDrawCard(5) }, onNavigateToCardDetail, parentSize, forceRevealed)
            }
        }
    }
}

data class ReadingInfo(
    val cardCount: Int,
    val cardMeanings: List<String>,
    val layout: CardLayout
)

enum class CardLayout {
    SINGLE,
    HORIZONTAL_3,
    PYRAMID_3,
    CROSS_5,
    PYRAMID_6,
    CROSS_7,
    COMPATIBILITY_CROSS,
    GRID_3x3,
    HORIZONTAL_5,
    HORIZONTAL_6,
    HORIZONTAL_7,
    HORIZONTAL_9,
    CELTIC_CROSS_10,
    PATH_5,
    WORK_PROBLEM_6,
    FINANCIAL_4,
    FINANCIAL_6
}

fun getReadingInfo(readingType: String): ReadingInfo {
    return when (readingType.trim()) {
        "GÜNLÜK AÇILIM" -> ReadingInfo(3, listOf("Düşünce", "His", "Aksiyon"), CardLayout.HORIZONTAL_3)
        "TEK KART AÇILIMI" -> ReadingInfo(1, listOf("Günün Kartı"), CardLayout.SINGLE)
        "EVET – HAYIR AÇILIMI" -> ReadingInfo(1, listOf("Cevap"), CardLayout.SINGLE)
        "GEÇMİŞ, ŞİMDİ, GELECEK" -> ReadingInfo(3, listOf("Geçmiş", "Şimdi", "Gelecek"), CardLayout.HORIZONTAL_3)
        "DURUM, AKSİYON, SONUÇ" -> ReadingInfo(3, listOf("Durum", "Aksiyon", "Sonuç"), CardLayout.HORIZONTAL_3)
        "İLİŞKİ AÇILIMI" -> ReadingInfo(3, listOf("Sen", "O", "İlişkiniz"), CardLayout.HORIZONTAL_3)
        "UYUMLULUK AÇILIMI" -> ReadingInfo(7, listOf(
            "Duygusal uyumunuz",
            "Sizin istekleriniz",
            "Partnerinizin istekleri",
            "Fiziksel uyumunuz",
            "Farklılıklar",
            "Benzerlikler",
            "Mental uyumunuz"
        ), CardLayout.COMPATIBILITY_CROSS)
        "DETAYLI İLİŞKİ AÇILIMI" -> ReadingInfo(9, listOf(
            "Geçmişteki Düşünceler",
            "Mevcut Düşünceler",
            "Gelecekteki Düşünceler",
            "Geçmişteki Duygular",
            "Mevcut Duygular",
            "Gelecekteki Duygular",
            "Geçmişteki Eylemler",
            "Mevcut Eylemler",
            "Gelecekteki Eylemler/Sonuç"
        ), CardLayout.GRID_3x3)
        "MÜCADELELER AÇILIMI" -> ReadingInfo(7, listOf(
            "Dış etkiler",
            "İlişkinizdeki problemde sizin rolünüz",
            "Problemde partnerinizin rolü",
            "İlişkiniz kurtarılabilir mi?",
            "Sizin için tavsiye",
            "Partneriniz için tavsiye",
            "İlişkiniz kurtarılmaya değer mi?"
        ), CardLayout.COMPATIBILITY_CROSS)
        "TAMAM MI, DEVAM MI" -> ReadingInfo(6, listOf(
            "İlişkinin Mevcut Durumu",
            "Devam Etmenin Potansiyeli",
            "Bitirmenin Potansiyeli",
            "İlişki devam ederse hissedecekleriniz",
            "Genel tavsiye",
            "İlişkiniz biterse hissedecekleriniz"
        ), CardLayout.PYRAMID_6)
        "GELECEĞİNE GİDEN YOL" -> ReadingInfo(5, listOf(
            "Hayalinizdeki kariyer",
            "Potansiyel yollar",
            "Yardımcı olabilecek kaynaklar",
            "Yetenekleriniz",
            "Dikkat etmeniz gerekenler"
        ), CardLayout.PATH_5)
        "İŞ YERİNDEKİ PROBLEMLER" -> ReadingInfo(6, listOf(
            "İşinizin Size Kazandırdıkları",
            "Sizi İşinizden Geri Çeken Etmenler",
            "Hedefiniz",
            "İşinizdeki Engel", 
            "Sizi İşinizde İleri İten Etmenler",
            "Gizli Kalmış Sebepler"
        ), CardLayout.WORK_PROBLEM_6)
        "FİNANSAL DURUM" -> ReadingInfo(6, listOf(
            "Kök Neden",
            "Karşıt Güçler",
            "Eylem Planı",
            "Destekleyici Güçler",
            "Yardım Kaynakları",
            "Olası Sonuç"
        ), CardLayout.FINANCIAL_6)
        else -> ReadingInfo(1, listOf("Kart"), CardLayout.SINGLE)
    }
}

// ==================== PREVIEW ====================

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun GeneralReadingInterpretationScreenPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Preview için gradient arka plan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A0A2E),
                            Color(0xFF2D1B4E),
                            Color(0xFF1A1A3E)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "←",
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "GÜNLÜK AÇILIM",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Kartlar alanı
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 3 açık kart örneği
                    repeat(3) { index ->
                        Card(
                            modifier = Modifier
                                .width(80.dp)
                                .aspectRatio(0.7f),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2D1B4E)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(2.dp, Color(0xFFFFD700))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = listOf("☀️", "🌙", "⭐")[index],
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = listOf("Güneş", "Ay", "Yıldız")[index],
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Yorum kutusu
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.6f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "GÜNLÜK AÇILIM Yorumu",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Düşünce: Güneş Kartı\n" +
                                "Bugün zihinsel olarak enerjik ve pozitif bir gün. Yeni başlangıçlar için ideal bir zaman.\n\n" +
                                "His: Ay Kartı\n" +
                                "Duygusal olarak içsel bir yolculuk. Sezgilerinizi dinleyin.\n\n" +
                                "Aksiyon: Yıldız Kartı\n" +
                                "Umut dolu adımlar atın. Hayallerinize doğru ilerleyin.",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
private fun LoadingScreenPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Preview için gradient arka plan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A0A2E),
                            Color(0xFF2D1B4E),
                            Color(0xFF1A1A3E)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Yorumunuz hazırlanıyor...",
                color = Color.White,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lütfen bekleyin",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun MeaningCardPreview() {
    Column(
        modifier = Modifier
            .background(Color(0xFF1A0A2E))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Seçili kart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.6f)
            ),
            border = BorderStroke(1.dp, Color(0xFFFFD700))
        ) {
            Text(
                text = "1. Düşünce (Seçili)",
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                fontSize = 18.sp
            )
        }

        // Normal kart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.6f)
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Text(
                text = "2. His",
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                fontSize = 18.sp
            )
        }

        // Devre dışı kart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.6f)
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
        ) {
            Text(
                text = "3. Aksiyon (Devre Dışı)",
                color = Color.Gray,
                modifier = Modifier.padding(16.dp),
                fontSize = 18.sp
            )
        }
    }
} 