package com.denizcan.astrosea.presentation.tarot.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.presentation.components.AstroTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotSpreadsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AstroTopBar(
                title = "Tarot Açılımları",
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
                text = "Çok Yakında!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Farklı konular için hazırlanmış tarot açılımlarını yakında burada bulabileceksiniz.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 