package com.denizcan.astrosea.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.denizcan.astrosea.util.KvkkTexts

/**
 * KVKK Aydınlatma Metni Dialog'u
 * Tam ekran scrollable dialog
 */
@Composable
fun KvkkDialog(
    onDismiss: () -> Unit,
    onAccept: (() -> Unit)? = null, // null ise sadece okuma modu (profil ekranından)
    showAcceptButton: Boolean = true
) {
    val title = KvkkTexts.getTitle()
    val fullText = KvkkTexts.getFullText()
    val scrollState = rememberScrollState()
    
    // Kullanıcı en alta kaydırdı mı kontrolü
    val isScrolledToBottom by remember {
        derivedStateOf {
            val maxScroll = scrollState.maxValue
            if (maxScroll == 0) true // İçerik scroll gerektirmiyorsa
            else scrollState.value >= maxScroll - 100 // 100px tolerans
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A2E)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Başlık
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D1B4E))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Metin içeriği (scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    Text(
                        text = fullText.trim(),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }

                // Alt butonlar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D1B4E).copy(alpha = 0.5f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Kapat butonu
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (KvkkTexts.isDeviceTurkishLocale()) "Kapat" else "Close"
                        )
                    }

                    // Kabul Et butonu (sadece kayıt ekranında göster)
                    if (showAcceptButton && onAccept != null) {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f),
                            enabled = isScrolledToBottom,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4A148C),
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = if (KvkkTexts.isDeviceTurkishLocale()) "Kabul Et" else "Accept",
                                color = if (isScrolledToBottom) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                // Scroll ipucu
                if (!isScrolledToBottom && showAcceptButton && onAccept != null) {
                    Text(
                        text = if (KvkkTexts.isDeviceTurkishLocale()) 
                            "Kabul etmek için metni sonuna kadar okuyun" 
                        else 
                            "Scroll to the end to accept",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2D1B4E).copy(alpha = 0.3f))
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * KVKK Checkbox + Link bileşeni
 * Kayıt ekranında kullanılacak
 */
@Composable
fun KvkkCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF4A148C),
                uncheckedColor = Color.White.copy(alpha = 0.7f),
                checkmarkColor = Color.White
            )
        )
        
        TextButton(
            onClick = onTextClick,
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Text(
                text = KvkkTexts.getConsentText(),
                color = Color.White,
                fontSize = 13.sp,
                style = LocalTextStyle.current.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            )
        }
    }
}

/**
 * Profil ekranında kullanılacak KVKK linki
 */
@Composable
fun KvkkLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = KvkkTexts.getShortTitle(),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            style = LocalTextStyle.current.copy(
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
            )
        )
    }
}
