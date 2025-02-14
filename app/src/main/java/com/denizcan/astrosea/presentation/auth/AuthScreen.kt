package com.denizcan.astrosea.presentation.auth

import android.content.IntentSender
import android.util.Log
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "auth_options"
    ) {
        composable("auth_options") {
            AuthOptionsScreen(
                onNavigateToSignIn = { navController.navigate("sign_in") },
                onNavigateToSignUp = { navController.navigate("sign_up") },
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
                onSignUpSuccess = onNavigateToHome,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
} 