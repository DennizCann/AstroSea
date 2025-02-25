package com.denizcan.astrosea.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denizcan.astrosea.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.ColorFilter

@Composable
fun AuthOptionsScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.anabackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo kısmı - yukarıda ve büyük
            Box(
                modifier = Modifier
                    .weight(1f)  // Üst kısmı esnek yaptık
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.astrosea_logo),
                    contentDescription = "AstroSea Logo",
                    modifier = Modifier
                        .size(340.dp),  // Logoyu büyüttük
                    contentScale = ContentScale.Fit
                )
            }

            // Alt kısım - sabit yükseklikte ve yerinde
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),  // Sabit yükseklik
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Giriş Yap Butonu
                Button(
                    onClick = onNavigateToSignIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A1A1A)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Giriş Yap",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }

                // Kayıt Ol Butonu
                Button(
                    onClick = onNavigateToSignUp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A4A8F),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Kayıt Ol",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }

                // "veya" kısmı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                    Text(
                        "veya",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                }

                // Sosyal medya butonları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google butonu
                    IconButton(
                        onClick = onGoogleSignIn,
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.google_icon),
                            contentDescription = "Google ile giriş yap",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                        )
                    }

                    // Facebook butonu
                    IconButton(
                        onClick = { /* Facebook girişi */ },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = Color(0xFF1877F2), // Facebook mavi
                                shape = CircleShape
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.facebook_icon),
                            contentDescription = "Facebook ile giriş yap",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }

                    // Twitter butonu
                    IconButton(
                        onClick = { /* Twitter girişi */ },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = Color.Black,
                                shape = CircleShape
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.twitter_icon),
                            contentDescription = "X ile giriş yap",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }
            }
        }
    }
} 