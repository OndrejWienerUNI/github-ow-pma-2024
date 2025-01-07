package com.mitch.fontpicker.ui.screens.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme
import com.mitch.fontpicker.ui.screens.camera.components.CameraActionRow
import com.mitch.fontpicker.ui.screens.home.PAGE_PADDING_HORIZONTAL

private val TOP_PADDING = 68.dp
private val LIVE_VIEW_CORNER_RADIUS = 16.dp
private val LIVE_VIEW_BORDER_WIDTH = 1.dp

@Composable
fun CameraScreen(
    viewModel: CameraViewModel
) {
    val uiState = viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FontPickerDesignSystem.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PAGE_PADDING_HORIZONTAL)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = TOP_PADDING) // Dynamic top padding
            ) {

                // TODO: Separate cameraLiveView when functionality is added
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3 / 4f)
                        .background(
                            color = FontPickerDesignSystem.colorScheme.background,
                            shape = RoundedCornerShape(LIVE_VIEW_CORNER_RADIUS)
                        )
                        .border(
                            width = LIVE_VIEW_BORDER_WIDTH,
                            color = FontPickerDesignSystem.extendedColorScheme.borders,
                            shape = RoundedCornerShape(LIVE_VIEW_CORNER_RADIUS)
                        ),
                )

                Spacer(modifier = Modifier.weight(1f))

                CameraActionRow(
                    onShoot = { viewModel.shootPhoto() },
                    onGallery = { viewModel.openGallery() },
                    onFlip = { viewModel.flipCamera() },
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Temporary suppression for the state
        when (@Suppress("UNUSED_VARIABLE") val state = uiState.value) {
            is CameraUiState.Loading -> {
                // Display a loading indicator if needed
            }
            is CameraUiState.Success -> {
                // Show success message or toast
            }
            is CameraUiState.Error -> {
                // Show error message or retry UI
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CameraScreenPreview() {
    FontPickerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FontPickerDesignSystem.colorScheme.background)
        ) {
            CameraScreen(
                // DON'T EVER DO THIS, IT JUST WORKS FOR THE PREVIEW
                viewModel = CameraViewModel()
            )
        }
    }
}
