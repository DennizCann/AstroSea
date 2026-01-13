package com.denizcan.astrosea.presentation.premium

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.R
import com.denizcan.astrosea.billing.BillingConfig
import com.denizcan.astrosea.billing.SubscriptionProduct

@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    onPurchaseComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: PremiumViewModel = viewModel(
        factory = PremiumViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    // Satın alma başarılı olduğunda
    LaunchedEffect(uiState.purchaseSuccess) {
        if (uiState.purchaseSuccess) {
            onPurchaseComplete()
            viewModel.resetPurchaseSuccess()
        }
    }
    
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
                // Close button ve Test Mode Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Test Mode Badge (sol üst)
                    if (uiState.isTestMode) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(
                                    color = Color(0xFFFF6B6B),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🔧 TEST MODU",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                    
                    // Close button (sağ üst)
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
                
                // Logo
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
                
                // Fiyatlandırma Başlığı
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
                
                // Loading durumu
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFFD4AF37),
                        modifier = Modifier.padding(32.dp)
                    )
                } else {
                    // Pricing Cards
                    uiState.products.forEachIndexed { index, product ->
                        PricingCard(
                            product = product,
                            isSelected = uiState.selectedProductIndex == index,
                            onSelect = { viewModel.selectProduct(index) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Ödemeye Geç Butonu
                Button(
                    onClick = { viewModel.showPurchaseConfirmation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    enabled = !uiState.isLoading && !uiState.isPurchasing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (uiState.isPurchasing) {
                                        listOf(
                                            Color(0xFF4A148C).copy(alpha = 0.5f),
                                            Color(0xFF6A1B9A).copy(alpha = 0.5f),
                                            Color(0xFF8E24AA).copy(alpha = 0.5f)
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFF4A148C),
                                            Color(0xFF6A1B9A),
                                            Color(0xFF8E24AA)
                                        )
                                    }
                                ),
                                shape = RoundedCornerShape(30.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isPurchasing) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "İşleniyor...",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        } else {
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
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bilgi yazısı
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
        
        // Onay Dialogu
        if (uiState.showConfirmDialog) {
            PurchaseConfirmDialog(
                product = uiState.products.getOrNull(uiState.selectedProductIndex),
                isTestMode = uiState.isTestMode,
                onConfirm = {
                    val activity = context as? Activity
                    if (activity != null) {
                        viewModel.startPurchase(activity)
                    }
                },
                onDismiss = { viewModel.dismissConfirmDialog() }
            )
        }
        
        // Hata Snackbar
        uiState.errorMessage?.let { error ->
            LaunchedEffect(error) {
                // Snackbar göster
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFB71C1C)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = error,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PurchaseConfirmDialog(
    product: SubscriptionProduct?,
    isTestMode: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1F3A),
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTestMode) {
                    Text(
                        text = "🔧 TEST MODU",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = "Satın Almayı Onayla",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFD4AF37)
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = product?.name ?: "Premium",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${product?.price ?: ""}${product?.duration ?: ""}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                    ),
                    color = Color(0xFFD4AF37)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isTestMode) {
                    Text(
                        text = "⚠️ Bu bir test satın almasıdır.\nGerçek ödeme alınmayacaktır.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF6B6B),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Aboneliğiniz otomatik olarak yenilenecektir.\nİstediğiniz zaman iptal edebilirsiniz.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                        ),
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A148C)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isTestMode) "Test Et" else "Satın Al",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily(Font(R.font.cinzel_bold))
                    ),
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "İptal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily(Font(R.font.cinzel_bold))
                    ),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    )
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
    product: SubscriptionProduct,
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
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        
                        if (product.isPopular) {
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
                            text = product.price,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFD4AF37)
                        )
                        Text(
                            text = product.duration,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                fontSize = 14.sp
                            ),
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    
                    // Aylık fiyat varsa göster
                    product.pricePerMonth?.let {
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

// ==================== PREVIEW ====================

@Preview(showBackground = true, backgroundColor = 0xFF0A0E27)
@Composable
private fun PremiumFeaturesListPreview() {
    PremiumFeaturesList()
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E27)
@Composable
private fun PricingCardPreview() {
    Column(
        modifier = Modifier
            .background(Color(0xFF0A0E27))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Seçili kart
        PricingCard(
            product = SubscriptionProduct(
                productId = "monthly",
                name = "Aylık",
                price = "₺79.99",
                duration = "/ay",
                durationDays = 30,
                pricePerMonth = null,
                isPopular = true
            ),
            isSelected = true,
            onSelect = {}
        )
        
        // Seçili olmayan kart
        PricingCard(
            product = SubscriptionProduct(
                productId = "yearly",
                name = "Yıllık",
                price = "₺499.99",
                duration = "/yıl",
                durationDays = 365,
                pricePerMonth = "Aylık ₺41.67",
                isPopular = false
            ),
            isSelected = false,
            onSelect = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1F3A)
@Composable
private fun PurchaseConfirmDialogPreview() {
    PurchaseConfirmDialog(
        product = SubscriptionProduct(
            productId = "monthly",
            name = "Aylık Premium",
            price = "₺79.99",
            duration = "/ay",
            durationDays = 30,
            isPopular = true
        ),
        isTestMode = true,
        onConfirm = {},
        onDismiss = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PremiumScreenContentPreview() {
    val mockProducts = listOf(
        SubscriptionProduct(
            productId = "weekly",
            name = "Haftalık",
            price = "₺29.99",
            duration = "/hafta",
            durationDays = 7
        ),
        SubscriptionProduct(
            productId = "monthly",
            name = "Aylık",
            price = "₺79.99",
            duration = "/ay",
            durationDays = 30,
            isPopular = true
        ),
        SubscriptionProduct(
            productId = "yearly",
            name = "Yıllık",
            price = "₺499.99",
            duration = "/yıl",
            durationDays = 365,
            pricePerMonth = "Aylık ₺41.67"
        )
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient overlay (Preview için basitleştirilmiş)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0E27),
                            Color(0xFF1A1F3A),
                            Color(0xFF2D1B4E)
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Test Mode Badge
            Box(
                modifier = Modifier
                    .align(Alignment.Start)
                    .background(
                        color = Color(0xFFFF6B6B),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🔧 TEST MODU",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Başlık
            Text(
                text = "Premium'a Yükseltin",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFFD4AF37),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sınırsız Tarot Açılımları ve Özel İçeriklere Erişin",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Özellikler
            PremiumFeaturesList()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Planınızı Seçin",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fiyat kartları
            mockProducts.forEachIndexed { index, product ->
                PricingCard(
                    product = product,
                    isSelected = index == 1, // Aylık seçili
                    onSelect = {}
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Ödemeye Geç butonu
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(28.dp)
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
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ödemeye Geç",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "İstediğiniz zaman iptal edebilirsiniz",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
