package com.mitch.christmas.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val DarkColorScheme: ColorScheme = darkColorScheme()
val LightColorScheme: ColorScheme = lightColorScheme()

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
