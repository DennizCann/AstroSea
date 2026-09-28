package com.denizcan.astrosea.presentation.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar
import java.text.SimpleDateFormat
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.denizcan.astrosea.billing.BillingConfig
import java.util.Calendar
import com.denizcan.astrosea.presentation.components.WheelDatePickerDialog
import com.denizcan.astrosea.util.responsiveSize
import com.denizcan.astrosea.util.responsivePadding
import com.denizcan.astrosea.presentation.components.KvkkDialog
import com.denizcan.astrosea.presentation.notifications.NotificationManager
import com.denizcan.astrosea.notifications.DailyNotificationScheduler
import com.denizcan.astrosea.util.KvkkTexts
import com.denizcan.astrosea.util.LanguageManager
import androidx.compose.ui.res.stringResource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit = onNavigateBack,
    viewModel: ProfileViewModel = viewModel()
) {
    val state = viewModel.profileState
    val context = LocalContext.current
    val notificationManager = remember { NotificationManager(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var initialDailyNotificationsEnabled by remember {
        mutableStateOf(DailyNotificationScheduler.areDailyNotificationsEnabled(context))
    }
    var selectedDailyNotificationsEnabled by remember {
        mutableStateOf(initialDailyNotificationsEnabled)
    }
    var systemNotificationsEnabled by remember { mutableStateOf(notificationManager.checkNotificationPermission()) }
    val calendar = remember { Calendar.getInstance() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showKvkkDialog by remember { mutableStateOf(false) }
    var showAccountDeletionDialog by remember { mutableStateOf(false) }
    var showFinalAccountDeletionDialog by remember { mutableStateOf(false) }
    var showSaveConfirmationDialog by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var initialProfileData by remember { mutableStateOf(state.profileData.copy()) }
    var initialLanguage by rememberSaveable { mutableStateOf(LanguageManager.getLanguage(context)) }
    var selectedLanguage by rememberSaveable { mutableStateOf(initialLanguage) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (selectedDailyNotificationsEnabled == initialDailyNotificationsEnabled) {
                    val storedPreference = DailyNotificationScheduler.areDailyNotificationsEnabled(context)
                    initialDailyNotificationsEnabled = storedPreference
                    selectedDailyNotificationsEnabled = storedPreference
                }
                systemNotificationsEnabled = notificationManager.checkNotificationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        initialProfileData = state.profileData.copy()
    }
    val hasProfileChanges = state.profileData.let { current ->
        current.name != initialProfileData.name ||
            current.surname != initialProfileData.surname ||
            current.birthDate != initialProfileData.birthDate ||
            current.birthTime != initialProfileData.birthTime ||
            current.country != initialProfileData.country ||
            current.city != initialProfileData.city
    }
    val hasLanguageChange = selectedLanguage != initialLanguage
    val hasNotificationChange = selectedDailyNotificationsEnabled != initialDailyNotificationsEnabled
    val hasChanges = hasProfileChanges || hasLanguageChange || hasNotificationChange
    
    // Mevcut tarihi parse et
    val (initialYear, initialMonth, initialDay) = remember(state.profileData.birthDate) {
        try {
            if (state.profileData.birthDate.isNotEmpty()) {
                val format = SimpleDateFormat("dd.MM.yyyy")
                val date = format.parse(state.profileData.birthDate)
                if (date != null) {
                    val cal = Calendar.getInstance()
                    cal.time = date
                    Triple(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                } else {
                    Triple(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                }
            } else {
                Triple(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
        } catch (e: Exception) {
            Triple(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    // Ülke ve şehir için dropdown state
    val countryList = listOf("Türkiye", "Almanya")
    val turkeyCities = listOf(
        "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin", "Aydın", "Balıkesir", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale", "Çankırı", "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum", "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkari", "Hatay", "Isparta", "Mersin", "İstanbul", "İzmir", "Kars", "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir", "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa", "Kahramanmaraş", "Mardin", "Muğla", "Muş", "Nevşehir", "Niğde", "Ordu", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop", "Sivas", "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Şanlıurfa", "Uşak", "Van", "Yozgat", "Zonguldak", "Aksaray", "Bayburt", "Karaman", "Kırıkkale", "Batman", "Şırnak", "Bartın", "Ardahan", "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye", "Düzce"
    )
    val germanyCities = listOf(
        "Berlin", "Hamburg", "Münih", "Köln", "Frankfurt", "Stuttgart", "Düsseldorf", "Dortmund", "Essen", "Leipzig", "Bremen", "Dresden", "Hannover", "Nürnberg", "Duisburg", "Bochum", "Wuppertal", "Bielefeld", "Bonn", "Münster", "Karlsruhe", "Mannheim", "Augsburg", "Wiesbaden", "Gelsenkirchen", "Mönchengladbach", "Braunschweig", "Chemnitz", "Kiel", "Aachen", "Halle", "Magdeburg", "Freiburg", "Krefeld", "Lübeck", "Oberhausen", "Erfurt", "Mainz", "Rostock", "Kassel", "Hagen", "Hamm", "Saarbrücken", "Mülheim", "Potsdam", "Ludwigshafen", "Oldenburg", "Leverkusen", "Osnabrück", "Solingen", "Heidelberg", "Herne", "Neuss", "Darmstadt", "Paderborn", "Regensburg", "Ingolstadt", "Würzburg", "Wolfsburg", "Offenbach", "Ulm", "Heilbronn", "Pforzheim", "Göttingen", "Bottrop", "Trier", "Recklinghausen", "Reutlingen", "Bremerhaven", "Koblenz", "Bergisch Gladbach", "Jena", "Remscheid", "Erlangen", "Moers", "Siegen", "Hildesheim", "Salzgitter"
    )
    val cityList = when (state.profileData.country) {
        "Almanya" -> germanyCities
        else -> turkeyCities
    }
    var countryExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.anamenu),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AstroTopBar(
                    title = stringResource(R.string.profile_title),
                    onBackClick = onNavigateBack
                )
            }
        ) { paddingValues ->
            if (state.isLoading && state.profileData.name.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
            val scrollState = rememberScrollState()
            val horizontalPad = responsivePadding(compact = 6.dp, medium = 8.dp, expanded = 12.dp)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = horizontalPad)
                    .verticalScroll(scrollState),  // Scroll eklendi
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                // Bilgiler kutusu
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileField(
                            label = stringResource(R.string.field_name),
                            value = state.profileData.name,
                            icon = Icons.Default.Person,
                            enabled = true,
                            onValueChange = { viewModel.onNameChange(it) }
                        )
                        ProfileField(
                            label = stringResource(R.string.field_surname),
                            value = state.profileData.surname,
                            icon = Icons.Default.Person,
                            enabled = true,
                            onValueChange = { viewModel.onSurnameChange(it) }
                        )
                        ProfileDateField(
                            label = stringResource(R.string.field_birth_date),
                            value = state.profileData.birthDate,
                            icon = Icons.Default.DateRange,
                            enabled = true,
                            onClick = { showDatePicker = true }
                        )
                        ProfileDateField(
                            label = stringResource(R.string.field_birth_time),
                            value = state.profileData.birthTime,
                            icon = Icons.Default.Info,
                            enabled = true,
                            onClick = { showTimePicker = true }
                        )
                        // Ülke Dropdown
                        ExposedDropdownMenuBox(
                            expanded = countryExpanded,
                            onExpandedChange = { countryExpanded = !countryExpanded }
                        ) {
                            OutlinedTextField(
                                value = state.profileData.country,
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.field_country), color = Color.White)
                                    }
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                enabled = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedBorderColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White,
                                    disabledTextColor = Color.White,
                                    disabledBorderColor = Color.White.copy(alpha = 0.3f)
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = countryExpanded,
                                onDismissRequest = { countryExpanded = false }
                            ) {
                                countryList.forEach { country ->
                                    DropdownMenuItem(
                                        text = { Text(country) },
                                        onClick = {
                                            viewModel.onCountryChange(country)
                                            countryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        // Şehir Dropdown
                        ExposedDropdownMenuBox(
                            expanded = cityExpanded,
                            onExpandedChange = { cityExpanded = !cityExpanded }
                        ) {
                            OutlinedTextField(
                                value = state.profileData.city,
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.field_city), color = Color.White)
                                    }
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                enabled = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedBorderColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White,
                                    disabledTextColor = Color.White,
                                    disabledBorderColor = Color.White.copy(alpha = 0.3f)
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = cityExpanded,
                                onDismissRequest = { cityExpanded = false }
                            ) {
                                cityList.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city) },
                                        onClick = {
                                            viewModel.onCityChange(city)
                                            cityExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                // Dil Seçimi
                Spacer(modifier = Modifier.height(16.dp))
                LanguageSelectionCard(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { selectedLanguage = it }
                )

                Spacer(modifier = Modifier.height(16.dp))
                NotificationSettingsCard(
                    notificationsEnabled = selectedDailyNotificationsEnabled,
                    systemNotificationsEnabled = systemNotificationsEnabled,
                    onNotificationsChanged = { enabled -> selectedDailyNotificationsEnabled = enabled }
                )

                // Kaydetme işlemi, üyelik durumundan önce profil ayarlarının yanında kalır.
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showSaveConfirmationDialog = true },
                    enabled = hasChanges && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A1B9A),
                        disabledContainerColor = Color(0xFF1A1A2E).copy(alpha = 0.78f),
                        disabledContentColor = Color.White.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (hasChanges) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (hasChanges) 8.dp else 0.dp
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.profile_saving),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_bold))
                            )
                        )
                    } else {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = stringResource(R.string.btn_save),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.btn_save),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_bold))
                            )
                        )
                    }
                }

                // Premium Üyelik Bilgileri
                Spacer(modifier = Modifier.height(16.dp))
                PremiumStatusCard(
                    isPremium = state.profileData.isPremium,
                    premiumProductId = state.profileData.premiumProductId,
                    premiumEndDate = state.profileData.premiumEndDate,
                    onCancelPremium = {
                        viewModel.cancelPremium(
                            onSuccess = {
                                // Başarılı iptal
                            },
                            onError = { error ->
                                // Hata
                            }
                        )
                    }
                )
                
                // KVKK / Gizlilik Politikası Linki
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    TextButton(
                        onClick = { showKvkkDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = KvkkTexts.getShortTitle(),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Hesap silme talebi uygulamanın içinden de başlatılabilmelidir.
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, Color(0xFFFF8A80).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    TextButton(
                        onClick = { showAccountDeletionDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.account_deletion_request),
                            color = Color(0xFFFFB4AB),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (state.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = Color.White)
                }
                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                
                // Alt boşluk - scroll için
                Spacer(modifier = Modifier.height(24.dp))
            }
            }
        }
        // Wheel DatePicker Dialog
        if (showDatePicker) {
            WheelDatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                onDateSelected = { day, month, year ->
                    val formattedDate = String.format("%02d.%02d.%04d", day, month + 1, year)
                    viewModel.onBirthDateChange(formattedDate)
                },
                initialYear = initialYear,
                initialMonth = initialMonth,
                initialDay = initialDay
            )
        }
        // TimePicker Dialog (klasik şekilde kalıyor)
        if (showTimePicker) {
            val now = Calendar.getInstance()
            android.app.TimePickerDialog(
                context,
                { _, hour, minute ->
                    val timeStr = String.format("%02d:%02d", hour, minute)
                    viewModel.onBirthTimeChange(timeStr)
                    showTimePicker = false
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            ).apply {
                setOnCancelListener { showTimePicker = false }
            }.show()
        }
        
        // KVKK Dialog
        if (showKvkkDialog) {
            KvkkDialog(
                onDismiss = { showKvkkDialog = false },
                onAccept = null, // Profil ekranında sadece okuma modu
                showAcceptButton = false
            )
        }

        if (showSaveConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showSaveConfirmationDialog = false },
                containerColor = Color(0xFF1A1A2E),
                title = {
                    Text(
                        text = stringResource(R.string.profile_save_confirmation_title),
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.profile_save_confirmation_message),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val languageToSave = selectedLanguage
                            val languageChanged = hasLanguageChange
                            val notificationsToSave = selectedDailyNotificationsEnabled
                            val notificationsChanged = hasNotificationChange
                            showSaveConfirmationDialog = false
                            viewModel.saveProfile(
                                onSuccess = {
                                    initialProfileData = state.profileData.copy()
                                    if (languageChanged) {
                                        LanguageManager.setLanguage(context, languageToSave)
                                        initialLanguage = languageToSave
                                    }
                                    if (notificationsChanged) {
                                        if (notificationsToSave) {
                                            DailyNotificationScheduler.enableDailyNotifications(context)
                                        } else {
                                            DailyNotificationScheduler.disableDailyNotifications(context)
                                        }
                                        initialDailyNotificationsEnabled = notificationsToSave

                                        if (notificationsToSave && !systemNotificationsEnabled) {
                                            val activity = context as? android.app.Activity
                                            if (
                                                activity != null &&
                                                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                                                !notificationManager.wasNotificationPermissionRequested()
                                            ) {
                                                notificationManager.requestNotificationPermission(activity)
                                            } else {
                                                notificationManager.openNotificationSettings()
                                            }
                                        }
                                    }
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.profile_saved),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    if (languageChanged) {
                                        (context as? android.app.Activity)?.recreate()
                                    }
                                },
                                onError = {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.profile_save_failed),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        }
                    ) {
                        Text(stringResource(R.string.profile_save_confirmation_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveConfirmationDialog = false }) {
                        Text(stringResource(R.string.profile_save_confirmation_cancel))
                    }
                }
            )
        }

        if (showAccountDeletionDialog) {
            AlertDialog(
                onDismissRequest = { showAccountDeletionDialog = false },
                containerColor = Color(0xFF1A1A2E),
                title = {
                    Text(
                        text = stringResource(R.string.account_deletion_dialog_title),
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.account_deletion_dialog_text),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showFinalAccountDeletionDialog = true
                            showAccountDeletionDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.account_deletion_continue))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAccountDeletionDialog = false }) {
                        Text(stringResource(R.string.btn_dismiss))
                    }
                }
            )
        }

        if (showFinalAccountDeletionDialog) {
            AlertDialog(
                onDismissRequest = { showFinalAccountDeletionDialog = false },
                containerColor = Color(0xFF1A1A2E),
                title = {
                    Text(
                        text = stringResource(R.string.account_deletion_final_title),
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.account_deletion_final_text),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = !isDeletingAccount,
                        onClick = {
                            showFinalAccountDeletionDialog = false
                            isDeletingAccount = true
                            coroutineScope.launch {
                                try {
                                    FirebaseFunctions
                                        .getInstance("europe-west1")
                                        .getHttpsCallable("deleteAccount")
                                        .call()
                                        .await()
                                    FirebaseAuth.getInstance().signOut()
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.account_deletion_success),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onAccountDeleted()
                                } catch (error: Exception) {
                                    android.util.Log.e("ProfileScreen", "Account deletion failed", error)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.account_deletion_failure),
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    isDeletingAccount = false
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.account_deletion_confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isDeletingAccount,
                        onClick = { showFinalAccountDeletionDialog = false }
                    ) {
                        Text(stringResource(R.string.btn_dismiss))
                    }
                }
            )
        }
    }
}

