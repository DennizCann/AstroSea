package com.denizcan.astrosea

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.denizcan.astrosea.auth.GoogleAuthUiClient
import com.denizcan.astrosea.navigation.Screen
import com.denizcan.astrosea.presentation.auth.AuthScreen
import com.denizcan.astrosea.presentation.auth.EmailValidationScreen
import com.denizcan.astrosea.presentation.home.HomeScreen
import com.denizcan.astrosea.presentation.onboarding.OnboardingScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
import androidx.navigation.NavType
import com.denizcan.astrosea.presentation.tarot.meanings.TarotDetailScreen
import androidx.navigation.navArgument
import com.denizcan.astrosea.presentation.general.GeneralReadingDetailScreen
import com.denizcan.astrosea.presentation.general.GeneralReadingInfoScreen
import com.denizcan.astrosea.navigation.SmartNavigationHelper
import com.denizcan.astrosea.presentation.notifications.NotificationsScreen
import com.denizcan.astrosea.notifications.DailyNotificationScheduler
import com.denizcan.astrosea.notifications.PremiumReminderScheduler
import com.google.firebase.firestore.FirebaseFirestore
import com.denizcan.astrosea.presentation.profileCompletion.ProfileCompletionScreen1
import com.denizcan.astrosea.presentation.profileCompletion.ProfileCompletionScreen2
import com.denizcan.astrosea.presentation.profileCompletion.ProfileCompletionScreen3
import com.denizcan.astrosea.presentation.profileCompletion.ProfileCompletionViewModel
import com.denizcan.astrosea.presentation.profileCompletion.ProfileCompletionStatus
import com.denizcan.astrosea.presentation.auth.TransitionScreen
import com.denizcan.astrosea.presentation.introduction.IntroductionPopupScreen
import com.denizcan.astrosea.presentation.components.KvkkDialog


