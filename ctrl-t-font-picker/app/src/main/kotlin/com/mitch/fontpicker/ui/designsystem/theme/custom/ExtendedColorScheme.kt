package com.mitch.fontpicker.ui.designsystem.theme.custom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem

@Immutable
data class ExtendedColorScheme(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val redAccent: Color,
    val onRedAccent: Color,
    val icOnBackground: Color,
    val icOnBackgroundPressed: Color,
    val borders: Color,
    val pictureBackground: Color,
    val cardSurface: Color
)

val DarkExtendedColorScheme: ExtendedColorScheme = ExtendedColorScheme(
    success = Color(0xFF4EBD26),
    onSuccess = Color(0xFF1A1C1E),
    warning = Color(0xFFDAD741),
    onWarning = Color(0xFF1A1C1E),
    redAccent = Color(0xFFD50000),
    onRedAccent = Color(0xFFFFFFFF),
    icOnBackground = Color(0xFFFFFFFF),
    icOnBackgroundPressed = Color(0xFFC4C4C4),
    borders = Color(0xFF727272),
    pictureBackground = Color(0xFF000000),
    cardSurface = Color(0xFF000000)
)

val LightExtendedColorScheme: ExtendedColorScheme = ExtendedColorScheme(
    success = Color(0xFF395F27),
    onSuccess = Color(0xFFFDFCFF),
    warning = Color(0xFFE1DE33),
    onWarning = Color(0xFF424242),
    redAccent = Color(0xFFD50000),
    onRedAccent = Color(0xFFFFFFFF),
    icOnBackground = Color(0xFF757575),
    icOnBackgroundPressed = Color(0xFF606060),
    borders = Color(0xFFB4B4B4),
    pictureBackground = Color(0xFF000000),
    cardSurface = Color(0xFFFFFFFF)
)

val LocalExtendedColorScheme: ProvidableCompositionLocal<ExtendedColorScheme> =
    staticCompositionLocalOf { error("No ExtendedColorScheme provided") }

@Suppress("UnusedReceiverParameter")
val FontPickerDesignSystem.extendedColorScheme: ExtendedColorScheme
    @Composable
    get() = LocalExtendedColorScheme.current
