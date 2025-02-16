package com.denizcan.astrosea.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.denizcan.astrosea.R

private val cinzelFamily = FontFamily(
    Font(R.font.cinzel_regular, FontWeight.Normal),
    Font(R.font.cinzel_bold, FontWeight.Bold)
)

private val cormorantFamily = FontFamily(
    Font(R.font.cormorantgaramond_regular, FontWeight.Normal),
    Font(R.font.cormorantgaramond_bold, FontWeight.Bold)
)

val AstroSeaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = cinzelFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = cinzelFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = cinzelFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = cinzelFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = cinzelFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = cinzelFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = cinzelFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = cinzelFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        fontWeight = FontWeight.Normal
    ),
    titleSmall = TextStyle(
        fontFamily = cinzelFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyLarge = TextStyle(
        fontFamily = cormorantFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = cormorantFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = cormorantFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = cinzelFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.Normal
    ),
    labelMedium = TextStyle(
        fontFamily = cinzelFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Normal
    ),
    labelSmall = TextStyle(
        fontFamily = cinzelFamily,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Normal
    )
)