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
                        .fillMaxWidth(0.9f)
                        .weight(6f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.acilimlarsayfasitak),
                        contentDescription = "Çerçeve",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Kartların yerleşeceği alan
                        Row(
                            modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val drawnCardMap = viewModel.drawnCards.associateBy { it.index }

                        repeat(readingInfo.cardCount) { index ->
                            val cardState = drawnCardMap[index]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.7f)
                            ) {
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
                                Card(
                                    modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                viewModel.drawCardForPosition(
                                                    readingType,
                                                    index
                                                )
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                            contentDescription = "Kapalı Kart",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Kart Anlamları ve Butonlar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .weight(4f),
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
                            val cardName = card?.name ?: "..."
                            val isCardDrawn = card != null
                            
                            MeaningCard(
                                text = "${index + 1}. $meaning: $cardName",
                        onClick = { 
                                    if (isCardDrawn) {
                                        onNavigateToCardDetail(card.id)
                                    } else {
                                        // Kart henüz çekilmemişse, tıklandığında çek
                                        viewModel.drawCardForPosition(readingType, index)
                                    }
                                },
                                isSelected = isCardDrawn,
                                enabled = true
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Yeniden Çek Butonu - Her zaman görünür
                    Button(
                        onClick = { viewModel.resetAndDrawNew(readingType) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text("Yeniden Çek", color = Color.White)
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

data class ReadingInfo(val cardCount: Int, val cardMeanings: List<String>)

fun getReadingInfo(readingType: String): ReadingInfo {
    return when (readingType.trim()) {
        "GÜNLÜK AÇILIM" -> ReadingInfo(3, listOf("Düşünce", "His", "Aksiyon"))
        "TEK KART AÇILIMI" -> ReadingInfo(1, listOf("Günün Kartı"))
        "EVET – HAYIR AÇILIMI" -> ReadingInfo(1, listOf("Cevap"))
        "GEÇMİŞ, ŞİMDİ, GELECEK" -> ReadingInfo(3, listOf("Geçmiş", "Şimdi", "Gelecek"))
        "DURUM, AKSİYON, SONUÇ" -> ReadingInfo(3, listOf("Durum", "Aksiyon", "Sonuç"))
        "İLİŞKİ AÇILIMI" -> ReadingInfo(3, listOf("Sen", "O", "İlişkiniz"))
        "UYUMLULUK AÇILIMI" -> ReadingInfo(7, listOf("Senin Geçmişin", "Onun Geçmişi", "Sizin Uyumunuz", "Senin Beklentin", "Onun Beklentisi", "İlişkinin Geleceği", "Sonuç"))
        "DETAYLI İLİŞKİ AÇILIMI" -> ReadingInfo(9, listOf("Geçmiş", "Şimdi", "Gelecek", "Senin Bilinçaltın", "Onun Bilinçaltı", "Dış Etkenler", "Umutlar ve Korkular", "Potansiyel", "Nihai Sonuç"))
        "MÜCADELELER AÇILIMI" -> ReadingInfo(7, listOf("Ana Sorun", "Senin Bakış Açın", "Onun Bakış Açısı", "Geçmişin Etkisi", "Çözüm Önerisi", "Olası Gelecek", "Nihai Tavsiye"))
        "TAMAM MI, DEVAM MI" -> ReadingInfo(6, listOf("İlişkinin Temeli", "Mevcut Durum", "Devam Etme Potansiyeli", "Bitirme Potansiyeli", "Senin İçin En İyisi", "Nihai Karar"))
        "GELECEĞİNE GİDEN YOL" -> ReadingInfo(5, listOf("Mevcut Durumun", "Hedefin", "Engellerin", "Yardımcı Etkenler", "Atman Gereken Adım"))
        "İŞ YERİNDEKİ PROBLEMLER" -> ReadingInfo(6, listOf("Problemin Kökü", "Seni Etkileyen Faktör", "Diğerlerini Etkileyen Faktör", "Gözden Kaçırdığın", "Çözüm Yolu", "Sonuç"))
        "FİNANSAL DURUM" -> ReadingInfo(6, listOf("Mevcut Finansal Durum", "Para Akışın", "Engeller", "Fırsatlar", "Atman Gereken Adım", "Uzun Vadeli Sonuç"))
        else -> ReadingInfo(1, listOf("Kart"))
    }
} 