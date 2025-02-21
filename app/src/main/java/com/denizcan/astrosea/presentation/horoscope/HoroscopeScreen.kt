package com.denizcan.astrosea.presentation.horoscope

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoroscopeScreen(
    onNavigateBack: () -> Unit,
    onZodiacClick: (String) -> Unit = {} // Burç detayına gitmek için
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
                    title = "Burç Yorumu",
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
                items(zodiacSigns) { zodiac ->
                    ZodiacCard(
                        name = zodiac.name,
                        date = zodiac.date,
                        onClick = { onZodiacClick(zodiac.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ZodiacCard(
    name: String,
    date: String,
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
                    text = date,
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

private data class ZodiacSign(
    val name: String,
    val date: String
)

private val zodiacSigns = listOf(
    ZodiacSign("Koç", "21 Mart - 20 Nisan"),
    ZodiacSign("Boğa", "21 Nisan - 20 Mayıs"),
    ZodiacSign("İkizler", "21 Mayıs - 21 Haziran"),
    ZodiacSign("Yengeç", "22 Haziran - 22 Temmuz"),
    ZodiacSign("Aslan", "23 Temmuz - 22 Ağustos"),
    ZodiacSign("Başak", "23 Ağustos - 22 Eylül"),
    ZodiacSign("Terazi", "23 Eylül - 22 Ekim"),
    ZodiacSign("Akrep", "23 Ekim - 21 Kasım"),
    ZodiacSign("Yay", "22 Kasım - 21 Aralık"),
    ZodiacSign("Oğlak", "22 Aralık - 19 Ocak"),
    ZodiacSign("Kova", "20 Ocak - 18 Şubat"),
    ZodiacSign("Balık", "19 Şubat - 20 Mart")
) 