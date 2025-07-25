package com.denizcan.astrosea.presentation.general

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
fun AnimatedReadingCard(
    cardState: ReadingCardState,
    onCardClick: () -> Unit,
    onNavigateToCardDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    parentSize: IntSize = IntSize.Zero
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // Animasyon durumları
    var isRevealing by remember { mutableStateOf(false) }
    // Kart animasyonu için state
    var isFlipped by remember { mutableStateOf(cardState.isRevealed) }
    
    // Animasyon değerleri
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "rotation"
    )
    
    // Basit state senkronizasyonu
    LaunchedEffect(cardState.isRevealed) {
        isFlipped = cardState.isRevealed
    }
    
    // Kart tıklama işleyicisi - onCardClick parametresini kullan
    val handleCardClick: () -> Unit = {
        // Sadece kapalı kartlara tıklanabilir ve animasyon çalışmıyorsa
        if (!cardState.isRevealed && !isRevealing) {
            Log.d("AnimatedReadingCard", "🎴 Starting card reveal animation for card ${cardState.index}")
            isRevealing = true
            onCardClick() // onCardClick parametresini kullan
            isRevealing = false
        } else if (cardState.isRevealed && !isRevealing) {
            // Kart ön yüzü dönükken direkt detay sayfasına git
            cardState.card?.let { card ->
                onNavigateToCardDetail(card.id)
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
        // Kart ön yüz veya arka yüz gösterme
        if (rotation < 90f) {
            // Arka yüz - kapalı kart
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
                    contentDescription = "Tarot kartı",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
            // Ön yüz - açık kart
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f // Ters çevirme düzeltmesi
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
                    // Kart çekildiyse ve geçerli bir kart varsa
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
                                contentDescription = "Kart bulunamadı",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        // Kart henüz çekilmemişse arka yüzü göster
                        Image(
                            painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                            contentDescription = "Kapalı Kart",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
} 