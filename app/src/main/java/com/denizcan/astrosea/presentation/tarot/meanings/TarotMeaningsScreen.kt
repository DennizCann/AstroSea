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
import androidx.navigation.NavController
import com.denizcan.astrosea.model.TarotCard
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotMeaningsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TarotMeaningsViewModel,
    navController: NavController
) {
    var selectedType by remember { mutableStateOf("tarot") } // tarot veya rune
    var selectedSuit by remember { mutableStateOf("all") }

    // Mor tema rengi
    val selectedBg = Color(0xFF6C4AB6)
    val selectedContent = Color.White
    val unselectedContent = Color.Black.copy(alpha = 0.6f)

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
                            modifier = Modifier.padding(start = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            shadowElevation = 8.dp
                        ) {
                            Text(
                                text = "Tüm Anlamlar",
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
                        modifier = Modifier.clickable { selectedType = "tarot" },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily(Font(R.font.cinzel_regular))
                        )
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
                        modifier = Modifier.clickable { selectedType = "rune" },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily(Font(R.font.cinzel_regular))
                        )
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (selectedSuit == "all") selectedBg else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { selectedSuit = "all" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Hepsi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedSuit == "all") selectedContent else unselectedContent,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Major ikonu
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (selectedSuit == "major") selectedBg else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { selectedSuit = "major" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_crown),
                                contentDescription = "Major",
                                tint = if (selectedSuit == "major") selectedContent else unselectedContent,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Kupa ikonu
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (selectedSuit == "cups") selectedBg else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { selectedSuit = "cups" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cup),
                                contentDescription = "Kupalar",
                                tint = if (selectedSuit == "cups") selectedContent else unselectedContent,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Kılıç ikonu
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (selectedSuit == "swords") selectedBg else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { selectedSuit = "swords" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sword),
                                contentDescription = "Kılıçlar",
                                tint = if (selectedSuit == "swords") selectedContent else unselectedContent,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Tılsım ikonu
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (selectedSuit == "pentacles") selectedBg else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { selectedSuit = "pentacles" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_pentacle),
                                contentDescription = "Tılsımlar",
                                tint = if (selectedSuit == "pentacles") selectedContent else unselectedContent,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Değnek ikonu
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (selectedSuit == "wands") selectedBg else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { selectedSuit = "wands" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_wand),
                                contentDescription = "Değnekler",
                                tint = if (selectedSuit == "wands") selectedContent else unselectedContent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            } else {
                // Rün sekmesi seçiliyse bilgi ve yakında mesajı göster
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rünler, eski İskandinav ve Germen kültürlerinde kullanılan, sembolik anlamlar taşıyan kadim harflerdir. Fal ve spiritüel rehberlikte de kullanılır.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                            color = Color.White,
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Çok yakında rün anlamları burada olacak!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                            color = Color.White,
                            fontSize = 22.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Filtrelenmiş kartlar/rünler
            val filteredItems = when (selectedType) {
                "tarot" -> viewModel.cards.filter { card ->
                    when (selectedSuit) {
                        "all" -> true
                        "major" -> card.type == "major"
                        "cups" -> card.type.contains("Cups", ignoreCase = true)
                        "swords" -> card.type.contains("Swords", ignoreCase = true)
                        "pentacles" -> card.type.contains("Pentacles", ignoreCase = true)
                        "wands" -> card.type.contains("Wands", ignoreCase = true)
                        else -> true
                    }
                }
                "rune" -> emptyList() // Rünler için ayrı bir liste oluşturulacak
                else -> emptyList()
            }

            // Grid görünümü
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredItems) { item ->
                    TarotCard(
                        card = item,
                        onClick = {
                            navController.navigate("tarot_detail/${item.id}")
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
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