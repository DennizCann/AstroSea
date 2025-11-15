package com.denizcan.astrosea.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.denizcan.astrosea.R
import java.util.Calendar

@Composable
fun WheelDatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (day: Int, month: Int, year: Int) -> Unit,
    initialYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    initialMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    initialDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    minYear: Int = 1900,
    maxYear: Int = Calendar.getInstance().get(Calendar.YEAR)
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedDay by remember { mutableStateOf(initialDay) }

    // Ay isimleri
    val monthNames = listOf(
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    )

    // Yıl listesi
    val years = (minYear..maxYear).toList().reversed()
    
    // Seçilen aya göre gün sayısı
    val daysInMonth = remember(selectedYear, selectedMonth) {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, 1)
        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    
    // Gün listesi
    val days = (1..daysInMonth).toList()
    
    // Seçilen gün, ayın gün sayısından fazlaysa düzelt
    LaunchedEffect(selectedYear, selectedMonth) {
        if (selectedDay > daysInMonth) {
            selectedDay = daysInMonth
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başlık
                Text(
                    text = "Doğum Tarihi Seçin",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        fontSize = 22.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Yıl, Ay, Gün seçici
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Gün seçici
                    WheelPicker(
                        items = days,
                        selectedIndex = days.indexOf(selectedDay).coerceAtLeast(0),
                        label = "Gün",
                        onItemSelected = { selectedDay = it }
                    )

                    // Ay seçici
                    WheelPicker(
                        items = monthNames,
                        selectedIndex = selectedMonth,
                        label = "Ay",
                        onItemSelected = { monthName -> 
                            selectedMonth = monthNames.indexOf(monthName).coerceAtLeast(0)
                        }
                    )

                    // Yıl seçici
                    WheelPicker(
                        items = years,
                        selectedIndex = years.indexOf(selectedYear).coerceAtLeast(0),
                        label = "Yıl",
                        onItemSelected = { selectedYear = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Butonlar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // İptal butonu
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "İptal",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                            )
                        )
                    }

                    // Tamam butonu
                    Button(
                        onClick = {
                            onDateSelected(selectedDay, selectedMonth, selectedYear)
                            onDismissRequest()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Tamam",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun <T> WheelPicker(
    items: List<T>,
    selectedIndex: Int,
    label: String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Merkeze hizalama için offset hesapla
    val centerOffset = (180.dp.value - 36.dp.value) / 2 // 72dp - item'ı merkeze getirmek için
    
    // İlk yüklemede seçili öğeyi merkeze getir
    LaunchedEffect(Unit) {
        if (selectedIndex in items.indices) {
            val targetIndex = selectedIndex.coerceIn(0, items.size - 1)
            kotlinx.coroutines.delay(100) // Layout tamamlanana kadar bekle
            listState.scrollToItem(
                index = targetIndex,
                scrollOffset = 0
            )
        }
    }
    
    // Scroll durduğunda merkezdeki öğeyi seç
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            // Scroll durdu, merkezdeki item'ı bul
            kotlinx.coroutines.delay(50) // Scroll tam durduğundan emin ol
            
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset + 
                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            
            // Merkeze en yakın item'ı bul
            val centerItem = layoutInfo.visibleItemsInfo.minByOrNull { itemInfo ->
                val itemCenter = itemInfo.offset + itemInfo.size / 2
                kotlin.math.abs(itemCenter - viewportCenter)
            }
            
            centerItem?.let { item ->
                if (item.index != selectedIndex && item.index in items.indices) {
                    // Merkezdeki item seçili değilse, onu seç
                    onItemSelected(items[item.index])
                }
            }
        }
    }

    Column(
        modifier = modifier
            .width(80.dp)
            .height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Wheel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            LazyColumn(
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = centerOffset.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(items) { index, item ->
                    val isSelected = index == selectedIndex
                    val textColor = if (isSelected) {
                        Color.White
                    } else {
                        Color.White.copy(alpha = 0.5f)
                    }
                    val fontSize = if (isSelected) 18.sp else 16.sp

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clickable {
                                onItemSelected(item)
                                scope.launch {
                                    listState.scrollToItem(index = index, scrollOffset = 0)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.toString(),
                            color = textColor,
                            fontSize = fontSize,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        )
                    }
                }
            }

            // Seçili öğeyi vurgulamak için overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.Center)
                    .background(Color.White.copy(alpha = 0.1f))
            )
        }
    }
}

