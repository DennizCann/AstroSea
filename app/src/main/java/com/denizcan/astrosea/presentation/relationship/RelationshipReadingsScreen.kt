package com.denizcan.astrosea.presentation.relationship

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipReadingsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToGeneralReadings: () -> Unit,
    onNavigateToCareerReadings: () -> Unit,
    onNavigateToReadingDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: RelationshipReadingViewModel = viewModel(factory = RelationshipReadingViewModel.Factory(context))
    
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
                    title = "İLİŞKİ AÇILIMI",
                    onBackClick = onNavigateToHome,
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
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Açılım kartları - scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. İlişki Açılımı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onNavigateToReadingDetail("İLİŞKİ AÇILIMI") }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.width(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(3) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier
                                                .width(14.dp)
                                                .height(21.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "İLİŞKİ AÇILIMI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "İlişkinizde yaşanan güncel durumları gösteren temel açılım. Geçmiş, şimdi ve gelecekte ilişkinin durumunu anlamak için kullanılır.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    // 2. Uyumluluk Açılımı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onNavigateToReadingDetail("UYUMLULUK AÇILIMI") }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.width(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.width(56.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 1. satır: 1 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(10.dp).height(15.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    // 2. satır: 2 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        repeat(2) {
                                            Image(
                                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                                contentDescription = "Kart Arkası",
                                                modifier = Modifier.width(10.dp).height(15.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                    }
                                    // 3. satır: 1 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(10.dp).height(15.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    // 4. satır: 2 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        repeat(2) {
                                            Image(
                                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                                contentDescription = "Kart Arkası",
                                                modifier = Modifier.width(10.dp).height(15.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                    }
                                    // 5. satır: 1 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(10.dp).height(15.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "UYUMLULUK AÇILIMI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Karşınızdaki insanla gerçekte ne kadar uyumlusunuz? Duygu, düşünce ve fiziksel uyumunuzu gösteren 7 kartlık açılım.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    // 3. Detaylı İlişki Açılımı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onNavigateToReadingDetail("DETAYLI İLİŞKİ AÇILIMI") }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.width(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.width(56.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    repeat(3) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            repeat(3) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                                    contentDescription = "Kart Arkası",
                                                    modifier = Modifier.width(14.dp).height(21.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                        }
                                        if (it < 2) Spacer(modifier = Modifier.height(2.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "DETAYLI İLİŞKİ AÇILIMI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Kalp, düşünce ve aksiyon hanelerini içeren ve geçmiş, şimdi ve gelecek ekseninde yorumlanan detaylı açılım. 9 kart ile ilişkinin tüm boyutlarını analiz eder.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    // 4. Mücadeleler Açılımı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onNavigateToReadingDetail("MÜCADELELER AÇILIMI") }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.width(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.width(56.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 1. satır: 1 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(10.dp).height(15.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    // 2. satır: 2 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        repeat(2) {
                                            Image(
                                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                                contentDescription = "Kart Arkası",
                                                modifier = Modifier.width(10.dp).height(15.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                    }
                                    // 3. satır: 1 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(10.dp).height(15.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    // 4. satır: 2 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        repeat(2) {
                                            Image(
                                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                                contentDescription = "Kart Arkası",
                                                modifier = Modifier.width(10.dp).height(15.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                    }
                                    // 5. satır: 1 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(10.dp).height(15.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "MÜCADELELER AÇILIMI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "İlişki içerisindeki tartışma ve zorlukları inceleyen ve çözümler öneren 7 kartlık açılım.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    // 5. Tamam mı, Devam mı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onNavigateToReadingDetail("TAMAM MI, DEVAM MI") }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.width(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.width(56.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 1. satır: 1 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(10.dp).height(15.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    // 2. satır: 2 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        repeat(2) {
                                            Image(
                                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                                contentDescription = "Kart Arkası",
                                                modifier = Modifier.width(10.dp).height(15.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                    }
                                    // 3. satır: 3 kart
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        repeat(3) {
                                            Image(
                                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                                contentDescription = "Kart Arkası",
                                                modifier = Modifier.width(10.dp).height(15.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "TAMAM MI, DEVAM MI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bazı durumlar ve kişiler değişmez. Peki artık bu ilişki için çabalamalı mı, yoksa bitmesine izin mi vermeli? 6 kart ile ilişkinin devam edip etmeyeceğine dair rehberlik sunar.",
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

                // Alt navigasyon butonları
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Genel Açılımlar Tab
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = onNavigateToGeneralReadings
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "GENEL AÇILIMLAR",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                    fontSize = 16.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
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
                        onClick = onNavigateToCareerReadings
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
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
} 