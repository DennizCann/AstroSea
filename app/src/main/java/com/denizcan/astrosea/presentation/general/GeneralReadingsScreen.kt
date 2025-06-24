package com.denizcan.astrosea.presentation.general

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    onNavigateToHome: () -> Unit,
    onNavigateToRelationshipReadings: () -> Unit,
    onNavigateToCareerReading: () -> Unit,
    onNavigateToReadingDetail: (String) -> Unit
) {
    val readings = listOf(
        Triple("GÜNLÜK AÇILIM", "Günlük düşünce, hissiyat ve sürecin/konunun gidişatını görmek için yapılan kısa açılım.", 3),
        Triple("TEK KART AÇILIMI", "Gününüzün genel enerjilerini gösteren ve kısa tavsiyeler veren tek kartlık açılım.", 1),
        Triple("EVET – HAYIR AÇILIMI", "Aklınızdaki sorunun cevabı; evet mi, hayır mı?", 1),
        Triple("GEÇMİŞ, ŞİMDİ, GELECEK", "Geçmişte nasıldı, şimdi nasıl ve gelecekte nasıl sorularının cevaplarını veren açılım.", 3),
        Triple("DURUM, AKSİYON, SONUÇ", "Bir durum hakkında sürecin; sürecin/konunun sonuçlarını gösteren kısa açılım.", 3)
    )
    val cardArrangements = listOf(
        listOf(3), // Günlük Açılım
        listOf(1), // Tek Kart Açılımı
        listOf(1), // Evet – Hayır Açılımı
        listOf(3), // Geçmiş, Şimdi, Gelecek
        listOf(3)  // Durum, Aksiyon, Sonuç
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
                    onBackClick = onNavigateToHome
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    readings.forEachIndexed { idx, (title, desc, cardCount) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            onClick = { onNavigateToReadingDetail(title) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.width(48.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                                ) {
                                    repeat(cardArrangements[idx][0]) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier
                                                .width(12.dp)
                                                .height(21.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
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
                
                // Alt Tab Bar'lar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // İlişki Açılımları Tab
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = onNavigateToRelationshipReadings
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "İLİŞKİ AÇILIMLARI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                    fontSize = 16.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                    
                    // Kariyer Açılımı Tab
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = onNavigateToCareerReading
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "KARİYER AÇILIMI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                    fontSize = 16.sp
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