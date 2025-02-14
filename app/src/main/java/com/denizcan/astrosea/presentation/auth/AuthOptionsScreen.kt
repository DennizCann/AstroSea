package com.denizcan.astrosea.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthOptionsScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Astro Sea'ye Hoş Geldiniz",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onNavigateToSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Giriş Yap")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToSignUp,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Kayıt Ol")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Google ile Devam Et")
        }
    }
} 