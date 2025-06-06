package com.denizcan.astrosea.presentation.relationship

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipReadingsScreen(
    onNavigateBack: () -> Unit
) {
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
                    title = "İlişki Açılımları",
                    onBackClick = onNavigateBack
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. İlişki Açılımı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF1A2236).copy(alpha = 0.7f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kart dizilimi (3 kart yatay)
                        Row(
                            modifier = Modifier.width(80.dp),
                            horizontalArrangement = Arrangement.spacedBy((-16).dp)
                        ) {
                            repeat(3) {
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
                                text = "İLİŞKİ AÇILIMI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                    fontSize = 18.sp
                                ),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "İlişkinizde yaşanan güncel durumları gösteren temel açılım.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                    fontSize = 14.sp
                                ),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
                // 2. Uyumluluk Açılımı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF1A2236).copy(alpha = 0.7f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kart dizilimi (üstte 2, altta 2)
                        Column(
                            modifier = Modifier.width(80.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(2) {
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(2) {
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
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "UYUMLULUK AÇILIMI",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Karşınızdaki insanla gerçekte ne kadar uyumlusunuz? Duygu, düşünce ve fiziksel uyumunuzu gösteren açılım.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
                // 3. Detaylı İlişki Açılımı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF1A2236).copy(alpha = 0.7f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kart dizilimi (üstte 3, ortada 1, altta 3)
                        Column(
                            modifier = Modifier.width(80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(3) {
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Image(
                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                contentDescription = "Kart Arkası",
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(54.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(3) {
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
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "DETAYLI İLİŞKİ AÇILIMI",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kalp, düşünce ve aksiyon hanelerini içeren ve geçmiş, şimdi ve gelecek ekseninde yorumlanan detaylı açılım.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
                // 4. Mücadeleler Açılımı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF1A2236).copy(alpha = 0.7f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kart dizilimi (üstte 2, ortada 1, altta 2)
                        Column(
                            modifier = Modifier.width(80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(2) {
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Image(
                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                contentDescription = "Kart Arkası",
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(54.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(2) {
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
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "MÜCADELELER AÇILIMI",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "İlişki içerisindeki tartışma ve zorlukları inceleyen ve çözümler öneren detaylı açılım.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
                // 5. Tamam mı, Devam mı?
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF1A2236).copy(alpha = 0.7f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kart dizilimi (üstte 1, ortada 2, altta 1)
                        Column(
                            modifier = Modifier.width(80.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                contentDescription = "Kart Arkası",
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(54.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(2) {
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Image(
                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                contentDescription = "Kart Arkası",
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(54.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "TAMAM MI, DEVAM MI?",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bazı durumlar ve kişiler değişmez. Peki artık bu ilişki için çabalamalı mı, yoksa bitmesine izin mi vermeli?",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
            }
        }
    }
} 