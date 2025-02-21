package com.denizcan.astrosea.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onNavigateToSignUp: () -> Unit,
    onSignInSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.anabackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            topBar = {
                AstroTopBar(
                    title = "Giriş Yap",
                    onBackClick = onBackClick
                )
            },
            containerColor = Color.Transparent,  // Scaffold'ı tamamen saydam yap
            contentColor = Color.White  // İçerik rengini beyaz yap
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .fillMaxHeight(0.55f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (errorMessage != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Red.copy(alpha = 0.3f)
                                    ),
                                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = when {
                                            email.isEmpty() -> "E-posta alanı boş bırakılamaz"
                                            password.isEmpty() -> "Şifre alanı boş bırakılamaz"
                                            errorMessage!!.contains("password") -> "Şifre hatalı"
                                            errorMessage!!.contains("user") -> "Bu e-posta adresi kayıtlı değil"
                                            else -> "Giriş yapılamadı. Lütfen bilgilerinizi kontrol edin"
                                        },
                                        modifier = Modifier.padding(16.dp),
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { 
                                    email = it
                                    errorMessage = null  // Input değişince hata mesajını temizle
                                },
                                label = { Text("E-posta", color = Color.White) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedBorderColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                    focusedLabelColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { 
                                    password = it
                                    errorMessage = null  // Input değişince hata mesajını temizle
                                },
                                label = { Text("Şifre", color = Color.White) },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedBorderColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                    focusedLabelColor = Color.White
                                )
                            )

                            Button(
                                onClick = {
                                    when {
                                        email.isEmpty() -> errorMessage = "E-posta alanı boş bırakılamaz"
                                        password.isEmpty() -> errorMessage = "Şifre alanı boş bırakılamaz"
                                        !email.contains("@") -> errorMessage = "Geçerli bir e-posta adresi giriniz"
                                        password.length < 6 -> errorMessage = "Şifre en az 6 karakter olmalıdır"
                                        else -> {
                                            scope.launch {
                                                isLoading = true
                                                errorMessage = null
                                                try {
                                                    auth.signInWithEmailAndPassword(email, password).await()
                                                    onSignInSuccess()
                                                } catch (e: Exception) {
                                                    errorMessage = when {
                                                        e.message?.contains("password") == true -> "Şifre hatalı"
                                                        e.message?.contains("user") == true -> "Bu e-posta adresi kayıtlı değil"
                                                        else -> "Giriş yapılamadı. Lütfen bilgilerinizi kontrol edin"
                                                    }
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(top = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f)
                                ),
                                enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text(
                                        "Giriş Yap",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }

                            TextButton(
                                onClick = onNavigateToSignUp,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    "Hesabın yok mu? Kayıt ol",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 