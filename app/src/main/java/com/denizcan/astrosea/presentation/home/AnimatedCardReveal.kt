package com.denizcan.astrosea.presentation.home

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Animasyon durumları
    var isRevealing by remember { mutableStateOf(false) }
    
    // Animasyon değerleri
    val rotation by animateFloatAsState(
        targetValue = if (cardState.isRevealed) 180f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "rotation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isRevealing) 2.5f else 1f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val zIndex by animateFloatAsState(
        targetValue = if (isRevealing) 9999f else 1f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "zindex"
    )
    
    // Kart tıklama işleyicisi
    val handleCardClick: () -> Unit = {
        // Kart arka yüzü dönükken animasyon çalışır
        if (rotation < 90f && !isRevealing) {
            Log.d("AnimatedCardReveal", "🎴 Starting card reveal animation")
            scope.launch {
                isRevealing = true
                
                // Büyütme animasyonunu bekle
                delay(700)
                
                // Kartı çek ve aç
                onDrawCard()
                
                // Çevirme animasyonunu bekle
                delay(800)
                
                // 1 saniye bekle
                delay(1000)
                
                // Küçültme animasyonunu bekle
                delay(700)
                
                isRevealing = false
                Log.d("AnimatedCardReveal", "🎉 Animation completed")
            }
        } else if (rotation >= 90f && !isRevealing) {
            // Kart ön yüzü dönükken direkt detay sayfasına git
            cardState.card?.let { card ->
                onCardDetailClick(card.id)
            }
        }
    }
    
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                scaleX = scale
                scaleY = scale
                cameraDistance = 12f * density
            }
            .zIndex(zIndex)
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
                shape = RoundedCornerShape(8.dp)
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
                shape = RoundedCornerShape(8.dp)
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
                    }
                }
            }
        }
    }
} 