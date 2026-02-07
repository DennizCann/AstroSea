package com.denizcan.astrosea.presentation.profile

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import com.denizcan.astrosea.presentation.components.WheelDatePickerDialog
import com.denizcan.astrosea.util.responsiveSize
import com.denizcan.astrosea.util.responsivePadding
import com.denizcan.astrosea.presentation.components.KvkkDialog
import com.denizcan.astrosea.util.KvkkTexts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state = viewModel.profileState
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showKvkkDialog by remember { mutableStateOf(false) }
    // İlk yüklenen profil verisini sakla
    val initialProfileData = remember { state.profileData.copy() }
    // Değişiklik kontrolü
    val isChanged = state.profileData != initialProfileData
    
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
                    title = "Profil",
                    onBackClick = onNavigateBack
                )
            },
            bottomBar = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(bottom = 8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveProfile {
                                onNavigateBack()
                            }
                        },
                        enabled = isChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isChanged) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Kaydet", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Kaydet",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
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
                            label = "Ad",
                            value = state.profileData.name,
                            icon = Icons.Default.Person,
                            enabled = true,
                            onValueChange = { viewModel.onNameChange(it) }
                        )
                        ProfileField(
                            label = "Soyad",
                            value = state.profileData.surname,
                            icon = Icons.Default.Person,
                            enabled = true,
                            onValueChange = { viewModel.onSurnameChange(it) }
                        )
                        ProfileDateField(
                            label = "Doğum Tarihi",
                            value = state.profileData.birthDate,
                            icon = Icons.Default.DateRange,
                            enabled = true,
                            onClick = { showDatePicker = true }
                        )
                        ProfileDateField(
                            label = "Doğum Saati",
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
                                        Text("Ülke", color = Color.White)
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
                                        Text("Şehir", color = Color.White)
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
    
    // Plan adını Türkçe'ye çevir
    val planName = when (premiumProductId) {
        "astrosea_weekly" -> "Haftalık Plan"
        "astrosea_monthly" -> "Aylık Plan"
        "astrosea_yearly" -> "Yıllık Plan"
        else -> "Bilinmiyor"
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
                text = "Üyelik Durumu",
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
                        text = "Durum",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                            color = Color.White
                        )
                    )
                }
                Text(
                    text = if (isPremium) "Premium Üye ✨" else "Standart Üye",
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
                            text = "Plan",
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
                                text = "Bitiş Tarihi",
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
                
                // Üyeliği İptal Et Butonu
                OutlinedButton(
                    onClick = { showCancelDialog = true },
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
                        text = "Üyeliği İptal Et (Demo)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular))
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
                    text = "Üyeliği İptal Et",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                        color = Color.White
                    )
                )
            },
            text = {
                Text(
                    text = "Premium üyeliğinizi iptal etmek istediğinizden emin misiniz?\n\nBu işlem sonrasında premium özelliklere erişiminiz sona erecektir.",
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
                        text = "İptal Et",
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
                        text = "Vazgeç",
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