package com.denizcan.astrosea.presentation.general

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralReadingDetailScreen(
    readingType: String,
    onNavigateBack: () -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: GeneralReadingViewModel = viewModel(
        key = "GeneralReadingViewModel_$readingType",
        factory = GeneralReadingViewModel.Factory(context)
    )
    
    LaunchedEffect(readingType) {
        viewModel.loadReadingState(readingType)
    }
    
    val readingInfo = remember(readingType) {
        getReadingInfo(readingType)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.acilimlararkaplan),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Scaffold(
            topBar = {
                AstroTopBar(
                    title = readingType,
                    onBackClick = onNavigateBack
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Çerçeve ve Kartlar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .weight(6.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.acilimlarsayfasitak),
                        contentDescription = "Çerçeve",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    // Kartların yerleşeceği alan
                    CardLayoutContainer(
                        readingInfo = readingInfo,
                        drawnCardMap = viewModel.drawnCards.associateBy { it.index },
                        onDrawCard = { index ->
                            viewModel.drawCardForPosition(readingType, index)
                        },
                        onNavigateToCardDetail = onNavigateToCardDetail
                    )
                }
                
                // Kart Anlamları ve Butonlar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .weight(3.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Kart Anlamları - Her zaman görünür
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(readingInfo.cardMeanings) { index, meaning ->
                            val card = viewModel.drawnCards.find { it.index == index }?.card
                            val isCardDrawn = card != null
                            val isCardRevealed = card != null && viewModel.drawnCards.find { it.index == index }?.isRevealed == true
                            
                            // Günlük açılım için özel mantık: kart açılmışsa ismini göster, açılmamışsa sadece anlamı göster
                            val displayText = if (readingType.trim() == "GÜNLÜK AÇILIM") {
                                if (isCardRevealed) {
                                    "${index + 1}. $meaning: ${card?.name ?: ""}"
                                } else {
                                    "${index + 1}. $meaning"
                                }
                            } else {
                                // Diğer açılımlar için normal mantık
                                "${index + 1}. $meaning: ${card?.name ?: "..."}"
                            }
                            
                            MeaningCard(
                                text = displayText,
                                onClick = { 
                                    if (isCardDrawn && isCardRevealed) {
                                        onNavigateToCardDetail(card.id)
                                    } else {
                                        // Kart henüz çekilmemişse veya açılmamışsa, tıklandığında çek
                                        viewModel.drawCardForPosition(readingType, index)
                                    }
                                },
                                isSelected = isCardRevealed,
                                enabled = true
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Yeniden Çek Butonu - Günlük açılım için inaktif
                    val isDailyReading = readingType.trim() == "GÜNLÜK AÇILIM"
                    Button(
                        onClick = { viewModel.resetAndDrawNew(readingType) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isDailyReading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDailyReading) Color.Gray.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (isDailyReading) "Günlük Açılım - Günde Bir Kez" else "Yeniden Çek",
                            color = if (isDailyReading) Color.Gray else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeaningCard(
    text: String,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    enabled: Boolean = true
) {
    val borderColor = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f)
    val textColor = if (enabled) Color.White else Color.Gray
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = enabled),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier.padding(16.dp),
            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
            fontSize = 18.sp
        )
    }
}

@Composable
fun CardLayoutContainer(
    readingInfo: ReadingInfo,
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        when (readingInfo.layout) {
            CardLayout.SINGLE -> SingleCardLayout(drawnCardMap, onDrawCard, onNavigateToCardDetail)
            CardLayout.HORIZONTAL_3 -> HorizontalLayout(3, drawnCardMap, onDrawCard, onNavigateToCardDetail, maxWidthFraction = 0.65f)
            CardLayout.PYRAMID_3 -> Pyramid3Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail)
            CardLayout.CROSS_5 -> Cross5Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail)
            CardLayout.PYRAMID_6 -> Pyramid6Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail)
            CardLayout.CROSS_7 -> Cross7Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail)
            CardLayout.COMPATIBILITY_CROSS -> CompatibilityCrossLayout(drawnCardMap, onDrawCard, onNavigateToCardDetail)
            CardLayout.GRID_3x3 -> Grid3x3Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail)
            CardLayout.PATH_5 -> Path5Layout(drawnCardMap, onDrawCard, onNavigateToCardDetail)
            CardLayout.WORK_PROBLEM_6 -> WorkProblemLayout(drawnCardMap, onDrawCard, onNavigateToCardDetail)
            CardLayout.FINANCIAL_4 -> FinancialLayout(drawnCardMap, onDrawCard, onNavigateToCardDetail)
            CardLayout.FINANCIAL_6 -> FinancialLayout(drawnCardMap, onDrawCard, onNavigateToCardDetail)
            // Diğer layout'lar için varsayılan
            else -> HorizontalLayout(readingInfo.cardCount, drawnCardMap, onDrawCard, onNavigateToCardDetail)
        }
    }
}

