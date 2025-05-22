package com.denizcan.astrosea.presentation.tarot.meanings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denizcan.astrosea.model.TarotCard
import com.denizcan.astrosea.R
import androidx.compose.ui.platform.LocalContext

@Composable
fun TarotDetailScreen(
    onNavigateBack: () -> Unit,
    card: TarotCard
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Arka plan (düz renk veya başka bir görsel)
        Image(
            painter = painterResource(id = R.drawable.kartanlamlariarkaplan), // Arka plan görselini buraya ekleyin
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Ortalanmış çerçeve ve kart
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Kart adı başlığı en üstte
            Surface(
                color = Color(0xFF2B3A67).copy(alpha = 0.85f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )
            }
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
            // Açıklama kutusu
            Surface(
                color = Color(0xFF1A2236).copy(alpha = 0.85f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = card.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Getirdiği Haberler kutusu
            Surface(
                color = Color(0xFF1A2236).copy(alpha = 0.85f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kartın Getirdiği Haberler:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    card.predictions?.forEach {
                        Text(
                            text = "• $it",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Burçlar kutusu
            Surface(
                color = Color(0xFF2B3A67).copy(alpha = 0.85f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Burçlar: ${card.zodiacSigns}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
} 