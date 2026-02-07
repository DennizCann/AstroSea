package com.denizcan.astrosea.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import com.denizcan.astrosea.util.responsiveSize
import com.denizcan.astrosea.util.heightPercent
import com.denizcan.astrosea.util.widthPercent
import com.denizcan.astrosea.util.isShortScreen
import androidx.compose.foundation.layout.heightIn

@Composable
fun AuthOptionsScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    // Responsive değerler
    val logoSize = responsiveSize(compact = 220.dp, medium = 280.dp, expanded = 340.dp)
    val buttonSectionHeight = heightPercent(fraction = 0.42f, maxSize = 320.dp)
    val horizontalPadding = responsiveSize(compact = 20.dp, medium = 28.dp, expanded = 32.dp)
    val buttonSpacing = responsiveSize(compact = 12.dp, medium = 16.dp, expanded = 16.dp)
    val isShort = isShortScreen()
    
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
                .padding(horizontal = horizontalPadding)
                .padding(vertical = if (isShort) 16.dp else 32.dp)
                .verticalScroll(rememberScrollState()),
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
                        .size(logoSize),  // Responsive logo boyutu
                    contentScale = ContentScale.Fit
                )
            }

            // Alt kısım - responsive yükseklik
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = buttonSectionHeight),  // Responsive yükseklik
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                // Responsive buton yüksekliği
                val buttonHeight = responsiveSize(compact = 48.dp, medium = 52.dp, expanded = 56.dp)
                val buttonFontSize = if (isShort) 17.sp else 20.sp
                
                // Giriş Yap Butonu
                Button(
                    onClick = onNavigateToSignIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
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
                            fontSize = buttonFontSize
                        )
                    )
                }

                // Kayıt Ol Butonu
                Button(
                    onClick = onNavigateToSignUp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
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
                            fontSize = buttonFontSize
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
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sadece Google butonu
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
                }
            }
        }
    }
}

// ==================== PREVIEW ====================

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun AuthOptionsScreenPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Preview için gradient arka plan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A0A2E),
                            Color(0xFF2D1B4E),
                            Color(0xFF1A1A3E)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo placeholder
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Logo yerine altın renkli placeholder
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            color = Color(0xFFD4AF37).copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "☽ ✧ ☾",
                        fontSize = 48.sp,
                        color = Color(0xFFD4AF37)
                    )
                }
            }

            // Alt kısım - butonlar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Giriş Yap Butonu
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A1A1A)
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
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A4A8F),
                        contentColor = Color.White
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
                    HorizontalDivider(
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
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                }

                // Google butonu placeholder
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                }
            }
        }
    }
} 