@Composable
private fun CardView(
    modifier: Modifier,
    cardState: ReadingCardState?,
    onDrawCard: () -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    Box(modifier = modifier) {
        if (cardState != null) {
            ReadingFlippableCard(
                cardState = cardState,
                onCardClick = {
                    if (cardState.isRevealed) {
                        onNavigateToCardDetail(cardState.card.id)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                contentDescription = "Kapalı Kart",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDrawCard() }
            )
        }
    }
}

@Composable
fun SingleCardLayout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    val cardModifier = Modifier
        .width(130.dp)
        .aspectRatio(0.7f)
    CardView(
        modifier = cardModifier,
        cardState = drawnCardMap[0],
        onDrawCard = { onDrawCard(0) },
        onNavigateToCardDetail = onNavigateToCardDetail
    )
}

@Composable
fun HorizontalLayout(
    cardCount: Int,
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit,
    maxWidthFraction: Float = 1.0f
) {
    Row(
        modifier = Modifier.fillMaxWidth(maxWidthFraction),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(cardCount) { index ->
            CardView(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.7f),
                cardState = drawnCardMap[index],
                onDrawCard = { onDrawCard(index) },
                onNavigateToCardDetail = onNavigateToCardDetail
            )
        }
    }
}

@Composable
fun Pyramid3Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    val cardModifier = Modifier
        .width(100.dp)
        .aspectRatio(0.7f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CardView(
            modifier = cardModifier,
            cardState = drawnCardMap[0],
            onDrawCard = { onDrawCard(0) },
            onNavigateToCardDetail = onNavigateToCardDetail
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardView(
                modifier = cardModifier,
                cardState = drawnCardMap[1],
                onDrawCard = { onDrawCard(1) },
                onNavigateToCardDetail = onNavigateToCardDetail
            )
            CardView(
                modifier = cardModifier,
                cardState = drawnCardMap[2],
                onDrawCard = { onDrawCard(2) },
                onNavigateToCardDetail = onNavigateToCardDetail
            )
        }
    }
}

@Composable
fun Cross5Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    val cardModifier = Modifier
        .width(85.dp)
        .aspectRatio(0.7f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail)
            CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail)
            CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail)
        }
        CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail)
    }
}

@Composable
fun Pyramid6Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    Box(modifier = Modifier.padding(top = 16.dp), contentAlignment = Alignment.Center) {
        val cardModifier = Modifier
            .width(54.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. sıra (1 kart)
            CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail)
            // 2. sıra (2 kart)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail)
                CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail)
            }
            // 3. sıra (3 kart)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail)
                CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail)
                CardView(cardModifier, drawnCardMap[5], { onDrawCard(5) }, onNavigateToCardDetail)
            }
        }
    }
}

@Composable
fun Cross7Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    val cardModifier = Modifier
        .width(80.dp)
        .aspectRatio(0.7f)
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail)
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)) {
            CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail)
            CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail)
            CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)) {
            CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail)
            CardView(cardModifier, drawnCardMap[5], { onDrawCard(5) }, onNavigateToCardDetail)
            CardView(cardModifier, drawnCardMap[6], { onDrawCard(6) }, onNavigateToCardDetail)
        }
    }
}

