package com.denizcan.astrosea.presentation.premium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denizcan.astrosea.R

data class PremiumPlan(
    val name: String,
    val price: String,
    val duration: String,
    val pricePerMonth: String? = null,
    val isPopular: Boolean = false
)

@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    onPurchase: (String) -> Unit = {}
) {
    var selectedPlan by remember { mutableStateOf(1) } // Varsayılan olarak aylık seçili
    
    val plans = listOf(
        PremiumPlan(
            name = "Haftalık",
            price = "25 ₺",
            duration = "/hafta",
            pricePerMonth = null,
            isPopular = false
        ),
        PremiumPlan(
            name = "Aylık",
            price = "40 ₺",
            duration = "/ay",
            pricePerMonth = null,
            isPopular = true
        ),
        PremiumPlan(
            name = "Yıllık",
            price = "400 ₺",
            duration = "/yıl",
            pricePerMonth = "33 ₺/ay",
            isPopular = false
        )
    )
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.anabackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0E27).copy(alpha = 0.9f),
                            Color(0xFF1A1F3A).copy(alpha = 0.95f),
                            Color(0xFF2D1B4E).copy(alpha = 0.9f)
                        )
                    )
                )
        )
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(42.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Logo veya Icon
                Icon(
                    painter = painterResource(id = R.drawable.astrosea_logo),
                    contentDescription = "AstroSea Logo",
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFFD4AF37)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Başlık
                Text(
                    text = "Premium'a Yükseltin",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFFD4AF37),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Alt başlık
                Text(
                    text = "Sınırsız Tarot Açılımları ve Özel İçeriklere Erişin",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Premium Özellikler
                PremiumFeaturesList()
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Fiyatlandırma Kartları
                Text(
                    text = "Planınızı Seçin",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Pricing Cards
                plans.forEachIndexed { index, plan ->
                    PricingCard(
                        plan = plan,
                        isSelected = selectedPlan == index,
                        onSelect = { selectedPlan = index }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Ödemeye Geç Butonu
                Button(
                    onClick = { 
                        // Şimdilik çalışmaz
                        // onPurchase(plans[selectedPlan].name)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF4A148C),
                                        Color(0xFF6A1B9A),
                                        Color(0xFF8E24AA)
                                    )
                                ),
                                shape = RoundedCornerShape(30.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ödemeye Geç",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Küçük bilgi yazısı
                Text(
                    text = "İstediğiniz zaman iptal edebilirsiniz",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        fontSize = 13.sp
                    ),
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PremiumFeaturesList() {
    val features = listOf(
        "Sınırsız Tarot Açılımları",
        "Tüm Burç Yorumlarına Erişim",
        "Kişiselleştirilmiş Doğum Haritası",
        "Rün Falı ve Özel İçerikler",
        "Reklamsız Deneyim",
        "Günlük Bildirimler ve Hatırlatmalar"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        features.forEach { feature ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = Color(0xFFD4AF37).copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        fontSize = 16.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun PricingCard(
    plan: PremiumPlan,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF2D1B4E).copy(alpha = 0.8f) 
            else 
                Color(0xFF1A0F2E).copy(alpha = 0.6f)
        ),
        border = if (isSelected) {
            BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFD4AF37),
                        Color(0xFFFFD700)
                    )
                )
            )
        } else {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // Plan adı
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plan.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        
                        if (plan.isPopular) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFD4AF37),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Popüler",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                        fontSize = 10.sp
                                    ),
                                    color = Color(0xFF1A0F2E)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Fiyat
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = plan.price,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFD4AF37)
                        )
                        Text(
                            text = plan.duration,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                fontSize = 14.sp
                            ),
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    
                    // Aylık fiyat varsa göster
                    plan.pricePerMonth?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                fontSize = 13.sp
                            ),
                            color = Color(0xFFD4AF37).copy(alpha = 0.8f)
                        )
                    }
                }
                
                // Seçim göstergesi
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                        .background(
                            color = if (isSelected) Color(0xFFD4AF37) else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seçili",
                            tint = Color(0xFF1A0F2E),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

