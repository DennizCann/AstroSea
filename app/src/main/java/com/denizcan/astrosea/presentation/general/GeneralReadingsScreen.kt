package com.denizcan.astrosea.presentation.general

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralReadingsScreen(
    onNavigateBack: () -> Unit
) {
    val readings = listOf(
        Triple("GÜNLÜK AÇILIM", "Günlük düşünce, hissiyat ve sürecin/konunun gidişatını görmek için yapılan kısa açılım.", 3),
        Triple("TEK KART AÇILIMI", "Gününüzün genel enerjilerini gösteren ve kısa tavsiyeler veren tek kartlık açılım.", 1),
        Triple("EVET – HAYIR AÇILIMI", "Aklınızdaki sorunun cevabı; evet mi, hayır mı?", 1),
        Triple("GEÇMİŞ, ŞİMDİ, GELECEK", "Geçmişte nasıldı, şimdi nasıl ve gelecekte nasıl sorularının cevaplarını veren açılım.", 3),
        Triple("DURUM, AKSİYON, SONUÇ", "Bir durum hakkında sürecin; sürecin/konunun sonuçlarını gösteren kısa açılım.", 3)
    )
    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.acilimlararkaplan),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Scaffold(
            topBar = {
                AstroTopBar(
                    title = "Genel Açılımlar",
                    onBackClick = onNavigateBack,
                    titleStyle = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily(Font(R.font.cinzel_regular))
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                readings.forEach { (title, desc, cardCount) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.width(80.dp),
                                horizontalArrangement = Arrangement.spacedBy((-16).dp)
                            ) {
                                repeat(cardCount) {
                                    Image(
                                        painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                        contentDescription = "Kart Arkası",
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(54.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 