package com.mitch.fontpicker.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem

private val ERROR_BG_PADDING = 30.dp
private val ERROR_TEXT_PADDING = 22.dp

@Composable
fun ErrorDisplayBox(
    errorMessage: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FontPickerDesignSystem.colorScheme.background.copy(alpha = 0.7f))
            .padding(ERROR_BG_PADDING),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $errorMessage",
            textAlign = TextAlign.Center,
            color = FontPickerDesignSystem.colorScheme.error,
            style = FontPickerDesignSystem.typography.titleMedium,
            modifier = Modifier
                .background(
                    color = FontPickerDesignSystem.colorScheme.surface,
                    shape = FontPickerDesignSystem.shapes.medium
                )
                .padding(ERROR_TEXT_PADDING)
        )
    }
}