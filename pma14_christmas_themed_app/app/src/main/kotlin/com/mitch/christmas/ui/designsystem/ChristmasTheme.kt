package com.mitch.christmas.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Added color scheme on top of the default light and dark schemes
val ChristmasRed = Color(0xFFC22424)
val ChristmasGreen = Color(0xFF12863D)
val ChristmasLightGray = Color(0xFFAFAFAF)
val ChristmasDarkGray = Color(0xFF343434)
val ChristmasGold = Color(0xFFFFD700)


val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = ChristmasRed,
    secondary = ChristmasGreen,
    tertiary = ChristmasGold,
    background = ChristmasDarkGray,
    surface = ChristmasDarkGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = ChristmasRed,
    secondary = ChristmasGreen,
    tertiary = ChristmasGold,
    background = Color.White,
    surface = ChristmasLightGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)


// Use ChristmasDesignSystem instead of MaterialTheme everywhere
typealias ChristmasDesignSystem = MaterialTheme

@Composable
fun ChristmasTheme(
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
