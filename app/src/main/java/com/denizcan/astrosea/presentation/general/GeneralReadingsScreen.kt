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
    // İlk eleman navigasyon/veri anahtarıdır; ekranda gösterilen ad ve açıklama ReadingTexts ile çevrilir.
    val readings = listOf(
        "GÜNLÜK AÇILIM" to 3,
        "TEK KART AÇILIMI" to 1,
        "EVET – HAYIR AÇILIMI" to 1,
        "GEÇMİŞ, ŞİMDİ, GELECEK" to 3,
        "DURUM, AKSİYON, SONUÇ" to 3
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
                    title = androidx.compose.ui.res.stringResource(R.string.title_general_readings),
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
                    readings.forEachIndexed { idx, (title, _) ->
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
                                            contentDescription = androidx.compose.ui.res.stringResource(R.string.cd_card_back),
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
                                        text = com.denizcan.astrosea.util.ReadingTexts.displayName(title),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                            fontSize = 18.sp
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = com.denizcan.astrosea.util.ReadingTexts.description(title),
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
                                text = androidx.compose.ui.res.stringResource(R.string.tab_relationship_readings),
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
                                text = androidx.compose.ui.res.stringResource(R.string.tab_career_reading),
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