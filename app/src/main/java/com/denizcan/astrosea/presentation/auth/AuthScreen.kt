package com.denizcan.astrosea.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.denizcan.astrosea.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.anabackground),
            contentDescription = "Arka plan görseli",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        NavHost(
            navController = navController,
            startDestination = "auth_options"
        ) {
            composable("auth_options") {
                AuthOptionsScreen(
                    onNavigateToSignUp = { navController.navigate("sign_up") },
                    onNavigateToSignIn = { navController.navigate("sign_in") },
                    onGoogleSignIn = onGoogleSignIn
                )
            }

            composable("sign_in") {
                SignInScreen(
                    onNavigateToSignUp = {
                        navController.navigate("sign_up") {
                            popUpTo("auth_options")
                        }
                    },
                    onSignInSuccess = onNavigateToHome,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("sign_up") {
                SignUpScreen(
                    onNavigateToSignIn = {
                        navController.navigate("sign_in") {
                            popUpTo("auth_options")
                        }
                    },
                    onSignUpSuccess = { email, password ->
                        navController.navigate("email_validation/$email/$password")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            composable("email_validation/{email}/{password}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                val password = backStackEntry.arguments?.getString("password") ?: ""
                EmailValidationScreen(
                    email = email,
                    password = password,
                    onEmailVerified = onNavigateToHome,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
