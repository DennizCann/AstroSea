package com.denizcan.astrosea.presentation.tarot.meanings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.model.TarotCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotDetailScreen(
    onNavigateBack: () -> Unit,
    cardId: String,
    viewModel: TarotMeaningsViewModel
) {
    val card = viewModel.cards.find { it.id == cardId }
    val context = LocalContext.current
    
    if (card == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Kart bulunamadı",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
        return
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.kartanlamlariarkaplan),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(card.name) },
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
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Kart görseli
                val imageName = card.imageResName
                    .replace("ace", "one")
                    .replace("_of_", "of")
                    .replace("_", "")
                    .lowercase()
                val imageResId = context.resources.getIdentifier(
                    imageName,
                    "drawable",
                    context.packageName
                )

                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = card.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Kart detayları
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Temsil ettiği burçlar
                        Text(
                            text = "Temsil Ettiği Burçlar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = card.zodiacSigns ?: "Bilgi bulunamadı",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Getirdiği haberler
                        Text(
                            text = "Getirdiği Haberler",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!card.predictions.isNullOrEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                card.predictions.forEach { prediction ->
                                    Text(
                                        text = "• $prediction",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Bilgi bulunamadı",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (card.keywords.isNotEmpty()) {
                            Text(
                                text = "Anahtar Kelimeler",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = card.keywords.joinToString(", "),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
} 