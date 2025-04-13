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
import com.denizcan.astrosea.ui.theme.AstroSeaTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.presentation.profile.ProfileViewModel
import com.denizcan.astrosea.presentation.horoscope.HoroscopeScreen
import com.denizcan.astrosea.presentation.tarot.meanings.TarotMeaningsScreen
import com.denizcan.astrosea.presentation.motivation.MotivationScreen
import com.denizcan.astrosea.presentation.profile.ProfileScreen
import com.denizcan.astrosea.presentation.tarotSpreads.TarotSpreadsScreen
import com.denizcan.astrosea.presentation.yesNo.YesNoScreen
import com.denizcan.astrosea.presentation.relationship.RelationshipReadingsScreen
import com.denizcan.astrosea.presentation.general.GeneralReadingsScreen
import com.denizcan.astrosea.presentation.career.CareerReadingScreen
import com.denizcan.astrosea.presentation.more.MoreScreen
import android.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.denizcan.astrosea.presentation.auth.SignInScreen
import com.denizcan.astrosea.presentation.birthChart.BirthChartScreen
import com.denizcan.astrosea.presentation.tarot.meanings.TarotMeaningsViewModel
import com.denizcan.astrosea.presentation.tarot.meanings.TarotMeaningsViewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.denizcan.astrosea.util.JsonLoader
import android.content.Context

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
            AstroSeaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    val navController = rememberNavController()
                    val scope = rememberCoroutineScope()
                    var startDestination by remember {
                        mutableStateOf<String>(Screen.Onboarding.route)
                    }
                    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
                    val viewModel: ProfileViewModel = viewModel()
                    val context = LocalContext.current

                    LaunchedEffect(key1 = currentUser) {
                        if (currentUser != null) {
                            startDestination = Screen.Home.route
                        } else {
                            // SharedPreferences'ten onboarding durumunu kontrol et
                            val hasSeenOnboarding = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                .getBoolean("has_seen_onboarding", false)
                            startDestination = if (hasSeenOnboarding) Screen.Auth.route else Screen.Onboarding.route
                        }
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
                                    navController.navigate(Screen.YesNo.route)
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
                                onSignOut = {
                                    scope.launch {
                                        googleAuthUiClient.signOut()
                                        FirebaseAuth.getInstance().signOut()
                                        navController.navigate(Screen.Auth.route) {
                                            popUpTo(Screen.Home.route) { inclusive = true }
                                        }
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
                            TarotMeaningsRoute(
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }

                        composable(Screen.YesNo.route) {
                            YesNoScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("relationship_readings") {
                            RelationshipReadingsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("career_reading") {
                            CareerReadingScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("more") {
                            MoreScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("general_readings") {
                            GeneralReadingsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
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

@Composable
fun TarotMeaningsRoute(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: TarotMeaningsViewModel = viewModel(
        factory = TarotMeaningsViewModelFactory(JsonLoader(context))
    )

    TarotMeaningsScreen(
        onNavigateBack = onNavigateBack,
        viewModel = viewModel
    )
}