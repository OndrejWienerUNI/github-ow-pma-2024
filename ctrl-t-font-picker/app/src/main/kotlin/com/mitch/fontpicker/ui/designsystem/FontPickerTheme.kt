package com.mitch.fontpicker.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


val ColCerulean = Color(0xFF0E7498)
val ColSkyBlue = Color(0xFF13CDF6)
val ColGray = Color(0xFF999999)
val ColWhiteSmoke = Color(0xFFF5F5F5)
val ColOnyx = Color(0xFF1A1B20)


val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = ColSkyBlue,
    secondary = ColSkyBlue,
    tertiary = ColGray,
    background = ColOnyx,
    surface = ColOnyx,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = ColCerulean,
    secondary = ColCerulean,
    tertiary = ColGray,
    background = ColWhiteSmoke,
    surface = ColWhiteSmoke,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)


// Use FontPickerDesignSystem instead of MaterialTheme everywhere, NOT FontPickerTheme
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
