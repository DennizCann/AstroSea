package com.denizcan.astrosea.presentation.introduction

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.denizcan.astrosea.R

data class IntroPage(
    val title: String,
    val description: String,
    val imageRes: Int,
    val primaryButtonText: String,
    val secondaryButtonText: String
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
            title = "Tarot Rehberliği",
            description = "78 tarot kartı ile günlük falınızı görün, ilişki, kariyer ve genel yaşam sorularınıza yanıt bulun. Her kart size özel mesajlar taşır.",
            imageRes = R.drawable.tarotacilimlariimage,
            primaryButtonText = "Tarot Falına Başla",
            secondaryButtonText = "İleri"
        ),
        IntroPage(
            title = "Burç Yorumları",
            description = "Günlük, haftalık ve aylık burç yorumlarınızı okuyun. Astrolojik hareketleri takip edin ve yıldızların size rehberlik etmesine izin verin.",
            imageRes = R.drawable.zodiac,
            primaryButtonText = "Burç Yorumunu Gör",
            secondaryButtonText = "İleri"
        ),
        IntroPage(
            title = "Rün Falı",
            description = "Eski Viking bilgeliği ile geleceğinize ışık tutun. Rün taşları size yol göstersin, hayatınızdaki önemli kararları desteklesin.",
            imageRes = R.drawable.rune,
            primaryButtonText = "Rün Falına Bak",
            secondaryButtonText = "İleri"
        ),
        IntroPage(
            title = "Doğum Haritanız",
            description = "Doğum tarihiniz ve yerinize göre kişisel astrolojik haritanızı keşfedin. Kişiliğinizi, yeteneklerinizi ve potansiyelinizi anlamlandırın.",
            imageRes = R.drawable.birthchart,
            primaryButtonText = "Haritamı Görüntüle",
            secondaryButtonText = "Başlayalım"
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
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A2236))
            ) {
                // Arka plan görseli
                Image(
                    painter = painterResource(id = R.drawable.acilimlararkaplan),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.2f
                )
                
                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
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
                                .padding(horizontal = 4.dp)
                                .size(
                                    width = if (index == currentPage) 40.dp else 12.dp,
                                    height = 12.dp
                                )
                                .clip(CircleShape)
                                .background(
                                    if (index == currentPage) 
                                        Color(0xFFFFD700) 
                                    else 
                                        Color.White.copy(alpha = 0.3f)
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
                        onPrimaryClick = {
                            when (page) {
                                0 -> onNavigateToTarotMeanings()
                                1 -> onNavigateToHoroscope()
                                2 -> onNavigateToPremium() // Rün için premium sayfasına yönlendir
                                3 -> onNavigateToBirthChart()
                            }
                        },
                        onSecondaryClick = {
                            if (page < pages.size - 1) {
                                currentPage++
                            } else {
                                onDismiss()
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Navigasyon butonları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Geri butonu
                    if (currentPage > 0) {
                        OutlinedButton(
                            onClick = { currentPage-- },
                            modifier = Modifier
                                .height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp,
                                brush = androidx.compose.ui.graphics.SolidColor(Color.White)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Geri",
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_bold)),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    
                    // Atla butonu
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            "Atla",
                            color = Color.White.copy(alpha = 0.7f),
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                            fontSize = 14.sp
                        )
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
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Başlık
        Text(
            text = introPage.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFFFFD700),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Görsel
        Card(
            modifier = Modifier
                .size(200.dp)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Image(
                painter = painterResource(id = introPage.imageRes),
                contentDescription = introPage.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Açıklama
        Text(
            text = introPage.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                fontSize = 16.sp,
                lineHeight = 22.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Primary Action Button
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = introPage.primaryButtonText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                    fontSize = 16.sp
                ),
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Secondary Action Button
        OutlinedButton(
            onClick = onSecondaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(Color.White)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = introPage.secondaryButtonText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_bold)),
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

