package com.denizcan.astrosea.presentation.profileCompletion

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun ProfileCompletionScreen2(
    viewModel: ProfileCompletionViewModel = viewModel(),
    onNavigateToNext: () -> Unit
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
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a0033),
                        Color(0xFF2d1b69),
                        Color(0xFF4a2f8f),
                        Color(0xFF5d3fa8),
                        Color(0xFF2a4f7f),
                        Color(0xFF1a365d)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Başlık
            Text(
                text = "Yıldızlar kaderinizi şekillendirsin.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Doğum Tarihi Input (Tıklanabilir)
            OutlinedTextField(
                value = birthDate,
                onValueChange = { },
                label = { 
                    Text(
                        "Doğum tarihiniz...",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { datePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )

            // Doğum Saati Input (Tıklanabilir)
            OutlinedTextField(
                value = birthTime,
                onValueChange = { },
                label = { 
                    Text(
                        "Doğum saatiniz...",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .clickable { timePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                    .width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B4FA0).copy(alpha = 0.7f),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "İleri",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

