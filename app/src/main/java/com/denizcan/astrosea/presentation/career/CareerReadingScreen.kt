package com.denizcan.astrosea.presentation.career

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerReadingScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToGeneralReadings: () -> Unit,
    onNavigateToRelationshipReadings: () -> Unit,
    onNavigateToReadingDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: CareerReadingViewModel = viewModel(factory = CareerReadingViewModel.Factory(context))
    
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
                    title = "KARİYER AÇILIMI",
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
                // Açılım kartları
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Geleceğine Giden Yol
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onNavigateToReadingDetail("GELECEĞİNE GİDEN YOL") }
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
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(14.dp).height(21.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(14.dp).height(21.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
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
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "GELECEĞİNE GİDEN YOL",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "İstediğin geleceği biliyorsun, peki oraya nasıl ulaşacaksınız? Size yol haritası çizen açılım.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    // 2. İş Yerindeki Problemler
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onNavigateToReadingDetail("İŞ YERİNDEKİ PROBLEMLER") }
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
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(12.dp).height(18.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        repeat(4) {
                                            Image(
                                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                                contentDescription = "Kart Arkası",
                                                modifier = Modifier.width(12.dp).height(18.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(12.dp).height(18.dp),
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
                                    text = "İŞ YERİNDEKİ PROBLEMLER",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "İş yerinde karşılaştığınız problemlerin sebebini inceleyen açılım.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    // 3. Finansal Durum
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onNavigateToReadingDetail("FİNANSAL DURUM") }
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
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kart Arkası",
                                            modifier = Modifier.width(14.dp).height(21.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
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
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        repeat(2) {
                                            Image(
                                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                                contentDescription = "Kart Arkası",
                                                modifier = Modifier.width(14.dp).height(21.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "FİNANSAL DURUM",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Finansal durumunuzu gösteren ve neye ihtiyacınız olduğunu söyleyen açılım.",
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onNavigateToGeneralReadings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A5568)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Genel Açılımlar",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                color = Color.White
                            )
                        )
                    }
                    Button(
                        onClick = onNavigateToRelationshipReadings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A5568)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "İlişki Açılımları",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
} 