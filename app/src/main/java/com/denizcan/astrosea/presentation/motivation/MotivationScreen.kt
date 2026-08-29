package com.denizcan.astrosea.presentation.motivation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotivationScreen(
    onNavigateBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.anamenu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AstroTopBar(
                    title = stringResource(R.string.title_motivation),
                    onBackClick = onNavigateBack
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.coming_soon_exclaim),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily(Font(R.font.cinzel_regular))
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.motivation_coming_body),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
} 