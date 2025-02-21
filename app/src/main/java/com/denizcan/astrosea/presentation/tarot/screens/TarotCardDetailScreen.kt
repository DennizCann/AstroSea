package com.denizcan.astrosea.presentation.tarot.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar
import com.denizcan.astrosea.presentation.tarot.viewmodel.TarotViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotCardDetailScreen(
    viewModel: TarotViewModel = viewModel(),
    cardId: String,
    onNavigateBack: () -> Unit
) {
    val cards = viewModel.tarotCards.collectAsState()
    val card = cards.value.find { it.id == cardId }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.anabackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AstroTopBar(
                    title = card?.name ?: "Kart Detayı",
                    onBackClick = onNavigateBack
                )
            }
        ) { paddingValues ->
            if (card != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Kart görseli
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        val imageResId = when(card.id) {
                            "fool" -> R.drawable.fool_card
                            "ace_of_wands" -> R.drawable.wands_ace
                            "ace_of_cups" -> R.drawable.cups_ace
                            "ace_of_swords" -> R.drawable.swords_ace
                            "ace_of_pentacles" -> R.drawable.pentacles_ace
                            else -> R.drawable.tarotkartiarkasikesimli // varsayılan görsel
                        }
                        
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = card.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Kart bilgileri
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Dik Anlamı
                            Text(
                                text = "Dik Anlamı",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = card.meaningUpright,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Divider(color = Color.White.copy(alpha = 0.3f))

                            // Ters Anlamı
                            Text(
                                text = "Ters Anlamı",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = card.meaningReversed,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Divider(color = Color.White.copy(alpha = 0.3f))

                            // Açıklama
                            Text(
                                text = "Açıklama",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = card.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            // Anahtar Kelimeler
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Anahtar Kelimeler",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    card.keywords.forEach { keyword ->
                                        SuggestionChip(
                                            onClick = { },
                                            label = {
                                                Text(
                                                    text = keyword,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White
                                                )
                                            },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = Color.White.copy(alpha = 0.2f)
                                            ),
                                            border = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 