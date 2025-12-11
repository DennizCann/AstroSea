package com.denizcan.astrosea.presentation.introduction

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.denizcan.astrosea.R
import com.denizcan.astrosea.model.TarotCard
import com.denizcan.astrosea.util.JsonLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class IntroPage(
    val title: String,
    val description: String,
    val imageRes: Int,
    val primaryButtonText: String,
    val secondaryButtonText: String,
    val tip: String = ""
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IntroductionPopupScreen(
    onDismiss: () -> Unit,
    onNavigateToTarotMeanings: () -> Unit = {},
    onNavigateToHoroscope: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onNavigateToBirthChart: () -> Unit = {}
) {
    var currentPage by remember { mutableStateOf(0) }

    val pages = listOf(
        IntroPage(
            title = "TAROT AÇILIMLARI",
            description = "İpucu: Kaderinizi görmek için kartlara tıklayarak açabilirsiniz.",
            imageRes = R.drawable.tarotacilimlariimage,
            primaryButtonText = "Ücretsiz Denemeyi Başlat",
            secondaryButtonText = "Açılımı Yap",
            tip = "İPUCU: KADERİNİZİ GÖRMEK İÇİN KARTLARA TIKLAYARAK AÇABİLİRSİNİZ."
        ),
        IntroPage(
            title = "BURÇ YORUMLARI",
            description = "Evrensel öykünüzü görmek için burcunuzu yorumlayalım.",
            imageRes = R.drawable.zodiac,
            primaryButtonText = "Ücretsiz Denemeyi Başlat",
            secondaryButtonText = "Yorumu Gör",
            tip = "EVRENSEL ÖYKÜNÜZÜ GÖRMEK İÇİN BURCUNUZU YORUMLAYALIM."
        ),
        IntroPage(
            title = "DOĞUM HARİTASI",
            description = "Doğduğun an evren sana bir şifre fışıldadı: Duymaya hazır mısın?",
            imageRes = R.drawable.birthchart,
            primaryButtonText = "Ücretsiz Denemeyi Başlat",
            secondaryButtonText = "Haritanı Çiz",
            tip = "DOĞDUĞUN AN EVREN SANA BİR ŞİFRE FIŞILDADI: DUYMAYA HAZIR MISIN?"
        ),
        IntroPage(
            title = "RÜN FALI",
            description = "İpucu: Nors bilgeliğinin ışığının yolunu ve amacını aydınlatması için rüne tıkla ve sonucu gör",
            imageRes = R.drawable.rune,
            primaryButtonText = "Ücretsiz Denemeyi Başlat",
            secondaryButtonText = "Fal Bak",
            tip = "İPUCU: NORS BİLGELİĞİNİN IŞIĞININ YOLUNU VE AMACINI AYDINLATMASI İÇİN RÜNE TIKLA VE SONUCU GÖR"
        )
    )

    Dialog(
        onDismissRequest = { /* Boş bırak, sadece close butonu ile kapansın */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A0E27), // Koyu lacivert
                                Color(0xFF1A1F3A), // Orta lacivert-mor
                                Color(0xFF2D1B4E)  // Mor
                            )
                        )
                    )
            ) {
                // Arka plan görseli (yıldızlar)
                Image(
                    painter = painterResource(id = R.drawable.anabackground),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.3f
                )

                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(42.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Sayfa göstergesi
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(pages.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(
                                        width = if (index == currentPage) 32.dp else 8.dp,
                                        height = 8.dp
                                    )
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (index == currentPage)
                                            Color(0xFFD4AF37) // Altın
                                        else
                                            Color.White.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // İçerik
                    AnimatedContent(
                        targetState = currentPage,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { width -> width } + fadeIn() with
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> -width } + fadeIn() with
                                        slideOutHorizontally { width -> width } + fadeOut()
                            }.using(SizeTransform(clip = false))
                        },
                        label = "page_transition"
                    ) { page ->
                        IntroPageContent(
                            introPage = pages[page],
                            pageIndex = page,
                            onPrimaryClick = {
                                // Ücretsiz Denemeyi Başlat - hep premium'a gider
                                onNavigateToPremium()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Navigasyon butonları
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Geri butonu (ilk sayfadaysa görünmez)
                        if (currentPage > 0) {
                            TextButton(
                                onClick = { currentPage-- },
                                modifier = Modifier.height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Geri",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        // Atla butonu (sonraki sayfaya geçer, son sayfadaysa kapatır)
                        TextButton(
                            onClick = {
                                if (currentPage < pages.size - 1) {
                                    currentPage++
                                } else {
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(
                                "Atla",
                                color = Color.White.copy(alpha = 0.8f),
                                fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                fontSize = 15.sp
                            )
                            if (currentPage < pages.size - 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Atla",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroPageContent(
    introPage: IntroPage,
    pageIndex: Int,
    onPrimaryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Altın çerçeve ile görsel kartı
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A0F2E) // Koyu mor arka plan
            ),
            border = BorderStroke(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFD4AF37), // Altın
                        Color(0xFFFFD700), // Parlak altın
                        Color(0xFFB8860B), // Koyu altın
                        Color(0xFFD4AF37)  // Altın
                    )
                )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A0F2E), // Koyu mor
                                Color(0xFF2D1B4E), // Orta mor
                                Color(0xFF1A0F2E)  // Koyu mor
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Eğer Tarot sayfasıysa (index 0), 3 kartlı açılım göster
                if (pageIndex == 0) {
                    IntroTarotCards()
                } else {
                    // Diğer sayfalar için normal görsel
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        // Görsel
                        Image(
                            painter = painterResource(id = introPage.imageRes),
                            contentDescription = introPage.title,
                            modifier = Modifier
                                .size(180.dp)
                                .padding(vertical = 12.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Başlık
                        Text(
                            text = introPage.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = Color(0xFFD4AF37), // Altın rengi
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Açıklama
                        Text(
                            text = introPage.description.uppercase(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Primary Action Button (Ücretsiz Denemeyi Başlat)
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4A148C), // Koyu mor
                                Color(0xFF6A1B9A), // Orta mor
                                Color(0xFF8E24AA)  // Açık mor
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = introPage.primaryButtonText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

// 3 kartlı tanıtım tarot açılımı
@Composable
private fun IntroTarotCards() {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    
    // Tüm tarot kartlarını yükle
    val allTarotCards = remember {
        JsonLoader(context).loadTarotCards()
    }
    
    // 3 kart için state
    data class CardState(
        val drawnCard: TarotCard?,
        val isRevealed: Boolean,
        val isFlipping: Boolean = false
    )
    
    var cards by remember {
        mutableStateOf(
            listOf(
                CardState(null, false),
                CardState(null, false),
                CardState(null, false)
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Başlık
        Text(
            text = "TAROT AÇILIMLARI",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            ),
            color = Color(0xFFD4AF37),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 3 Tarot Kartı
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            cards.forEachIndexed { index, cardState ->
                IntroTarotCard(
                    cardState = cardState,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.7f),
                    onCardClick = {
                        if (!cardState.isRevealed && !cardState.isFlipping) {
                            scope.launch {
                                // Flip başlat
                                cards = cards.toMutableList().apply {
                                    this[index] = cardState.copy(isFlipping = true)
                                }
                                
                                // Daha önce çekilen kartların ID'lerini al
                                val usedCardIds = cards.mapNotNull { it.drawnCard?.id }
                                
                                // Henüz çekilmemiş kartlardan seç
                                val availableCards = allTarotCards.filter { it.id !in usedCardIds }
                                
                                // Rastgele benzersiz kart çek
                                val randomCard = if (availableCards.isNotEmpty()) {
                                    availableCards.shuffled().firstOrNull()
                                } else {
                                    // Tüm kartlar çekilmişse (olmamalı ama yine de)
                                    allTarotCards.randomOrNull()
                                }
                                
                                delay(100)
                                
                                // Kartı aç
                                cards = cards.toMutableList().apply {
                                    this[index] = CardState(
                                        drawnCard = randomCard,
                                        isRevealed = true,
                                        isFlipping = false
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Açıklama
        Text(
            text = "İPUCU: KADERİNİZİ GÖRMEK İÇİN\nKARTLARA TIKLAYARAK AÇABİLİRSİNİZ.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.5.sp
            ),
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// Tek bir tanıtım tarot kartı
@Composable
private fun IntroTarotCard(
    cardState: Any,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    // Basit bir şekilde Any tipinden değerleri çıkar
    val drawnCard: TarotCard? = try {
        cardState.javaClass.getDeclaredField("drawnCard").apply { isAccessible = true }.get(cardState) as? TarotCard
    } catch (e: Exception) {
        null
    }
    
    val isRevealed: Boolean = try {
        cardState.javaClass.getDeclaredField("isRevealed").apply { isAccessible = true }.get(cardState) as? Boolean ?: false
    } catch (e: Exception) {
        false
    }
    
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "card_rotation"
    )
    
    Box(
        modifier = modifier
            .clickable(onClick = onCardClick)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density.density
            }
    ) {
        if (rotation < 90f) {
            // Arka yüz (kapalı kart)
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                    contentDescription = "Kapalı Tarot Kartı",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
            // Ön yüz (açık kart)
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                if (drawnCard != null) {
                    val imageName = drawnCard.imageResName
                        .replace("ace", "one")
                        .replace("_of_", "of")
                        .replace("_", "")
                        .lowercase()
                    val imageResId = context.resources.getIdentifier(
                        imageName,
                        "drawable",
                        context.packageName
                    )
                    
                    if (imageResId != 0) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = drawnCard.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.placeholder_card),
                            contentDescription = "Kart",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                        contentDescription = "Kapalı Kart",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun IntroductionPopupScreenPreview() {
    IntroductionPopupScreen(
        onDismiss = {},
        onNavigateToTarotMeanings = {},
        onNavigateToHoroscope = {},
        onNavigateToPremium = {},
        onNavigateToBirthChart = {}
    )
}