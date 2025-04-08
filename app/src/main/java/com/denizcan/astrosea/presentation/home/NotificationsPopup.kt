package com.denizcan.astrosea.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.*

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

@Composable
fun NotificationsPopup(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Başlık ve Kapat butonu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bildirimler",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White
                        )
                    }
                }
                
                Divider(
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Bildirim listesi (geçici veriler)
                val notifications = getSampleNotifications()
                
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hiç bildirim yok",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notification ->
                            NotificationItem(notification)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) 
                Color.DarkGray.copy(alpha = 0.5f) 
            else 
                Color.DarkGray
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = formatTimestamp(notification.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
    return format.format(date)
}

private fun getSampleNotifications(): List<Notification> {
    // Geçici bildirimler
    return listOf(
        Notification(
            id = "1",
            title = "Günlük Burç Yorumunuz Hazır",
            message = "Bugün için burç yorumunuzu okumayı unutmayın!",
            timestamp = System.currentTimeMillis() - 3600000, // 1 saat önce
            isRead = false
        ),
        Notification(
            id = "2",
            title = "Yeni Özellik!",
            message = "Rün falı özelliği yakında kullanıma açılacak. Takipte kalın!",
            timestamp = System.currentTimeMillis() - 86400000, // 1 gün önce
            isRead = true
        ),
        Notification(
            id = "3",
            title = "Profil Bilgilerinizi Güncelleyin",
            message = "Doğum haritanızın daha doğru hesaplanması için profil bilgilerinizi eksiksiz doldurun.",
            timestamp = System.currentTimeMillis() - 259200000, // 3 gün önce
            isRead = true
        )
    )
} 