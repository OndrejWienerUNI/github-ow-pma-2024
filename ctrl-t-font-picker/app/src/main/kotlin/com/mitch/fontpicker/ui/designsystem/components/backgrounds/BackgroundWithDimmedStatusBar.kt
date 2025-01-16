package com.mitch.fontpicker.ui.designsystem.components.backgrounds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem

// Place this on top of a screen to ensure background consistency and a tinted status bar
@Composable
fun BackgroundWithDimmedStatusBar() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FontPickerDesignSystem.colorScheme.background)
    )

    Box(
        modifier = Modifier
            .zIndex(1f)
            .background(
                FontPickerDesignSystem.colorScheme.background.copy(alpha = 0.2f)
            )
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
    )
}