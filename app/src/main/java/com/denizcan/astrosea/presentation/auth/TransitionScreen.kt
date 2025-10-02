package com.denizcan.astrosea.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TransitionScreen(
    message: String = "Yönlendiriliyorsunuz...",
    onTransitionComplete: () -> Unit
) {
    // Animasyon için alpha değeri
    var alpha by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        // Fade in animasyonu
        for (i in 0..10) {
            alpha = i / 10f
            delay(50)
        }
        
        // Mesajı göster
        delay(2000)
        
        // Fade out animasyonu
        for (i in 10 downTo 0) {
            alpha = i / 10f
            delay(50)
        }
        
        // Geçiş tamamlandı
        onTransitionComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a0033),
                        Color(0xFF2d1b69),
                        Color(0xFF4a2f8f),
                        Color(0xFF5d3fa8),
                        Color(0xFF2a4f7f),
                        Color(0xFF1a365d)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Mesaj
            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = alpha)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

