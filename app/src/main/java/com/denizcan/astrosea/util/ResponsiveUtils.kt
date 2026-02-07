package com.denizcan.astrosea.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Ekran boyutu kategorileri
 */
enum class ScreenSize {
    COMPACT,    // < 360dp (küçük telefonlar)
    MEDIUM,     // 360-600dp (normal telefonlar)
    EXPANDED    // > 600dp (tabletler, büyük telefonlar)
}

/**
 * Mevcut ekran boyutu kategorisini döndürür
 */
@Composable
fun getScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp < 360 -> ScreenSize.COMPACT
        configuration.screenWidthDp < 600 -> ScreenSize.MEDIUM
        else -> ScreenSize.EXPANDED
    }
}

/**
 * Ekran genişliğini dp olarak döndürür
 */
@Composable
fun screenWidth(): Dp {
    return LocalConfiguration.current.screenWidthDp.dp
}

/**
 * Ekran yüksekliğini dp olarak döndürür
 */
@Composable
fun screenHeight(): Dp {
    return LocalConfiguration.current.screenHeightDp.dp
}

/**
 * Ekran boyutuna göre responsive boyut döndürür
 */
@Composable
fun responsiveSize(
    compact: Dp,
    medium: Dp = compact,
    expanded: Dp = medium
): Dp {
    return when (getScreenSize()) {
        ScreenSize.COMPACT -> compact
        ScreenSize.MEDIUM -> medium
        ScreenSize.EXPANDED -> expanded
    }
}

/**
 * Ekran genişliğinin yüzdesine göre boyut döndürür
 */
@Composable
fun widthPercent(fraction: Float, maxSize: Dp = Dp.Unspecified): Dp {
    val calculated = screenWidth() * fraction
    return if (maxSize != Dp.Unspecified && calculated > maxSize) maxSize else calculated
}

/**
 * Ekran yüksekliğinin yüzdesine göre boyut döndürür
 */
@Composable
fun heightPercent(fraction: Float, maxSize: Dp = Dp.Unspecified): Dp {
    val calculated = screenHeight() * fraction
    return if (maxSize != Dp.Unspecified && calculated > maxSize) maxSize else calculated
}

/**
 * Ekran boyutuna göre responsive font boyutu döndürür
 */
@Composable
fun responsiveFontSize(
    compact: TextUnit,
    medium: TextUnit = compact,
    expanded: TextUnit = medium
): TextUnit {
    return when (getScreenSize()) {
        ScreenSize.COMPACT -> compact
        ScreenSize.MEDIUM -> medium
        ScreenSize.EXPANDED -> expanded
    }
}

/**
 * Ekran boyutuna göre responsive padding döndürür
 */
@Composable
fun responsivePadding(
    compact: Dp = 12.dp,
    medium: Dp = 16.dp,
    expanded: Dp = 24.dp
): Dp {
    return when (getScreenSize()) {
        ScreenSize.COMPACT -> compact
        ScreenSize.MEDIUM -> medium
        ScreenSize.EXPANDED -> expanded
    }
}

/**
 * Küçük ekranlarda içeriğin sığması için ölçekleme faktörü
 */
@Composable
fun scaleFactor(): Float {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp < 320 -> 0.75f
        configuration.screenWidthDp < 360 -> 0.85f
        configuration.screenWidthDp < 400 -> 0.95f
        else -> 1f
    }
}

/**
 * Boyutu ekran ölçeğine göre ayarlar
 */
@Composable
fun Dp.scaled(): Dp {
    return this * scaleFactor()
}

/**
 * Ekranın küçük olup olmadığını kontrol eder
 */
@Composable
fun isCompactScreen(): Boolean {
    return LocalConfiguration.current.screenWidthDp < 360
}

/**
 * Ekranın yüksekliğinin küçük olup olmadığını kontrol eder
 */
@Composable
fun isShortScreen(): Boolean {
    return LocalConfiguration.current.screenHeightDp < 640
}
