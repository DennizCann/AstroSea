package com.denizcan.astrosea.presentation.tarot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.presentation.components.AstroTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTarotCards: () -> Unit,
    onNavigateToYesNo: () -> Unit,
    onNavigateToTarotSpreads: () -> Unit,
    onNavigateToCustomSpread: () -> Unit
) {
    Scaffold(
        topBar = {
            AstroTopBar(
                title = "Tarot Falı",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Bilgilendirme Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tarot Falı Hakkında",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Tarot kartları, yüzyıllardır insanların geleceği yorumlamak ve hayatlarındaki önemli konularda rehberlik almak için kullandıkları mistik bir araçtır. Her kart, farklı anlamlar ve mesajlar taşır. Sizin için en uygun tarot falı seçeneğini aşağıdan seçebilirsiniz.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Tarot Seçenekleri
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TarotOptionCard(
                        title = "Tarot\nKartları",
                        description = "Tarot kartlarının anlamları",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTarotCards
                    )
                    TarotOptionCard(
                        title = "Evet/Hayır",
                        description = "Tek soru için hızlı cevap",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToYesNo
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TarotOptionCard(
                        title = "Tarot\nAçılımları",
                        description = "Hazır tarot dizilimleri",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTarotSpreads
                    )
                    TarotOptionCard(
                        title = "Özel\nAçılım",
                        description = "Kendi açılımını oluştur",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCustomSpread
                    )
                }
            }

            // Yakında notu
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Text(
                    text = "Bu özellikler çok yakında aktif olacak!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun TarotOptionCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 