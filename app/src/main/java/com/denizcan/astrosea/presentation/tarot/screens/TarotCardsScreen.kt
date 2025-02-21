package com.denizcan.astrosea.presentation.tarot.screens

import TarotCard
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.presentation.tarot.viewmodel.TarotViewModel
import androidx.compose.ui.text.capitalize
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotCardsScreen(
    viewModel: TarotViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onCardClick: (TarotCard) -> Unit
) {
    val cards = viewModel.tarotCards.collectAsState()
    
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
                    title = "Tarot Kartları",
                    onBackClick = onNavigateBack
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Major Arkana
                item {
                    Text(
                        "Major Arkana",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(viewModel.getMajorArcana()) { card ->
                    TarotCardItem(card = card, onClick = { onCardClick(card) })
                }

                // Minor Arkana
                listOf("wands", "cups", "swords", "pentacles").forEach { suit ->
                    item {
                        Text(
                            suit.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(viewModel.getMinorArcana(suit)) { card ->
                        TarotCardItem(card = card, onClick = { onCardClick(card) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TarotCardItem(
    card: TarotCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = card.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Detay",
                tint = Color.White
            )
        }
    }
}

// Tarot kartları listeleri
private val majorArcana = listOf(
    "0 - Deli",
    "I - Sihirbaz",
    "II - Yüksek Rahibe",
    "III - İmparatoriçe",
    "IV - İmparator",
    "V - Hierofant",
    "VI - Aşıklar",
    "VII - Savaş Arabası",
    "VIII - Güç",
    "IX - Ermiş",
    "X - Kader Çarkı",
    "XI - Adalet",
    "XII - Asılı Adam",
    "XIII - Ölüm",
    "XIV - Denge",
    "XV - Şeytan",
    "XVI - Yıkılan Kule",
    "XVII - Yıldız",
    "XVIII - Ay",
    "XIX - Güneş",
    "XX - Yargı",
    "XXI - Dünya"
)

private val wands = listOf(
    "Asaların Ası",
    "Asaların İkilisi",
    "Asaların Üçlüsü",
    "Asaların Dörtlüsü",
    "Asaların Beşlisi",
    "Asaların Altılısı",
    "Asaların Yedilisi",
    "Asaların Sekizlisi",
    "Asaların Dokuzlusu",
    "Asaların Onlusu",
    "Asaların Uşağı",
    "Asaların Şövalyesi",
    "Asaların Kraliçesi",
    "Asaların Kralı"
)

private val cups = listOf(
    "Kupaların Ası",
    "Kupaların İkilisi",
    "Kupaların Üçlüsü",
    "Kupaların Dörtlüsü",
    "Kupaların Beşlisi",
    "Kupaların Altılısı",
    "Kupaların Yedilisi",
    "Kupaların Sekizlisi",
    "Kupaların Dokuzlusu",
    "Kupaların Onlusu",
    "Kupaların Uşağı",
    "Kupaların Şövalyesi",
    "Kupaların Kraliçesi",
    "Kupaların Kralı"
)

private val swords = listOf(
    "Kılıçların Ası",
    "Kılıçların İkilisi",
    "Kılıçların Üçlüsü",
    "Kılıçların Dörtlüsü",
    "Kılıçların Beşlisi",
    "Kılıçların Altılısı",
    "Kılıçların Yedilisi",
    "Kılıçların Sekizlisi",
    "Kılıçların Dokuzlusu",
    "Kılıçların Onlusu",
    "Kılıçların Uşağı",
    "Kılıçların Şövalyesi",
    "Kılıçların Kraliçesi",
    "Kılıçların Kralı"
)

private val pentacles = listOf(
    "Pentakllerin Ası",
    "Pentakllerin İkilisi",
    "Pentakllerin Üçlüsü",
    "Pentakllerin Dörtlüsü",
    "Pentakllerin Beşlisi",
    "Pentakllerin Altılısı",
    "Pentakllerin Yedilisi",
    "Pentakllerin Sekizlisi",
    "Pentakllerin Dokuzlusu",
    "Pentakllerin Onlusu",
    "Pentakllerin Uşağı",
    "Pentakllerin Şövalyesi",
    "Pentakllerin Kraliçesi",
    "Pentakllerin Kralı"
)