package com.mitch.fontpicker.ui.screens.favorites.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding
import androidx.compose.material3.Text
import timber.log.Timber

private val GRADIENT_HEIGHT = 10.dp
private val TOP_SPACING = 26.dp
private val BOTTOM_SPACING = 100.dp

@Composable
fun ListEndText(
    text: String,
    modifier: Modifier = Modifier
) {
    val trimmedEndText = text.trim()
    if (trimmedEndText.isNotEmpty()) {

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(FontPickerDesignSystem.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Spacer
            Spacer(
                modifier = modifier
                    .fillMaxWidth()
                    .height(TOP_SPACING)
            )

            // Gradient above text
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(GRADIENT_HEIGHT)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                FontPickerDesignSystem.colorScheme.surface.copy(alpha = 1f),
                                FontPickerDesignSystem.colorScheme.surface.copy(alpha = 0f)
                            )
                        )
                    )
            )

            // Centered Text
            Text(
                text = trimmedEndText,
                style = FontPickerDesignSystem.typography.bodyMedium,
                color = FontPickerDesignSystem.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                modifier = modifier
                    .padding(horizontal = padding.large)
            )

            // Gradient below text
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(GRADIENT_HEIGHT)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                FontPickerDesignSystem.colorScheme.surface.copy(alpha = 0f),
                                FontPickerDesignSystem.colorScheme.surface.copy(alpha = 1f)
                            )
                        )
                    )
            )

            // Bottom Spacer
            Spacer(
                modifier = modifier
                    .fillMaxWidth()
                    .height(BOTTOM_SPACING)
            )
        }
    } else {
        Timber.w("ListEndText was composed without any text given. " +
                "This results in nothing getting rendered.")
    }
}
