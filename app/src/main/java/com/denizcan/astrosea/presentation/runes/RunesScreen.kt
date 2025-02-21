package com.denizcan.astrosea.presentation.runes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunesScreen(
    onNavigateBack: () -> Unit,
    onRuneClick: (String) -> Unit = {} // Rün detayına gitmek için
) {
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
                    title = "Rün Falı",
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
                items(runeSymbols) { rune ->
                    RuneCard(
                        name = rune.name,
                        meaning = rune.meaning,
                        onClick = { onRuneClick(rune.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RuneCard(
    name: String,
    meaning: String,
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
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Detay",
                tint = Color.White
            )
        }
    }
}

private data class RuneSymbol(
    val name: String,
    val meaning: String
)

private val runeSymbols = listOf(
    RuneSymbol("Fehu", "Zenginlik, Başarı"),
    RuneSymbol("Uruz", "Güç, Dayanıklılık"),
    RuneSymbol("Thurisaz", "Koruma, Savunma"),
    RuneSymbol("Ansuz", "İletişim, Bilgelik"),
    RuneSymbol("Raidho", "Yolculuk, Hareket"),
    RuneSymbol("Kenaz", "Işık, Bilgi"),
    RuneSymbol("Gebo", "Hediye, Ortaklık"),
    RuneSymbol("Wunjo", "Neşe, Mutluluk"),
    RuneSymbol("Hagalaz", "Değişim, Dönüşüm"),
    RuneSymbol("Nauthiz", "İhtiyaç, Zorluk"),
    RuneSymbol("Isa", "Durgunluk, Sabır"),
    RuneSymbol("Jera", "Hasat, Döngü"),
    RuneSymbol("Eihwaz", "Koruma, Savunma"),
    RuneSymbol("Perthro", "Gizem, Kader"),
    RuneSymbol("Algiz", "Koruma, Rehberlik"),
    RuneSymbol("Sowilo", "Güneş, Başarı"),
    RuneSymbol("Tiwaz", "Zafer, Adalet"),
    RuneSymbol("Berkana", "Büyüme, Yenilenme"),
    RuneSymbol("Ehwaz", "Hareket, İlerleme"),
    RuneSymbol("Mannaz", "İnsan, Benlik"),
    RuneSymbol("Laguz", "Akış, Sezgi"),
    RuneSymbol("Ingwaz", "İç Güç, Potansiyel"),
    RuneSymbol("Dagaz", "Gün Işığı, Aydınlanma"),
    RuneSymbol("Othala", "Miras, Ev")
)