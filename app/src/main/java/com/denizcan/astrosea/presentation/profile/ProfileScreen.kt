package com.denizcan.astrosea.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.text.SimpleDateFormat
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

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
    // İlk yüklenen profil verisini sakla
    val initialProfileData = remember { state.profileData.copy() }
    // Değişiklik kontrolü
    val isChanged = state.profileData != initialProfileData
    // Material3 DatePicker için state
    val datePickerState = rememberDatePickerState()

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
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
            }
        }
        // Material3 DatePicker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val millis = datePickerState.selectedDateMillis
                            if (millis != null) {
                                val date = java.util.Date(millis)
                                val format = SimpleDateFormat("dd.MM.yyyy")
                                viewModel.onBirthDateChange(format.format(date))
                            }
                            showDatePicker = false
                        }
                    ) { Text("Tamam") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
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