class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash Screen'i kur (super.onCreate'den ÖNCE olmalı)
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        // Sistem çubuklarını şeffaf yap
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        
        // Bildirimden gelen navigasyonu kontrol et
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "notifications") {
            // Bildirimden geldiğinde bildirimler sayfasına yönlendir
            android.util.Log.d("MainActivity", "Navigating to notifications from notification")
        }
        
        // Bildirim izinlerini kontrol et ve iste
        val notificationManager = com.denizcan.astrosea.presentation.notifications.NotificationManager(this)
        if (!notificationManager.checkNotificationPermission()) {
            notificationManager.requestNotificationPermission(this)
        }

        // Auth state listener oluşturalım
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            // Kullanıcı değiştiğinde uygulama durumunu güncelle
            if (user != null) {
                // Log işlemi ekleyelim - hata ayıklama için
                Log.d("Auth", "User logged in: ${user.uid}, email: ${user.email}")
                
                // Kullanıcı giriş yaptığında günlük bildirim alarmını kur (saat 10:00)
                DailyNotificationScheduler.scheduleDailyNotification(this)
                Log.d("Auth", "Günlük bildirim alarmı kuruldu (10:00)")
                
                // Premium durumunu kontrol et ve hatırlatmaları yönet
                checkPremiumAndScheduleReminders(user.uid)
            } else {
                Log.d("Auth", "User logged out")
                
                // Kullanıcı çıkış yaptığında tüm alarmları iptal et
                DailyNotificationScheduler.cancelDailyNotification(this)
                PremiumReminderScheduler.cancelAllReminders(this)
                Log.d("Auth", "Tüm bildirim alarmları iptal edildi")
            }
        }

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()
            var startDestination by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()
            var fallbackTried by remember { mutableStateOf(false) }
            
            // KVKK Dialog state
            var showKvkkDialog by remember { mutableStateOf(false) }
            var pendingNavDestination by remember { mutableStateOf<String?>(null) }
            var pendingUserId by remember { mutableStateOf<String?>(null) }

            val googleLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
                onResult = { result ->
                    if (result.resultCode == RESULT_OK && result.data != null) {
                        scope.launch {
                            val signInResult = googleAuthUiClient.signInWithIntent(result.data!!)
                            if (signInResult.data != null) {
                                // Google Sign-In başarılı, profil kontrolü yap
                                val profileVM = ProfileCompletionViewModel()
                                val completionStatus = profileVM.checkProfileCompletion()
                                
                                val destination = when (completionStatus) {
                                    ProfileCompletionStatus.COMPLETE -> Screen.Home.route
                                    ProfileCompletionStatus.INCOMPLETE_NAME -> Screen.ProfileCompletion1.route
                                    ProfileCompletionStatus.INCOMPLETE_BIRTH -> Screen.ProfileCompletion2.route
                                    ProfileCompletionStatus.INCOMPLETE_LOCATION -> Screen.ProfileCompletion3.route
                                    else -> Screen.Home.route
                                }
                                
                                // KVKK kontrolü
                                if (!signInResult.kvkkAccepted) {
                                    // KVKK kabul edilmemiş, dialog göster
                                    pendingNavDestination = destination
                                    pendingUserId = signInResult.data.userId
                                    showKvkkDialog = true
                                } else {
                                    // KVKK zaten kabul edilmiş, devam et
                                    navController.navigate(destination) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            } else {
                                Log.e("GoogleSignIn", "Giriş başarısız (fallback): ${signInResult.errorMessage}")
                            }
                        }
                    }
                }
            )

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { result ->
                    if (result.resultCode == RESULT_OK && result.data != null) {
                        scope.launch {
                            val signInResult = googleAuthUiClient.signInWithIntent(result.data!!)
                            if (signInResult.data != null) {
                                // Google Sign-In başarılı, profil kontrolü yap
                                val profileVM = ProfileCompletionViewModel()
                                val completionStatus = profileVM.checkProfileCompletion()
                                
                                val destination = when (completionStatus) {
                                    ProfileCompletionStatus.COMPLETE -> Screen.Home.route
                                    ProfileCompletionStatus.INCOMPLETE_NAME -> Screen.ProfileCompletion1.route
                                    ProfileCompletionStatus.INCOMPLETE_BIRTH -> Screen.ProfileCompletion2.route
                                    ProfileCompletionStatus.INCOMPLETE_LOCATION -> Screen.ProfileCompletion3.route
                                    else -> Screen.Home.route
                                }
                                
                                // KVKK kontrolü
                                if (!signInResult.kvkkAccepted) {
                                    // KVKK kabul edilmemiş, dialog göster
                                    pendingNavDestination = destination
                                    pendingUserId = signInResult.data.userId
                                    showKvkkDialog = true
                                } else {
                                    // KVKK zaten kabul edilmiş, devam et
                                    navController.navigate(destination) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            } else {
                                Log.e("GoogleSignIn", "Giriş başarısız (one tap): ${signInResult.errorMessage}")
                                if (!fallbackTried) {
                                    fallbackTried = true
                                    val fallbackIntent = googleAuthUiClient.getFallbackSignInIntent()
                                    googleLauncher.launch(fallbackIntent)
                                }
                            }
                        }
                    } else {
                        // RESULT_CANCELED veya data null ise: fallback dene (bir kere)
                        if (!fallbackTried) {
                            fallbackTried = true
                            val fallbackIntent = googleAuthUiClient.getFallbackSignInIntent()
                            googleLauncher.launch(fallbackIntent)
                        }
                    }
                }
            )

            // Onboarding ve oturum kontrolü
            LaunchedEffect(Unit) {
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
                val user = FirebaseAuth.getInstance().currentUser
                
                // Her kullanıcı için introduction kontrolü - FIRESTORE'DAN (tüm cihazlarda senkron)
                val hasSeenIntroduction = user?.uid?.let { userId ->
                    try {
                        val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .get()
                            .await()
                        userDoc.getBoolean("hasSeenIntroduction") ?: false
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Error checking hasSeenIntroduction", e)
                        false
                    }
                } ?: false

                startDestination = when {
                    !hasSeenOnboarding -> Screen.Onboarding.route
                    user == null -> Screen.Auth.route
                    user != null && !user.isEmailVerified -> Screen.Auth.route
                    user != null && user.isEmailVerified -> {
                        // Kullanıcı giriş yapmış ve email doğrulanmış, profil tamamlığını kontrol et
                        val profileVM = ProfileCompletionViewModel()
                        val completionStatus = profileVM.checkProfileCompletion()
                        
                        when (completionStatus) {
                            ProfileCompletionStatus.COMPLETE -> {
                                // Profil tamamlanmış, introduction popup kontrolü yap (Firestore'dan)
                                if (!hasSeenIntroduction) {
                                    Screen.IntroductionPopup.route
                                } else {
                                    Screen.Home.route
                                }
                            }
                            ProfileCompletionStatus.INCOMPLETE_NAME, 
                            ProfileCompletionStatus.INCOMPLETE_BIRTH, 
                            ProfileCompletionStatus.INCOMPLETE_LOCATION -> {
                                // Profil tamamlanmamış, kullanıcıyı çıkış yaptır
                                FirebaseAuth.getInstance().signOut()
                                Screen.Auth.route
                            }
                            else -> Screen.Home.route
                        }
                    }
                    else -> Screen.Auth.route
                }
            }

            if (startDestination == null) {
                Box(Modifier.fillMaxSize()) { /* Splash veya boş ekran */ }
            } else {
                val context = LocalContext.current
                val smartNavigation = SmartNavigationHelper(context, navController)
                
                // Test için info ekranı kayıtlarını temizle (geliştirme aşamasında)
                // smartNavigation.clearInfoScreenRecords()
                
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
                                // Başarılı giriş sonrası profil tamamlama kontrolü
                                scope.launch {
                                    val profileVM = ProfileCompletionViewModel()
                                    val completionStatus = profileVM.checkProfileCompletion()
                                    
                                    val destination = when (completionStatus) {
                                        ProfileCompletionStatus.COMPLETE -> Screen.Home.route
                                        ProfileCompletionStatus.INCOMPLETE_NAME -> Screen.ProfileCompletion1.route
                                        ProfileCompletionStatus.INCOMPLETE_BIRTH -> Screen.ProfileCompletion2.route
                                        ProfileCompletionStatus.INCOMPLETE_LOCATION -> Screen.ProfileCompletion3.route
                                        else -> Screen.Home.route
                                    }
                                    
                                    navController.navigate(destination) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            onGoogleSignIn = {
                                scope.launch {
                                    Log.d("GoogleAuth", "UI: Google sign-in requested")
                                    val signInIntentSender = googleAuthUiClient.signIn()
                                    if (signInIntentSender != null) {
                                        Log.d("GoogleAuth", "UI: Launching One Tap intent sender")
                                        launcher.launch(
                                            IntentSenderRequest.Builder(signInIntentSender).build()
                                        )
                                    } else {
                                        // One Tap uygun kimlik bulamadı, GoogleSignIn fallback'ını başlat
                                        Log.w("GoogleAuth", "UI: One Tap not available, launching GoogleSignIn fallback")
                                        val fallbackIntent = googleAuthUiClient.getFallbackSignInIntent()
                                        googleLauncher.launch(fallbackIntent)
                                    }
                                }
                            }
                        )
                    }
                    
                    composable(Screen.EmailValidation.route) { backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        val password = backStackEntry.arguments?.getString("password") ?: ""
                        EmailValidationScreen(
                            email = email,
                            password = password,
                            onEmailVerified = {
                                // Email doğrulandıktan sonra profil tamamlama kontrolü
                                scope.launch {
                                    val profileVM = ProfileCompletionViewModel()
                                    val completionStatus = profileVM.checkProfileCompletion()
                                    
                                    val destination = when (completionStatus) {
                                        ProfileCompletionStatus.COMPLETE -> Screen.Home.route
                                        ProfileCompletionStatus.INCOMPLETE_NAME -> Screen.ProfileCompletion1.route
                                        ProfileCompletionStatus.INCOMPLETE_BIRTH -> Screen.ProfileCompletion2.route
                                        ProfileCompletionStatus.INCOMPLETE_LOCATION -> Screen.ProfileCompletion3.route
                                        else -> Screen.Home.route
                                    }
                                    
                                    navController.navigate(destination) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            onBackClick = {
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToTransition = {
                                navController.navigate(Screen.TransitionToAuth.route) {
                                    popUpTo(Screen.EmailValidation.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    // Geçiş Ekranı
                    composable(Screen.TransitionToAuth.route) {
                        TransitionScreen(
                            message = "Mail adresinizi kontrol edin.\nGiriş sayfasına yönlendiriliyorsunuz...",
                            onTransitionComplete = {
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    // Profil Tamamlama Ekranları
                    composable(Screen.ProfileCompletion1.route) {
                        val profileVM: ProfileCompletionViewModel = viewModel()
                        ProfileCompletionScreen1(
                            viewModel = profileVM,
                            onNavigateToNext = {
                                navController.navigate(Screen.ProfileCompletion2.route) {
                                    popUpTo(Screen.ProfileCompletion1.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable(Screen.ProfileCompletion2.route) {
                        val profileVM: ProfileCompletionViewModel = viewModel()
                        ProfileCompletionScreen2(
                            viewModel = profileVM,
                            onNavigateToNext = {
                                navController.navigate(Screen.ProfileCompletion3.route) {
                                    popUpTo(Screen.ProfileCompletion2.route) { inclusive = true }
                                }
                            },
                            onNavigateBack = {
                                navController.navigate(Screen.ProfileCompletion1.route) {
                                    popUpTo(Screen.ProfileCompletion2.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable(Screen.ProfileCompletion3.route) {
                        val profileVM: ProfileCompletionViewModel = viewModel()
                        ProfileCompletionScreen3(
                            viewModel = profileVM,
                            onNavigateToHome = {
                                // Profil tamamlandıktan sonra introduction popup kontrolü yap (Firestore'dan)
                                scope.launch {
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    val hasSeenIntroduction = userId?.let { uid ->
                                        try {
                                            val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(uid)
                                                .get()
                                                .await()
                                            userDoc.getBoolean("hasSeenIntroduction") ?: false
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Error checking hasSeenIntroduction", e)
                                            false
                                        }
                                    } ?: false
                                    
                                    if (!hasSeenIntroduction) {
                                        navController.navigate(Screen.IntroductionPopup.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                            },
                            onNavigateBack = {
                                navController.navigate(Screen.ProfileCompletion2.route) {
                                    popUpTo(Screen.ProfileCompletion3.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable(Screen.IntroductionPopup.route) {
                        // Introduction popup görüldüğünde Firestore'a kaydet (tüm cihazlarda senkron)
                        fun markIntroductionAsSeen() {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            userId?.let { uid ->
                                scope.launch {
                                    try {
                                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .document(uid)
                                            .update("hasSeenIntroduction", true)
                                            .await()
                                        android.util.Log.d("MainActivity", "hasSeenIntroduction set to true for user: $uid")
                                    } catch (e: Exception) {
                                        android.util.Log.e("MainActivity", "Error setting hasSeenIntroduction", e)
                                    }
                                }
                            }
                        }
                        
                        IntroductionPopupScreen(
                            onDismiss = {
                                markIntroductionAsSeen()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToTarotMeanings = {
                                markIntroductionAsSeen()
                                navController.navigate(Screen.TarotMeanings.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToHoroscope = {
                                markIntroductionAsSeen()
                                navController.navigate(Screen.Horoscope.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToBirthChart = {
                                markIntroductionAsSeen()
                                navController.navigate(Screen.BirthChart.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToPremium = {
                                markIntroductionAsSeen()
                                navController.navigate(Screen.Premium.route)
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
                                smartNavigation.navigateToReading("EVET – HAYIR AÇILIMI")
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
                            onNavigateToDailyReadingInfo = {
                                smartNavigation.navigateToReading("GÜNLÜK AÇILIM")
                            },
                            onNavigateToNotifications = {
                                navController.navigate(Screen.Notifications.route)
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
                                smartNavigation.navigateToReading(readingType)
                            }
                        )
                    }
                    composable("career_reading") {
                        CareerReadingScreen(
                            onNavigateToHome = { navController.navigate("home") },
                            onNavigateToGeneralReadings = { navController.navigate("general_readings") },
                            onNavigateToRelationshipReadings = { navController.navigate("relationship_readings") },
                            onNavigateToReadingDetail = { readingType ->
                                smartNavigation.navigateToReading(readingType)
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
                                smartNavigation.navigateToReading(readingType)
                            }
                        )
                    }
                    composable(
                        route = Screen.GeneralReadingInfo.route,
                        arguments = listOf(
                            navArgument("readingType") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val readingType = backStackEntry.arguments?.getString("readingType") ?: ""
                        GeneralReadingInfoScreen(
                            readingType = readingType,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToReadingDetail = { readingType ->
                                smartNavigation.navigateFromInfoToDetail(readingType)
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
                                smartNavigation.navigateBackFromDetail(readingType)
                            },
                            onNavigateToCardDetail = { cardId ->
                                navController.navigate("tarot_detail/$cardId")
                            },
                            onNavigateToPremium = {
                                navController.navigate(Screen.Premium.route)
                            }
                        )
                    }
                    composable(Screen.Notifications.route) {
                        NotificationsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(Screen.Premium.route) {
                        com.denizcan.astrosea.presentation.premium.PremiumScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onPurchaseComplete = {
                                // Premium satın alma başarılı - ana sayfaya dön
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }
                
                // KVKK Dialog (Google Sign-In için)
                if (showKvkkDialog) {
                    KvkkDialog(
                        onDismiss = {
                            // Kullanıcı KVKK'yı kabul etmeden kapatırsa, çıkış yap
                            scope.launch {
                                googleAuthUiClient.signOut()
                                showKvkkDialog = false
                                pendingNavDestination = null
                                pendingUserId = null
                            }
                        },
                        onAccept = {
                            // KVKK kabul edildi, Firestore'a kaydet ve devam et
                            scope.launch {
                                pendingUserId?.let { userId ->
                                    googleAuthUiClient.saveKvkkConsent(userId)
                                }
                                showKvkkDialog = false
                                pendingNavDestination?.let { destination ->
                                    navController.navigate(destination) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                                pendingNavDestination = null
                                pendingUserId = null
                            }
                        },
                        showAcceptButton = true
                    )
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
        
        // Uygulama kapandığında premium hatırlatmasını zamanla
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && PremiumReminderScheduler.isFirstTimeUser(this)) {
            // İlk kez kullanıcı ise 30 dakika sonra premium hatırlatma
            scheduleInstantPremiumReminder(userId)
        }
    }
    
    /**
     * Premium durumunu kontrol eder ve hatırlatmaları zamanlar
     */
    private fun checkPremiumAndScheduleReminders(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val isPremium = document.getBoolean("isPremium") ?: false
                
                if (isPremium) {
                    // Premium kullanıcı - tüm hatırlatmaları iptal et
                    PremiumReminderScheduler.cancelAllReminders(this)
                    Log.d("MainActivity", "Kullanıcı premium, hatırlatmalar iptal edildi")
                } else {
                    Log.d("MainActivity", "Kullanıcı premium değil, hatırlatmalar aktif")
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Premium kontrol hatası", e)
            }
    }
    
    /**
     * İlk kez kullanıcı için anında premium hatırlatma zamanlar (30 dakika sonra)
     */
    private fun scheduleInstantPremiumReminder(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val isPremium = document.getBoolean("isPremium") ?: false
                
                if (!isPremium) {
                    // Premium değilse 30 dakika sonra hatırlatma
                    PremiumReminderScheduler.scheduleInstantReminder(this)
                    Log.d("MainActivity", "30 dakika sonra premium hatırlatma zamanlandı")
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Premium kontrol hatası", e)
            }
    }
}

