package com.denizcan.astrosea.presentation.profileCompletion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.denizcan.astrosea.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCompletionScreen3(
    viewModel: ProfileCompletionViewModel = viewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val birthCountry by viewModel.birthCountry.collectAsState()
    val birthCity by viewModel.birthCity.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Ülke ve şehir listeleri
    val countryList = listOf("Türkiye", "Almanya")
    val turkeyCities = listOf(
        "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin", "Aydın", "Balıkesir", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale", "Çankırı", "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum", "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkari", "Hatay", "Isparta", "Mersin", "İstanbul", "İzmir", "Kars", "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir", "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa", "Kahramanmaraş", "Mardin", "Muğla", "Muş", "Nevşehir", "Niğde", "Ordu", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop", "Sivas", "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Şanlıurfa", "Uşak", "Van", "Yozgat", "Zonguldak", "Aksaray", "Bayburt", "Karaman", "Kırıkkale", "Batman", "Şırnak", "Bartın", "Ardahan", "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye", "Düzce"
    )
    val germanyCities = listOf(
        "Berlin", "Hamburg", "Münih", "Köln", "Frankfurt", "Stuttgart", "Düsseldorf", "Dortmund", "Essen", "Leipzig", "Bremen", "Dresden", "Hannover", "Nürnberg", "Duisburg", "Bochum", "Wuppertal", "Bielefeld", "Bonn", "Münster", "Karlsruhe", "Mannheim", "Augsburg", "Wiesbaden", "Gelsenkirchen", "Mönchengladbach", "Braunschweig", "Chemnitz", "Kiel", "Aachen", "Halle", "Magdeburg", "Freiburg", "Krefeld", "Lübeck", "Oberhausen", "Erfurt", "Mainz", "Rostock", "Kassel", "Hagen", "Hamm", "Saarbrücken", "Mülheim", "Potsdam", "Ludwigshafen", "Oldenburg", "Leverkusen", "Osnabrück", "Solingen", "Heidelberg", "Herne", "Neuss", "Darmstadt", "Paderborn", "Regensburg", "Ingolstadt", "Würzburg", "Wolfsburg", "Offenbach", "Ulm", "Heilbronn", "Pforzheim", "Göttingen", "Bottrop", "Trier", "Recklinghausen", "Reutlingen", "Bremerhaven", "Koblenz", "Bergisch Gladbach", "Jena", "Remscheid", "Erlangen", "Moers", "Siegen", "Hildesheim", "Salzgitter"
    )
    
    val cityList = when (birthCountry) {
        "Almanya" -> germanyCities
        else -> turkeyCities
    }
    
    var countryExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.anabackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Başlık
            Text(
                text = "Yıldızlar kaderinizi şekillendirsin.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Ülke Dropdown - Kendi kutusunda
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = countryExpanded,
                    onExpandedChange = { countryExpanded = !countryExpanded }
                ) {
                    OutlinedTextField(
                        value = birthCountry,
                        onValueChange = {},
                        readOnly = true,
                        label = { 
                            Text(
                                "Doğduğunuz ülke...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                    fontSize = 18.sp
                                ),
                                color = Color.White.copy(alpha = 0.7f)
                            ) 
                        },
                        trailingIcon = { 
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) 
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White,
                            focusedBorderColor = Color.White.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = countryExpanded,
                        onDismissRequest = { countryExpanded = false }
                    ) {
                        countryList.forEach { country ->
                            DropdownMenuItem(
                                text = { Text(country) },
                                onClick = {
                                    viewModel.updateBirthCountry(country)
                                    // Ülke değiştiğinde şehri temizle
                                    viewModel.updateBirthCity("")
                                    countryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Şehir Dropdown - Kendi kutusunda
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = cityExpanded,
                    onExpandedChange = { cityExpanded = !cityExpanded }
                ) {
                    OutlinedTextField(
                        value = birthCity,
                        onValueChange = {},
                        readOnly = true,
                        label = { 
                            Text(
                                "Doğduğunuz şehir...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                    fontSize = 18.sp
                                ),
                                color = Color.White.copy(alpha = 0.7f)
                            ) 
                        },
                        trailingIcon = { 
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) 
                        },
                        singleLine = true,
                        enabled = birthCountry.isNotEmpty(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                            fontSize = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White.copy(alpha = 0.5f),
                            focusedBorderColor = Color.White.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            disabledBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = cityExpanded,
                        onDismissRequest = { cityExpanded = false }
                    ) {
                        cityList.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    viewModel.updateBirthCity(city)
                                    cityExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Butonlar - Siyah arka plan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Geri butonu
                Button(
                    onClick = onNavigateBack,
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Geri",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }

                // Tamamla butonu
                Button(
                    onClick = {
                        if (birthCountry.isNotBlank() && birthCity.isNotBlank()) {
                            scope.launch {
                                val success = viewModel.saveLocationData()
                                if (success) {
                                    onNavigateToHome()
                                }
                            }
                        }
                    },
                    enabled = birthCountry.isNotBlank() && birthCity.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.7f),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Tamamla",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileCompletionScreen3Preview() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Arka plan için placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1a0033),
                            Color(0xFF2d1b69),
                            Color(0xFF4a2f8f)
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Başlık
            Text(
                text = "Yıldızlar kaderinizi şekillendirsin.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Ülke Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { 
                        Text(
                            "Doğduğunuz ülke...",
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        ) 
                    },
                    readOnly = true,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.8f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Şehir Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { 
                        Text(
                            "Doğduğunuz şehir...",
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        ) 
                    },
                    readOnly = true,
                    singleLine = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White.copy(alpha = 0.5f),
                        focusedBorderColor = Color.White.copy(alpha = 0.8f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        disabledBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Butonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Geri butonu
                Button(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Geri",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Tamamla butonu
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.7f),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Tamamla",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
