package com.mitch.fontpicker.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


val ColCerulean = Color(0xFF0C8BB7)
val ColSkyBlue = Color(0xFF13CDF6)
val ColBusYellow = Color(0xFFF8D525)

val ColWhiteSmoke = Color(0xFFF5F5F5)
val ColBloodRed = Color(0xFF6E0E0D)

val ColOnyx = Color(0xFF2C2C2C)
val ColRed = Color(0xFFDD1C1A)


val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = ColSkyBlue,
    secondary = ColSkyBlue,
    tertiary = ColBusYellow,
    background = ColOnyx,
    surface = ColRed,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = ColCerulean,
    secondary = ColCerulean,
    tertiary = ColBusYellow,
    background = ColWhiteSmoke,
    surface = ColBloodRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)


// Use ChristmasDesignSystem instead of MaterialTheme everywhere, NOT ChristmasTheme
typealias FontPickerDesignSystem = MaterialTheme

@Composable
fun FontPickerTheme(
    isThemeDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isThemeDark) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
