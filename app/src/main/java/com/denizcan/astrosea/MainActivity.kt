package com.denizcan.astrosea

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
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
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.presentation.profile.ProfileViewModel
import com.denizcan.astrosea.presentation.horoscope.HoroscopeScreen
import com.denizcan.astrosea.presentation.tarot.meanings.TarotMeaningsScreen
import com.denizcan.astrosea.presentation.motivation.MotivationScreen
import com.denizcan.astrosea.presentation.profile.ProfileScreen
import com.denizcan.astrosea.presentation.relationship.RelationshipReadingsScreen
import com.denizcan.astrosea.presentation.general.GeneralReadingsScreen
import com.denizcan.astrosea.presentation.career.CareerReadingScreen
import com.denizcan.astrosea.presentation.more.MoreScreen
import android.graphics.Color
import androidx.core.view.WindowCompat
import com.denizcan.astrosea.presentation.birthChart.BirthChartScreen
import com.denizcan.astrosea.presentation.tarot.meanings.TarotMeaningsViewModel
import com.denizcan.astrosea.presentation.tarot.meanings.TarotMeaningsViewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.denizcan.astrosea.util.JsonLoader
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.navigation.NavController
import androidx.navigation.NavType
import com.denizcan.astrosea.presentation.tarot.meanings.TarotDetailScreen
import androidx.navigation.navArgument
import com.denizcan.astrosea.presentation.general.GeneralReadingDetailScreen


class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sistem çubuklarını şeffaf yap
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Auth state listener oluşturalım
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            // Kullanıcı değiştiğinde uygulama durumunu güncelle
            if (user != null) {
                // Log işlemi ekleyelim - hata ayıklama için
                Log.d("Auth", "User logged in: ${user.uid}, email: ${user.email}")
            } else {
                Log.d("Auth", "User logged out")
            }
        }

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()
            var startDestination by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { result ->
                    if (result.resultCode == RESULT_OK && result.data != null) {
                        scope.launch {
                            val signInResult = googleAuthUiClient.signInWithIntent(result.data!!)
                            if (signInResult.data != null) {
                                // Giriş başarılı, ana ekrana yönlendir
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                // Hata varsa logla veya kullanıcıya göster
                                Log.e("GoogleSignIn", "Giriş başarısız: ${signInResult.errorMessage}")
                            }
                        }
                    }
                }
            )

            // Onboarding ve oturum kontrolü
            LaunchedEffect(Unit) {
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
                val user = FirebaseAuth.getInstance().currentUser

                startDestination = when {
                    !hasSeenOnboarding -> Screen.Onboarding.route
                    user != null -> Screen.Home.route
                    else -> Screen.Auth.route
                }
            }

            if (startDestination == null) {
                Box(Modifier.fillMaxSize()) { /* Splash veya boş ekran */ }
            } else {
                NavHost(
                    navController = navController,
                    startDestination = startDestination!!
                ) {
                    composable(Screen.Onboarding.route) {
                        OnboardingScreen(
                            onFinishOnboarding = {
                                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("has_seen_onboarding", true)
                                    .apply()
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user != null) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(Screen.Auth.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                    composable(Screen.Auth.route) {
                        AuthScreen(
                            onNavigateToHome = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
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
                        val viewModel: ProfileViewModel = viewModel()
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToProfile = {
                                navController.navigate(Screen.Profile.route)
                            },
                            onNavigateToHoroscope = {
                                navController.navigate(Screen.Horoscope.route)
                            },
                            onNavigateToTarotMeanings = {
                                navController.navigate(Screen.TarotMeanings.route)
                            },
                            onNavigateToBirthChart = {
                                navController.navigate(Screen.BirthChart.route)
                            },
                            onNavigateToMotivation = {
                                navController.navigate(Screen.Motivation.route)
                            },
                            onNavigateToYesNo = {
                                navController.navigate(Screen.GeneralReadingDetail.createRoute("EVET – HAYIR AÇILIMI"))
                            },
                            onNavigateToRelationshipReadings = {
                                navController.navigate("relationship_readings")
                            },
                            onNavigateToCareerReading = {
                                navController.navigate("career_reading")
                            },
                            onNavigateToMore = {
                                navController.navigate("more")
                            },
                            onNavigateToGeneralReadings = {
                                navController.navigate("general_readings")
                            },
                            onNavigateToCardDetail = { cardId ->
                                navController.navigate("tarot_detail/$cardId")
                            },
                            onSignOut = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.Horoscope.route) {
                        HoroscopeScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.BirthChart.route) {
                        BirthChartScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Motivation.route) {
                        MotivationScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(route = Screen.TarotMeanings.route) {
                        val context = LocalContext.current
                        val viewModel: TarotMeaningsViewModel = viewModel(
                            factory = TarotMeaningsViewModelFactory(JsonLoader(context))
                        )
                        TarotMeaningsScreen(
                            onNavigateBack = { navController.navigateUp() },
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                    composable("tarot_detail/{cardId}") { backStackEntry ->
                        val cardId = backStackEntry.arguments?.getString("cardId")
                        if (cardId != null) {
                            val context = LocalContext.current
                            val viewModel: TarotMeaningsViewModel = viewModel(
                                factory = TarotMeaningsViewModelFactory(JsonLoader(context))
                            )
                            val card = viewModel.cards.find { it.id == cardId }
                            if (card != null) {
                                TarotDetailScreen(
                                    onNavigateBack = { navController.navigateUp() },
                                    card = card
                                )
                            }
                        }
                    }
                    composable("relationship_readings") {
                        RelationshipReadingsScreen(
                            onNavigateToHome = { navController.navigate("home") },
                            onNavigateToGeneralReadings = { navController.navigate("general_readings") },
                            onNavigateToCareerReadings = { navController.navigate("career_reading") },
                            onNavigateToReadingDetail = { readingType ->
                                navController.navigate(Screen.GeneralReadingDetail.createRoute(readingType))
                            }
                        )
                    }
                    composable("career_reading") {
                        CareerReadingScreen(
                            onNavigateToHome = { navController.navigate("home") },
                            onNavigateToGeneralReadings = { navController.navigate("general_readings") },
                            onNavigateToRelationshipReadings = { navController.navigate("relationship_readings") },
                            onNavigateToReadingDetail = { readingType ->
                                navController.navigate(Screen.GeneralReadingDetail.createRoute(readingType))
                            },
                            navController = navController
                        )
                    }
                    composable("more") {
                        MoreScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.GeneralReadings.route) {
                        GeneralReadingsScreen(
                            onNavigateToHome = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToRelationshipReadings = {
                                navController.navigate("relationship_readings")
                            },
                            onNavigateToCareerReading = {
                                navController.navigate("career_reading")
                            },
                            onNavigateToReadingDetail = { readingType ->
                                navController.navigate(Screen.GeneralReadingDetail.createRoute(readingType))
                            }
                        )
                    }
                    composable(
                        route = Screen.GeneralReadingDetail.route,
                        arguments = listOf(
                            navArgument("readingType") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val readingType = backStackEntry.arguments?.getString("readingType") ?: ""
                        GeneralReadingDetailScreen(
                            readingType = readingType,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToCardDetail = { cardId ->
                                navController.navigate("tarot_detail/$cardId")
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }
}