@Composable
fun CompatibilityCrossLayout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(top = 24.dp), contentAlignment = Alignment.Center) {
        val cardModifier = Modifier
            .width(43.dp)
            .aspectRatio(0.7f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Row 1: Card 1 (index 0)
            CardView(
                cardModifier,
                drawnCardMap[0],
                { onDrawCard(0) },
                onNavigateToCardDetail
            )

            // Row 2: Cards 2 & 3 (indices 1, 2)
            Row(horizontalArrangement = Arrangement.spacedBy(90.dp)) {
                CardView(
                    cardModifier,
                    drawnCardMap[1],
                    { onDrawCard(1) },
                    onNavigateToCardDetail
                )
                CardView(
                    cardModifier,
                    drawnCardMap[2],
                    { onDrawCard(2) },
                    onNavigateToCardDetail
                )
            }

            // Row 3: Card 4 (index 3)
            CardView(
                cardModifier,
                drawnCardMap[3],
                { onDrawCard(3) },
                onNavigateToCardDetail
            )

            // Row 4: Cards 5 & 6 (indices 4, 5)
            Row(horizontalArrangement = Arrangement.spacedBy(90.dp)) {
                CardView(
                    cardModifier,
                    drawnCardMap[4],
                    { onDrawCard(4) },
                    onNavigateToCardDetail
                )
                CardView(
                    cardModifier,
                    drawnCardMap[5],
                    { onDrawCard(5) },
                    onNavigateToCardDetail
                )
            }

            // Row 5: Card 7 (index 6)
            CardView(
                cardModifier,
                drawnCardMap[6],
                { onDrawCard(6) },
                onNavigateToCardDetail
            )
        }
    }
}

@Composable
fun Grid3x3Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    Box(modifier = Modifier.padding(top = 28.dp)) {
        val cardModifier = Modifier
            .width(58.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(3) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(3) { col ->
                        val index = row * 3 + col
                        CardView(
                            modifier = cardModifier,
                            cardState = drawnCardMap[index],
                            onDrawCard = { onDrawCard(index) },
                            onNavigateToCardDetail = onNavigateToCardDetail
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Path5Layout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    Box(modifier = Modifier.padding(top = 16.dp)) {
        val cardModifier = Modifier
            .width(62.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. sıra (1 kart)
            CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail)
            // 2. sıra (1 kart)
            CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail)
            // 3. sıra (3 kart)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail)
                CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail)
                CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail)
            }
        }
    }
}

@Composable
fun WorkProblemLayout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    Box(modifier = Modifier.padding(top = 16.dp)) {
        val cardModifier = Modifier
            .width(48.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. sıra: 1 kart
            CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail)
            
            // 2. sıra: 4 kart
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail)
                CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail)
                CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail)
                CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail)
            }
            
            // 3. sıra: 1 kart
            CardView(cardModifier, drawnCardMap[5], { onDrawCard(5) }, onNavigateToCardDetail)
        }
    }
}

@Composable
fun FinancialLayout(
    drawnCardMap: Map<Int, ReadingCardState>,
    onDrawCard: (Int) -> Unit,
    onNavigateToCardDetail: (String) -> Unit
) {
    Box(modifier = Modifier.padding(top = 16.dp)) {
        val cardModifier = Modifier
            .width(58.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. sıra: 1 kart
            CardView(cardModifier, drawnCardMap[0], { onDrawCard(0) }, onNavigateToCardDetail)
            // 2. sıra: 3 kart
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[1], { onDrawCard(1) }, onNavigateToCardDetail)
                CardView(cardModifier, drawnCardMap[2], { onDrawCard(2) }, onNavigateToCardDetail)
                CardView(cardModifier, drawnCardMap[3], { onDrawCard(3) }, onNavigateToCardDetail)
            }
            // 3. sıra: 2 kart
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, drawnCardMap[4], { onDrawCard(4) }, onNavigateToCardDetail)
                CardView(cardModifier, drawnCardMap[5], { onDrawCard(5) }, onNavigateToCardDetail)
            }
        }
    }
}

data class ReadingInfo(
    val cardCount: Int,
    val cardMeanings: List<String>,
    val layout: CardLayout
)

enum class CardLayout {
    SINGLE,
    HORIZONTAL_3,
    PYRAMID_3,
    CROSS_5,
    PYRAMID_6,
    CROSS_7,
    COMPATIBILITY_CROSS,
    GRID_3x3,
    HORIZONTAL_5,
    HORIZONTAL_6,
    HORIZONTAL_7,
    HORIZONTAL_9,
    CELTIC_CROSS_10,
    PATH_5,
    WORK_PROBLEM_6,
    FINANCIAL_4,
    FINANCIAL_6
}

