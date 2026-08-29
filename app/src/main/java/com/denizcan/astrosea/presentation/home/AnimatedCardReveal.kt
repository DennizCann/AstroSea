package com.denizcan.astrosea.presentation.home

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.positionInParent
import com.denizcan.astrosea.R
import com.denizcan.astrosea.model.TarotCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedCardReveal(
    cardState: DailyCardState,
    onCardClick: () -> Unit,
    onCardDetailClick: (String) -> Unit,
    onDrawCard: () -> Unit = {},
    modifier: Modifier = Modifier,
    parentSize: IntSize = IntSize.Zero
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // Kart kimliği için benzersiz key - kart değiştiğinde state sıfırlanır
    val cardKey = cardState.card?.id ?: "empty_${cardState.index}"
    
    // Animasyon durumları - cardKey ile key'lendi
    var isRevealing by remember(cardKey) { mutableStateOf(false) }
    var isFlipped by remember(cardKey, cardState.isRevealed) { mutableStateOf(cardState.isRevealed) }
    
    // Animasyon değerleri
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "rotation"
    )
    
    // State senkronizasyonu
    LaunchedEffect(cardState.isRevealed, cardKey) {
        isFlipped = cardState.isRevealed
        // Kart sıfırlandığında isRevealing'i de sıfırla
        if (!cardState.isRevealed) {
            isRevealing = false
        }
    }
    
    // Kart tıklama işleyicisi
    val handleCardClick: () -> Unit = {
        // Sadece kapalı kartlara tıklanabilir ve animasyon çalışmıyorsa
        if (!cardState.isRevealed && !isRevealing) {
            Log.d("AnimatedCardReveal", "🎴 Card click for card ${cardState.index}, cardKey: $cardKey")
            scope.launch {
                isRevealing = true
                
                // Kartı çek
                onDrawCard()
                
                // Kartı çevir
                isFlipped = true
                delay(800) // Çevirme animasyonunun tamamını bekle
                
                isRevealing = false
                Log.d("AnimatedCardReveal", "🎉 Animation completed for card ${cardState.index}")
            }
        } else if (cardState.isRevealed && !isRevealing) {
            // Kart zaten açıksa direkt detay sayfasına git
            cardState.card?.let { card ->
                onCardDetailClick(card.id)
            }
        }
    }
    
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density.density
            }
    ) {
        if (rotation < 90f) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = handleCardClick),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                    contentDescription = stringResource(R.string.cd_tarot_card),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f
                    }
                    .clickable(onClick = handleCardClick),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (cardState.card != null) {
                        val imageName = cardState.card.imageResName
                            .replace("ace", "one")
                            .replace("_of_", "of")
                            .replace("_", "")
                            .lowercase()
                        val imageResId = context.resources.getIdentifier(
                            imageName,
                            "drawable",
                            context.packageName
                        )
                        if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = cardState.card.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            // Resim bulunamadıysa varsayılan
                            Image(
                                painter = painterResource(id = R.drawable.placeholder_card),
                                contentDescription = stringResource(R.string.cd_card_not_found),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        // Kart henüz çekilmemişse arka yüzü göster
                        Image(
                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                            contentDescription = stringResource(R.string.cd_closed_card),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
} 