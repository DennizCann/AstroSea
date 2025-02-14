package com.denizcan.astrosea

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.denizcan.astrosea.auth.GoogleAuthUiClient
import com.denizcan.astrosea.navigation.Screen
import com.denizcan.astrosea.presentation.auth.AuthScreen
import com.denizcan.astrosea.presentation.home.HomeScreen
import com.denizcan.astrosea.presentation.onboarding.OnboardingScreen
import com.denizcan.astrosea.presentation.profile.ProfileScreen
import com.denizcan.astrosea.ui.theme.AstroSeaTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.presentation.profile.ProfileViewModel
import com.denizcan.astrosea.presentation.horoscope.HoroscopeScreen
import com.denizcan.astrosea.presentation.tarot.TarotScreen
import com.denizcan.astrosea.presentation.runes.RunesScreen
import com.denizcan.astrosea.presentation.birthchart.BirthChartScreen

class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AstroSeaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val scope = rememberCoroutineScope()
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    
                    val startDestination = if (currentUser != null) {
                        Screen.Home.route
                    } else {
                        Screen.Onboarding.route
                    }

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartIntentSenderForResult(),
                        onResult = { result ->
                            if (result.resultCode == RESULT_OK) {
                                scope.launch {
                                    try {
                                        val signInResult = googleAuthUiClient.signInWithIntent(
                                            intent = result.data ?: return@launch
                                        )
                                        if (signInResult.data != null) {
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.Auth.route) { inclusive = true }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("GoogleSignIn", "Exception during sign in: ${e.message}", e)
                                    }
                                }
                            }
                        }
                    )

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(Screen.Onboarding.route) {
                            OnboardingScreen(
                                onFinishOnboarding = {
                                    navController.navigate(Screen.Auth.route) {
                                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable(Screen.Auth.route) {
                            AuthScreen(
                                onNavigateToHome = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Auth.route) { inclusive = true }
                                    }
                                },
                                onGoogleSignIn = {
                                    scope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                            )
                        }
                        
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onSignOut = {
                                    FirebaseAuth.getInstance().signOut()
                                    navController.navigate(Screen.Auth.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                },
                                onNavigateToProfile = {
                                    navController.navigate(Screen.Profile.route)
                                },
                                onNavigateToHoroscope = {
                                    navController.navigate(Screen.Horoscope.route)
                                },
                                onNavigateToTarot = {
                                    navController.navigate(Screen.Tarot.route)
                                },
                                onNavigateToRunes = {
                                    navController.navigate(Screen.Runes.route)
                                },
                                onNavigateToBirthChart = {
                                    navController.navigate(Screen.BirthChart.route)
                                }
                            )
                        }
                        
                        composable(Screen.Profile.route) {
                            val profileViewModel: ProfileViewModel = viewModel()
                            ProfileScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                viewModel = profileViewModel
                            )
                        }

                        composable(Screen.Horoscope.route) {
                            HoroscopeScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Screen.Tarot.route) {
                            TarotScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Screen.Runes.route) {
                            RunesScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Screen.BirthChart.route) {
                            BirthChartScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}