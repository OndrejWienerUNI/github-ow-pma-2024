package com.mitch.fontpicker.ui.designsystem.components.overlays

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding
import timber.log.Timber

private val TEXT_PADDING_HORIZONTAL = 22.dp
private val TEXT_PADDING_VERTICAL = 20.dp
private val CLOSE_ICON_SIZE = 24.dp

/**
 *         // Handle errors
 *         if (!isPreview) {
 *             if (uiState is CameraUiState.Error) {
 *                 val errorMessage: String? = (uiState as CameraUiState.Error).error
 *                 if (errorMessage is String) {
 *                     ErrorOverlay(
 *                         errorMessage = errorMessage,
 *                         closable = true,
 *                         verticalBias = 0.1f,
 *                         onClose = {
 *                             Timber.i("ErrorOverlay closed. Resetting error state.")
 *                             viewModel.resetErrorState()
 *                         }
 *                     )
 *                 }
 *             }
 *         }
 */

@Composable
fun ErrorOverlay(
    errorMessage: String,
    closable: Boolean = false,
    onClose: (() -> Unit)? = null,
    verticalBias: Float = 0.0f // Default position is center
) {
    // Log a warning if onClose is provided but the overlay is not closable
    LaunchedEffect(closable, onClose) {
        if (!closable && onClose != null) {
            Timber.w("ErrorOverlay: 'onClose' callback provided, " +
                    "but 'closable' is set to false. The callback will never be executed.")
        }
    }

    val isOverlayVisible = remember { mutableStateOf(true) }
    val currentOnClose = rememberUpdatedState(onClose)

    // Handle back button/gesture if closable
    if (closable) {
        BackHandler(enabled = isOverlayVisible.value) {
            Timber.i("ErrorOverlay closed via back gesture.")
            isOverlayVisible.value = false
            currentOnClose.value?.invoke()
        }
    }

    if (isOverlayVisible.value) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val density = LocalDensity.current
        val offsetInDp = with(density) { -(verticalBias * screenHeight.toPx()).toDp() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FontPickerDesignSystem.colorScheme.background.copy(alpha = 0.5F))
        ) {
            // Center the error box slightly above the center using offset
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding.large)
                    .offset(y = offsetInDp)
                    .wrapContentSize()
                    .background(
                        color = FontPickerDesignSystem.colorScheme.surface,
                        shape = FontPickerDesignSystem.shapes.medium,
                    )
                    .border(
                        width = 1.dp,
                        color = FontPickerDesignSystem.extendedColorScheme.borders,
                        shape = FontPickerDesignSystem.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(
                            horizontal = TEXT_PADDING_HORIZONTAL, vertical = TEXT_PADDING_VERTICAL
                        )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(padding.medium)
                    ) {
                        // Row containing the Error label and the close icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Error label on the left
                            Text(
                                text = "Error",
                                color = FontPickerDesignSystem.colorScheme.onSurface,
                                style = FontPickerDesignSystem.typography.titleMedium,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )

                            // Close button on the right
                            if (closable) {
                                Icon(
                                    imageVector = FontPickerIcons.Outlined.Close,
                                    contentDescription = "Close",
                                    tint = FontPickerDesignSystem.colorScheme.onSurface,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .size(CLOSE_ICON_SIZE)
                                        .clickable {
                                            Timber.i("ErrorOverlay closed via close button.")
                                            isOverlayVisible.value = false
                                            currentOnClose.value?.invoke()
                                        }
                                )
                            }
                        }

                        Text(
                            text = errorMessage,
                            textAlign = TextAlign.Left,
                            color = FontPickerDesignSystem.colorScheme.error,
                            style = FontPickerDesignSystem.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}


@Composable
@PreviewLightDark
fun ErrorOverlayPreview() {
    FontPickerTheme {
        ErrorOverlay(
            errorMessage = "This is a sample error message.",
            closable = true,
            onClose = { Timber.i("Error overlay dismissed in preview.") }
        )
    }
}
