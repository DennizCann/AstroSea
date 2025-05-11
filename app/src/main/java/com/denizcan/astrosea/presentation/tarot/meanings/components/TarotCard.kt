package com.denizcan.astrosea.presentation.tarot.meanings.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.model.TarotCard

@Composable
fun TarotCard(
    card: TarotCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageName = card.imageResName
        .replace("ace", "one")
        .replace("_of_", "of")
        .replace("_", "")
        .lowercase()
    val imageResId = context.resources.getIdentifier(
        imageName,
        "drawable",
        context.packageName
    )

    // Debug için
    if (imageResId == 0) {
        Log.d("TarotCard", "Resim bulunamadı: ${card.imageResName}")
    }

    Card(
        modifier = modifier
            .aspectRatio(0.5f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray.copy(alpha = 0.5f))
        ) {
            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                // Resim bulunamadığında gösterilecek içerik
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = card.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
} 