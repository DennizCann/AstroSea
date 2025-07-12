package com.denizcan.astrosea.presentation.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// outlinedCardBorder fonksiyonunu tanımlıyoruz
private fun outlinedCardBorder(brush: androidx.compose.ui.graphics.Brush? = null): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(
        width = 1.dp,
        brush = brush ?: androidx.compose.ui.graphics.Brush.horizontalGradient(
            colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
        )
    )
}

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL
)

enum class NotificationType {
    DAILY_TAROT,
    GENERAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit
) {
    val notifications = remember { mutableStateListOf<Notification>() }
    var unreadCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val userId = auth.currentUser?.uid
    val context = LocalContext.current
    val notificationManager = remember { NotificationManager(context) }
    
    // Bildirimleri yükle
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                isLoading = true
                val loadedNotifications = notificationManager.getAllNotifications(userId)
                
                notifications.clear()
                notifications.addAll(loadedNotifications)
                unreadCount = notifications.count { !it.isRead }
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.acilimlararkaplan),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Scaffold(
            topBar = {
                AstroTopBar(
                    title = "Bildirimler",
                    onBackClick = onNavigateBack
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bildirim istatistikleri
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Toplam Bildirim",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${notifications.size}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                        fontSize = 28.sp
                                    ),
                                    color = Color.White
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Okunmamış",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "$unreadCount",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                        fontSize = 28.sp
                                    ),
                                    color = Color(0xFFFFD700) // Altın sarısı
                                )
                            }
                        }
                    }
                    
                    // Bildirim listesi
                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Bildirim Yok",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "Henüz bildiriminiz yok",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                        fontSize = 24.sp
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Günlük açılım kartlarınız yenilendiğinde burada bildirim göreceksiniz",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(notifications) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onNotificationClick = {
                                        scope.launch {
                                            // Bildirimi okundu olarak işaretle
                                            if (!notification.isRead && userId != null) {
                                                try {
                                                    notificationManager.markNotificationAsRead(userId, notification.id)
                                                    
                                                    // Yerel listeyi güncelle
                                                    val index = notifications.indexOfFirst { it.id == notification.id }
                                                    if (index != -1) {
                                                        notifications[index] = notification.copy(isRead = true)
                                                        unreadCount = notifications.count { !it.isRead }
                                                    }
                                                } catch (e: Exception) {
                                                    // Hata durumunda sessizce devam et
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onNotificationClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNotificationClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) 
                Color.Black.copy(alpha = 0.4f) 
            else 
                Color.Black.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = outlinedCardBorder(
            brush = if (!notification.isRead) {
                androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFD700), Color.Transparent)
                )
            } else {
                androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                )
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Bildirim ikonu
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = getNotificationTypeColor(notification.type)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getNotificationTypeIcon(notification.type),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Bildirim içeriği
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                        fontSize = 18.sp
                    ),
                    color = if (notification.isRead) Color.White.copy(alpha = 0.8f) else Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        fontSize = 16.sp
                    ),
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        fontSize = 14.sp
                    ),
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            
            // Okunmamış bildirim göstergesi
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(start = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFD700)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun getNotificationTypeIcon(type: NotificationType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        NotificationType.DAILY_TAROT -> Icons.Default.Notifications // Tarot ikonu için
        NotificationType.GENERAL -> Icons.Default.Notifications
    }
}

@Composable
private fun getNotificationTypeColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.DAILY_TAROT -> Color(0xFF9C27B0) // Mor
        NotificationType.GENERAL -> Color(0xFF607D8B) // Gri-mavi
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Az önce" // 1 dakikadan az
        diff < 3600000 -> "${diff / 60000} dakika önce" // 1 saatten az
        diff < 86400000 -> "${diff / 3600000} saat önce" // 1 günden az
        diff < 604800000 -> "${diff / 86400000} gün önce" // 1 haftadan az
        else -> {
            val date = Date(timestamp)
            val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            format.format(date)
        }
    }
}

 