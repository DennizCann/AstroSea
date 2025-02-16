package com.denizcan.astrosea.presentation.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.model.OnboardingPage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.denizcan.astrosea.R
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Tarot Falı",
            description = "Kartların mistik dünyasında gizlenen mesajları keşfedin ve sorularınızın cevaplarını bulun",
            imageRes = 0
        ),
        OnboardingPage(
            title = "Burç Yorumları",
            description = "Yıldızların size özel mesajlarını okuyun ve günlük burç yorumlarınızı öğrenin",
            imageRes = 0
        ),
        OnboardingPage(
            title = "Rün Falı",
            description = "Kadim rün sembollerinin bilgeliğiyle geleceğe ışık tutun ve yolunuzu aydınlatın",
            imageRes = 0
        )
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        while(true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % pages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.background_onboarding),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Sabit başlık kısmı
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Text(
                    text = "ASTROSEA",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = "Geçmiş, gelecek ve şimdi hakkında\nmerak ettiklerinizin cevabı...",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Onboarding içeriği
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    OnboardingPage(pages[page])
                }

                // Sayfa göstergeleri
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pages.size) { iteration ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(8.dp)
                                .background(
                                    color = if (pagerState.currentPage == iteration) {
                                        Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.3f)
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            // Atla butonu
            Button(
                onClick = onFinishOnboarding,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.7f)
                ),
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Text(
                    "ATLA",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
} 