package com.mitch.fontpicker.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.components.loading.LoadingScreen
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape

// Constants
private val PLACEHOLDER_CORNER_RADIUS = 16.dp
private val PLACEHOLDER_BORDER_WIDTH = 1.dp
private const val PLACEHOLDER_ASPECT_RATIO = 3 / 4f

@Composable
fun CameraLiveViewPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(PLACEHOLDER_CORNER_RADIUS)
) {
    Box(
        modifier = modifier
            .aspectRatio(PLACEHOLDER_ASPECT_RATIO)
            .background(
                color = FontPickerDesignSystem.colorScheme.surface,
                shape = shape
            )
            .border(
                width = PLACEHOLDER_BORDER_WIDTH,
                color = FontPickerDesignSystem.extendedColorScheme.borders,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        LoadingScreen(modifier = Modifier.fillMaxSize())
    }
}
