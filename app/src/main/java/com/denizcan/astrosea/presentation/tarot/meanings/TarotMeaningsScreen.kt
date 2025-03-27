package com.denizcan.astrosea.presentation.tarot.meanings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.denizcan.astrosea.presentation.tarot.meanings.components.TarotCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotMeaningsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TarotMeaningsViewModel
) {
    var selectedType by remember { mutableStateOf("tarot") } // tarot veya rune
    var selectedSuit by remember { mutableStateOf("all") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli
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
                            modifier = Modifier.padding(start = 32.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            shadowElevation = 8.dp
                        ) {
                            Text(
                                text = "Tüm Anlamlar",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.titleLarge
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

            // Tarot/Rün seçimi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tarot",
                        fontSize = 26.sp,
                        color = if (selectedType == "tarot") Color.White else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.clickable { selectedType = "tarot" }
                    )
                    Box(
                        modifier = Modifier
                            .width(if (selectedType == "tarot") 60.dp else 15.dp)
                            .height(2.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rün",
                        fontSize = 26.sp,
                        color = if (selectedType == "rune") Color.White else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.clickable { selectedType = "rune" }
                    )
                    Box(
                        modifier = Modifier
                            .width(if (selectedType == "rune") 45.dp else 12.dp)
                            .height(2.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }
            }

            // Tarot kartları için filtre seçenekleri
            if (selectedType == "tarot") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Hepsi seçeneği
                        Text(
                            text = "Hepsi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSuit == "all") Color.Black else Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { selectedSuit = "all" }
                        )

                        // Major ikonu
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = "Major",
                            tint = if (selectedSuit == "major") Color.Black else Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { selectedSuit = "major" }
                        )

                        // Kupa ikonu
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cup),
                            contentDescription = "Kupalar",
                            tint = if (selectedSuit == "cups") Color.Black else Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { selectedSuit = "cups" }
                        )

                        // Kılıç ikonu
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sword),
                            contentDescription = "Kılıçlar",
                            tint = if (selectedSuit == "swords") Color.Black else Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { selectedSuit = "swords" }
                        )

                        // Tılsım ikonu
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pentacle),
                            contentDescription = "Tılsımlar",
                            tint = if (selectedSuit == "pentacles") Color.Black else Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { selectedSuit = "pentacles" }
                        )

                        // Değnek ikonu
                        Icon(
                            painter = painterResource(id = R.drawable.ic_wand),
                            contentDescription = "Değnekler",
                            tint = if (selectedSuit == "wands") Color.Black else Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { selectedSuit = "wands" }
                        )
                    }
                }
            } else {
                // Rün filtreleri
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "Tüm Rünler",
                        fontSize = 16.sp,
                        color = if (selectedSuit == "all") Color.White else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.clickable { selectedSuit = "all" }
                    )
                }
            }

            // Filtrelenmiş kartlar/rünler
            val filteredItems = when (selectedType) {
                "tarot" -> viewModel.cards.filter { card ->
                    when (selectedSuit) {
                        "all" -> true
                        "major" -> card.type == "major"
                        "cups" -> card.type == "minor" && card.suit == "cups"
                        "swords" -> card.type == "minor" && card.suit == "swords"
                        "pentacles" -> card.type == "minor" && card.suit == "pentacles"
                        "wands" -> card.type == "minor" && card.suit == "wands"
                        else -> true
                    }
                }
                "rune" -> emptyList() // Rünler için ayrı bir liste oluşturulacak
                else -> emptyList()
            }

            // Grid görünümü
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredItems) { item ->
                    TarotCard(
                        card = item,
                        onClick = { viewModel.onCardClick(item) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryIcon(
    iconRes: Int,
    text: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CategoryButton(
    text: String,
    icon: Int?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White.copy(0.2f) else Color.Transparent
        ),
        border = BorderStroke(1.dp, Color.White.copy(0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Text(text = text, color = Color.White)
        }
    }
}

@Composable
private fun FilterOption(
    icon: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        modifier = Modifier
            .size(32.dp)
            .clickable(onClick = onClick),
        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
    )
}

@Composable
private fun TextFilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun PlaceholderCardItem(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(0.6f)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1A1A1A).copy(alpha = 0.3f) // Yarı saydam koyu arka plan
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Boş bir kutu olarak bırakıyoruz
        }
    }
} 