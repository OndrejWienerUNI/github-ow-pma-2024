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
val ChristmasLightGreen = Color(0xFF18C058)
val ChristmasDarkGreen = Color(0xFF05421E)
val ChristmasLightGray = Color(0xFFE8E8E8)
val ChristmasDarkGray = Color(0xFF343434)
val ChristmasGold = Color(0xFFFFD700)


val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = ChristmasRed,
    secondary = ChristmasDarkGreen,
    tertiary = ChristmasGold,
    background = ChristmasDarkGreen,
    surface = ChristmasDarkGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = ChristmasRed,
    secondary = ChristmasLightGreen,
    tertiary = ChristmasGold,
    background = ChristmasLightGreen,
    surface = ChristmasLightGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)


// Use ChristmasDesignSystem instead of MaterialTheme everywhere, NOT ChristmasTheme
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
