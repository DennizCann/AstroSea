package com.denizcan.astrosea.presentation.tarot.meanings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import com.denizcan.astrosea.util.TarotCard
import com.denizcan.astrosea.presentation.tarot.meanings.components.TarotCard
import com.denizcan.astrosea.util.JsonLoader
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import android.util.Log



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotMeaningsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TarotMeaningsViewModel
) {
    var selectedSuit by remember { mutableStateOf("all") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Üst Bar
            TopAppBar(
                title = { Text("Tarot Anlamları", color = Color.White) },
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

            // Filtre seçenekleri
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Sadece metin içeren "Hepsi" sekmesi
                FilterChip(
                    selected = selectedSuit == "all",
                    onClick = { selectedSuit = "all" },
                    label = { Text("Hepsi", fontSize = 12.sp) },
                    modifier = Modifier.height(32.dp)
                )

                // Sadece ikon içeren diğer sekmeler
                FilterChip(
                    selected = selectedSuit == "major",
                    onClick = { selectedSuit = "major" },
                    label = { }, // Boş label
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = "Major",
                            tint = if (selectedSuit == "major") Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )

                FilterChip(
                    selected = selectedSuit == "cups",
                    onClick = { selectedSuit = "cups" },
                    label = { }, // Boş label
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cup),
                            contentDescription = "Kupalar",
                            tint = if (selectedSuit == "cups") Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )

                FilterChip(
                    selected = selectedSuit == "swords",
                    onClick = { selectedSuit = "swords" },
                    label = { }, // Boş label
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sword),
                            contentDescription = "Kılıçlar",
                            tint = if (selectedSuit == "swords") Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )

                FilterChip(
                    selected = selectedSuit == "pentacles",
                    onClick = { selectedSuit = "pentacles" },
                    label = { }, // Boş label
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pentacle),
                            contentDescription = "Tılsımlar",
                            tint = if (selectedSuit == "pentacles") Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )

                FilterChip(
                    selected = selectedSuit == "wands",
                    onClick = { selectedSuit = "wands" },
                    label = { }, // Boş label
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_wand),
                            contentDescription = "Değnekler",
                            tint = if (selectedSuit == "wands") Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.height(32.dp)
                )
            }

            // Filtrelenmiş kartlar
            val filteredCards = viewModel.cards.filter { card ->
                when (selectedSuit) {
                    "all" -> true
                    "major" -> card.type == "major"
                    "cups" -> {
                        // Debug için
                        if (card.type == "minor") {
                            Log.d("TarotFilter", "Card: ${card.name}, Suit: ${card.suit}, Type: ${card.type}")
                        }
                        card.type == "minor" && card.suit == "cups"
                    }
                    "swords" -> card.type == "minor" && card.suit == "swords"
                    "pentacles" -> card.type == "minor" && card.suit == "pentacles"
                    "wands" -> card.type == "minor" && card.suit == "wands"
                    else -> true
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCards) { card ->
                    TarotCard(
                        card = card,
                        onClick = { viewModel.onCardClick(card) }
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