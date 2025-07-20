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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.positionInParent
import com.denizcan.astrosea.R
import com.denizcan.astrosea.util.TarotCard
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
    
    // Animasyon durumları
    var isRevealing by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var isFlipped by remember { mutableStateOf(cardState.isRevealed) }
    
    // Kart pozisyonu için state'ler
    var cardSize by remember { mutableStateOf(IntSize.Zero) }
    var cardOffset by remember { mutableStateOf(IntOffset.Zero) }
    
    // Animasyon değerleri
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 3.0f else 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "rotation"
    )
    
    val zIndex by animateFloatAsState(
        targetValue = if (isExpanded) 99999f else 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "zindex"
    )
    
    // Offset animasyonu - kartı ekranın ortasına taşır
    val offsetX by animateFloatAsState(
        targetValue = if (isExpanded && parentSize.width > 0) {
            val screenCenterX = parentSize.width / 2f
            val cardCenterX = cardOffset.x + (cardSize.width / 2f)
            screenCenterX - cardCenterX
        } else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "offsetX"
    )
    
    val offsetY by animateFloatAsState(
        targetValue = if (isExpanded && parentSize.height > 0) {
            val screenCenterY = (parentSize.height / 2f) - 100f
            val cardCenterY = cardOffset.y + (cardSize.height / 2f)
            screenCenterY - cardCenterY
        } else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "offsetY"
    )
    
    // Kart durumu değiştiğinde isFlipped'i güncelle
    LaunchedEffect(cardState.isRevealed) {
        isFlipped = cardState.isRevealed
    }
    
    // Kart tıklama işleyicisi
    val handleCardClick: () -> Unit = {
        // Sadece kapalı kartlara tıklanabilir ve animasyon çalışmıyorsa
        if (!cardState.isRevealed && !isRevealing) {
            Log.d("AnimatedCardReveal", "🎴 Starting card reveal animation for card ${cardState.index}")
            scope.launch {
                isRevealing = true
                isExpanded = true
                delay(600)
                isFlipped = true
                delay(400)
                onDrawCard()
                delay(400)
                delay(1000)
                isExpanded = false
                delay(600)
                isRevealing = false
                Log.d("AnimatedCardReveal", "🎉 Animation completed for card ${cardState.index}")
            }
        } else if (cardState.isRevealed && !isRevealing) {
            cardState.card?.let { card ->
                onCardDetailClick(card.id)
            }
        }
    }
    
    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                cardSize = coordinates.size
                cardOffset = coordinates.positionInParent().let { 
                    IntOffset(it.x.toInt(), it.y.toInt()) 
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationY = rotation
                cameraDistance = 12f * density.density
                translationX = offsetX
                translationY = offsetY
            }
            .zIndex(zIndex)
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
                    defaultElevation = if (isExpanded) 16.dp else 4.dp
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
                    defaultElevation = if (isExpanded) 16.dp else 4.dp
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
                            Image(
                                painter = painterResource(id = R.drawable.placeholder_card),
                                contentDescription = "Kart bulunamadı",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
} 