@Composable
fun NotificationSettingsCard(
    notificationsEnabled: Boolean,
    systemNotificationsEnabled: Boolean,
    onNotificationsChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
        border = BorderStroke(
            1.dp,
            if (notificationsEnabled && systemNotificationsEnabled) Color(0xFF81C784).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = if (notificationsEnabled && systemNotificationsEnabled) Color(0xFF81C784) else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.notification_settings_title),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                    )
                )
                Text(
                    text = stringResource(
                        if (notificationsEnabled && systemNotificationsEnabled) {
                            R.string.notification_settings_enabled
                        } else if (!notificationsEnabled) {
                            R.string.notification_settings_disabled
                        } else {
                            R.string.notification_settings_system_disabled
                        }
                    ),
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFFFD700),
                    checkedTrackColor = Color(0xFF6A1B9A)
                )
            )
        }
    }
}

@Composable
fun LanguageSelectionCard(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.language_label),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        color = Color.White
                    )
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageOptionChip(
                    label = stringResource(R.string.language_turkish),
                    selected = selectedLanguage == LanguageManager.TURKISH,
                    onClick = { onLanguageSelected(LanguageManager.TURKISH) }
                )
                LanguageOptionChip(
                    label = stringResource(R.string.language_english),
                    selected = selectedLanguage == LanguageManager.ENGLISH,
                    onClick = { onLanguageSelected(LanguageManager.ENGLISH) }
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color(0xFFD4AF37).copy(alpha = 0.9f) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (selected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (enabled) onValueChange(it) },
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, color = Color.White)
            }
        },
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
            focusedBorderColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White,
            disabledTextColor = Color.White,
            disabledBorderColor = Color.White.copy(alpha = 0.3f)
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ProfileDateField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, color = Color.White)
            }
        },
        enabled = false,
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
            focusedBorderColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White,
            disabledTextColor = Color.White,
            disabledBorderColor = Color.White.copy(alpha = 0.3f)
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
        )
    )
}

