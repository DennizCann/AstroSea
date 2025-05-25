package com.denizcan.astrosea.presentation.tarot.meanings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denizcan.astrosea.model.TarotCard
import com.denizcan.astrosea.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import com.denizcan.astrosea.presentation.components.AstroTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotDetailScreen(
    onNavigateBack: () -> Unit,
    card: TarotCard
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Arka plan (düz renk veya başka bir görsel)
        Image(
            painter = painterResource(id = R.drawable.kartanlamlariarkaplan),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            // Üst Bar
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Surface(
                            modifier = Modifier.padding(start = 64.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            shadowElevation = 8.dp
                        ) {
                            Text(
                                text = card.name,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                    fontSize = 28.sp
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Çerçeve/tag görseli ve kartı birlikte ortala
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(320f / 420f)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    // Tag görseli, ekranın tamamını yatayda kaplar
                    Image(
                        painter = painterResource(id = R.drawable.kartanlamisayfasitag),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .matchParentSize(),
                        contentScale = ContentScale.FillWidth
                    )
                    // Kart görselini tam ortalamak için iç Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.52f)
                            .fillMaxHeight(0.80f)
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        val context = LocalContext.current
                        val imageName = card.imageResName
                            .replace("ace", "one")
                            .replace("_of_", "of")
                            .replace("_", "")
                            .lowercase()
                        val imageResId = remember(card.imageResName) {
                            context.resources.getIdentifier(imageName, "drawable", context.packageName)
                        }
                        if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = card.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .aspectRatio(140f / 220f),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ana Card içinde sadece metinler
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A2236).copy(alpha = 0.85f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Açıklama
                        Text(
                            text = card.description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                fontSize = 20.sp
                            ),
                            color = Color.White
                        )

                        // Getirdiği Haberler
                        Text(
                            text = "Kartın Getirdiği Haberler:",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                fontSize = 22.sp
                            ),
                            color = Color.White
                        )
                        card.predictions?.forEach {
                            Text(
                                text = "• $it",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                    fontSize = 20.sp
                                ),
                                color = Color.White
                            )
                        }

                        // Burçlar
                        Text(
                            text = "Burçlar: ${card.zodiacSigns}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                fontSize = 20.sp
                            ),
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
} 