package com.mitch.fontpicker.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mitch.fontpicker.ui.designsystem.theme.custom.DarkExtendedColorScheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.LightExtendedColorScheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.LocalExtendedColorScheme


val ColCerulean = Color(0xFF0E7498)
val ColSkyBlue = Color(0xFF13CDF6)
val ColLightGray = Color(0xFF999999)
val ColDarkGray = Color(0xFF6E6E6E)
val ColWhiteSmoke = Color(0xFFF5F5F5)
val ColOnyx = Color(0xFF0F0F15)


val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = ColSkyBlue,
    primaryContainer = ColSkyBlue,
    secondary = ColSkyBlue,
    tertiary = ColLightGray,
    background = ColOnyx,
    surface = ColOnyx,
    onPrimary = Color.Black,
    onPrimaryContainer = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = ColCerulean,
    primaryContainer = ColCerulean,
    secondary = ColCerulean,
    tertiary = ColDarkGray,
    background = ColWhiteSmoke,
    surface = ColWhiteSmoke,
    onPrimary = Color.White,
    onPrimaryContainer = Color.White,
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
    LocalContext.current

    val colorScheme = if (isThemeDark) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val extendedColorScheme = if (isThemeDark) {
        DarkExtendedColorScheme
    } else {
        LightExtendedColorScheme
    }

    CompositionLocalProvider(
        LocalExtendedColorScheme provides extendedColorScheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}