@Composable
fun PremiumStatusCard(
    isPremium: Boolean,
    premiumProductId: String?,
    premiumEndDate: String?,
    onCancelPremium: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isTestMode = BillingConfig.TEST_MODE

    // Gerçek abonelik Google Play üzerinden yönetilir; uygulama içinden iptal edilemez.
    fun openPlaySubscriptions() {
        val url = if (premiumProductId != null) {
            "https://play.google.com/store/account/subscriptions?sku=$premiumProductId&package=${context.packageName}"
        } else {
            "https://play.google.com/store/account/subscriptions"
        }
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            android.util.Log.e("PremiumStatusCard", "Play Store abonelik sayfası açılamadı", e)
        }
    }

    val planName = when (premiumProductId) {
        "astrosea_weekly" -> stringResource(R.string.plan_weekly)
        "astrosea_monthly" -> stringResource(R.string.plan_monthly)
        "astrosea_yearly" -> stringResource(R.string.plan_yearly)
        else -> stringResource(R.string.plan_unknown)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, if (isPremium) Color(0xFFFFD700).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Başlık
            Text(
                text = stringResource(R.string.membership_status),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                    color = Color.White
                )
            )
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
            
            // Üyelik Durumu Satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isPremium) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.status_label),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                            color = Color.White
                        )
                    )
                }
                Text(
                    text = if (isPremium) stringResource(R.string.premium_member) else stringResource(R.string.standard_member),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        color = if (isPremium) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f)
                    )
                )
            }
            
            // Premium kullanıcılar için ek bilgiler
            if (isPremium) {
                // Seçilen Plan Satırı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.plan_label),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                color = Color.White
                            )
                        )
                    }
                    Text(
                        text = planName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
                
                // Bitiş Tarihi Satırı (varsa)
                premiumEndDate?.let { endDate ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.end_date_label),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                    color = Color.White
                                )
                            )
                        }
                        Text(
                            text = endDate.take(10), // Sadece tarih kısmını göster
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Abonelik yönetimi: gerçek satın almalar Google Play'den iptal edilir,
                // test modunda ise Firestore üzerinde demo iptal yapılır.
                OutlinedButton(
                    onClick = {
                        if (isTestMode) {
                            showCancelDialog = true
                        } else {
                            openPlaySubscriptions()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF6B6B)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTestMode) stringResource(R.string.btn_cancel_demo) else stringResource(R.string.btn_manage_subscription),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                        )
                    )
                }

                if (!isTestMode) {
                    Text(
                        text = stringResource(R.string.manage_sub_hint),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
    
    // İptal Onay Dialogu
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = Color(0xFF1A1A2E),
            title = {
                Text(
                    text = stringResource(R.string.cancel_dialog_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        color = Color.White
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.cancel_dialog_text),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelPremium()
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_cancel_confirm),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                        )
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCancelDialog = false },
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = stringResource(R.string.btn_dismiss),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                            color = Color.White
                        )
                    )
                }
            }
        )
    }
}
