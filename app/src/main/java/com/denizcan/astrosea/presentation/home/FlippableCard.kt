package com.denizcan.astrosea.presentation.home

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.util.TarotCard

@Composable
fun FlippableCard(
    cardState: DailyCardState,
    onCardClick: () -> Unit,
    onCardDetailClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Çevirme animasyonu
    val rotation by animateFloatAsState(
        targetValue = if (cardState.isRevealed) 180f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "card_flip"
    )
    
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        // Kart ön yüz veya arka yüz gösterme
        if (rotation < 90f) {
            // Arka yüz - kapalı kart
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onCardClick),
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
                    .clickable {
                        // Kart açıksa ve tıklanırsa detay sayfasına git
                        cardState.card?.let { card ->
                            onCardDetailClick(card.id)
                        }
                    },
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
                        Log.d("FlippableCard", "imageName: $imageName, imageResId: $imageResId")
                        
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