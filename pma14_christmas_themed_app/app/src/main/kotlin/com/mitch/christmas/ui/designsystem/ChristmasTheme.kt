package com.mitch.christmas.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Added color scheme on top of the default light and dark schemes
val ChristmasRedLight = Color(0xFFFF475E)
val ChristmasRedDark = Color(0xFFC22424)

val ChristmasGreenLight = Color(0xFF6AFF6F)
val ChristmasGreenDark = Color(0xFF128617)

val ChristmasLightGray = Color(0xFFAFAFAF)
val ChristmasDarkGray = Color(0xFF343434)

val ChristmasGold = Color(0xFFFFD700)


val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = ChristmasRedLight,
    secondary = ChristmasGreenLight,
    tertiary = ChristmasGold,
    background = ChristmasDarkGray,
    surface = ChristmasDarkGray,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = ChristmasRedDark,
    secondary = ChristmasGreenDark,
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