fun getReadingInfo(readingType: String): ReadingInfo {
    return when (readingType.trim()) {
        "GÜNLÜK AÇILIM" -> ReadingInfo(3, listOf("Düşünce", "His", "Aksiyon"), CardLayout.HORIZONTAL_3)
        "TEK KART AÇILIMI" -> ReadingInfo(1, listOf("Günün Kartı"), CardLayout.SINGLE)
        "EVET – HAYIR AÇILIMI" -> ReadingInfo(1, listOf("Cevap"), CardLayout.SINGLE)
        "GEÇMİŞ, ŞİMDİ, GELECEK" -> ReadingInfo(3, listOf("Geçmiş", "Şimdi", "Gelecek"), CardLayout.HORIZONTAL_3)
        "DURUM, AKSİYON, SONUÇ" -> ReadingInfo(3, listOf("Durum", "Aksiyon", "Sonuç"), CardLayout.HORIZONTAL_3)
        "İLİŞKİ AÇILIMI" -> ReadingInfo(3, listOf("Sen", "O", "İlişkiniz"), CardLayout.HORIZONTAL_3)
        "UYUMLULUK AÇILIMI" -> ReadingInfo(7, listOf(
            "Duygusal uyumunuz",
            "Sizin istekleriniz",
            "Partnerinizin istekleri",
            "Fiziksel uyumunuz",
            "Farklılıklar",
            "Benzerlikler",
            "Mental uyumunuz"
        ), CardLayout.COMPATIBILITY_CROSS)
        "DETAYLI İLİŞKİ AÇILIMI" -> ReadingInfo(9, listOf(
            "Geçmişteki Düşünceler",
            "Mevcut Düşünceler",
            "Gelecekteki Düşünceler",
            "Geçmişteki Duygular",
            "Mevcut Duygular",
            "Gelecekteki Duygular",
            "Geçmişteki Eylemler",
            "Mevcut Eylemler",
            "Gelecekteki Eylemler/Sonuç"
        ), CardLayout.GRID_3x3)
        "MÜCADELELER AÇILIMI" -> ReadingInfo(7, listOf(
            "Dış etkiler",
            "İlişkinizdeki problemde sizin rolünüz",
            "Problemde partnerinizin rolü",
            "İlişkiniz kurtarılabilir mi?",
            "Sizin için tavsiye",
            "Partneriniz için tavsiye",
            "İlişkiniz kurtarılmaya değer mi?"
        ), CardLayout.COMPATIBILITY_CROSS)
        "TAMAM MI, DEVAM MI" -> ReadingInfo(6, listOf(
            "İlişkinin Mevcut Durumu",
            "Devam Etmenin Potansiyeli",
            "Bitirmenin Potansiyeli",
            "İlişki devam ederse hissedecekleriniz",
            "İlişkiniz biterse hissedecekleriniz",
            "Genel tavsiye"
        ), CardLayout.PYRAMID_6)
        "GELECEĞİNE GİDEN YOL" -> ReadingInfo(5, listOf(
            "Hayalinizdeki kariyer",
            "Potansiyel yollar",
            "Yetenekleriniz",
            "Yardımcı olabilecek kaynaklar",
            "Dikkat etmeniz gerekenler"
        ), CardLayout.PATH_5)
        "İŞ YERİNDEKİ PROBLEMLER" -> ReadingInfo(6, listOf(
            "Hedefiniz",
            "İşinizdeki Engel", 
            "Sizi İşinizden Geri Çeken Etmenler",
            "Sizi İşinizde İleri İten Etmenler",
            "İşinizin Size Kazandırdıkları",
            "Gizli Kalmış Sebepler"
        ), CardLayout.WORK_PROBLEM_6)
        "FİNANSAL DURUM" -> ReadingInfo(6, listOf(
            "Kök Neden",
            "Karşıt Güçler",
            "Eylem Planı",
            "Destekleyici Güçler",
            "Yardım Kaynakları",
            "Olası Sonuç"
        ), CardLayout.FINANCIAL_6)
        else -> ReadingInfo(1, listOf("Kart"), CardLayout.SINGLE)
    }
} 