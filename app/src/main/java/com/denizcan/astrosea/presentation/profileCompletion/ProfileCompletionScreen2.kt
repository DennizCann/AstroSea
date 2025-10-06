package com.denizcan.astrosea.presentation.profileCompletion

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.R
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun ProfileCompletionScreen2(
    viewModel: ProfileCompletionViewModel = viewModel(),
    onNavigateToNext: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val birthDate by viewModel.birthDate.collectAsState()
    val birthTime by viewModel.birthTime.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // DatePicker için
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            viewModel.updateBirthDate(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // TimePicker için
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            viewModel.updateBirthTime(formattedTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Başlık
            Text(
                text = "Yıldızlar kaderinizi şekillendirsin.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Doğum Tarihi Input
            OutlinedTextField(
                value = birthDate,
                onValueChange = { },
                placeholder = { 
                    Text(
                        "Doğum tarihiniz...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                            fontSize = 18.sp
                        ),
                        color = Color.White.copy(alpha = 0.7f)
                    ) 
                },
                readOnly = true,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                    fontSize = 20.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.8f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    disabledBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White,
                    focusedContainerColor = Color.Black.copy(alpha = 0.6f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.6f),
                    disabledContainerColor = Color.Black.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { datePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Doğum Saati Input
            OutlinedTextField(
                value = birthTime,
                onValueChange = { },
                placeholder = { 
                    Text(
                        "Doğum saatiniz...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                            fontSize = 18.sp
                        ),
                        color = Color.White.copy(alpha = 0.7f)
                    ) 
                },
                readOnly = true,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                    fontSize = 20.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.8f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    disabledBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White,
                    focusedContainerColor = Color.Black.copy(alpha = 0.6f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.6f),
                    disabledContainerColor = Color.Black.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { timePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Butonlar - Siyah arka plan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Geri butonu
                Button(
                    onClick = onNavigateBack,
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.75f),
                        disabledContainerColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "GERİ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }

                // İleri butonu
                Button(
                    onClick = {
                        if (birthDate.isNotBlank() && birthTime.isNotBlank()) {
                            scope.launch {
                                val success = viewModel.saveBirthData()
                                if (success) {
                                    onNavigateToNext()
                                }
                            }
                        }
                    },
                    enabled = birthDate.isNotBlank() && birthTime.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.75f),
                        disabledContainerColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "İLERİ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileCompletionScreen2Preview() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Arka plan için placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1a0033),
                            Color(0xFF2d1b69),
                            Color(0xFF4a2f8f)
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Başlık
            Text(
                text = "Yıldızlar kaderinizi şekillendirsin.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Doğum Tarihi Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { 
                        Text(
                            "Doğum tarihiniz...",
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        ) 
                    },
                    readOnly = true,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.8f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Doğum Saati Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { 
                        Text(
                            "Doğum saatiniz...",
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        ) 
                    },
                    readOnly = true,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.8f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Butonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Geri butonu
                Button(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Geri",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // İleri butonu
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.7f),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "İleri",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
