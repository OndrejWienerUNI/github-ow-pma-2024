package com.mitch.fontpicker.ui.screens.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.components.loading.LoadingScreen
import com.mitch.fontpicker.ui.screens.home.PAGE_PADDING_HORIZONTAL

@Composable
fun CameraScreen(viewModel: CameraViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is CameraUiState.Loading -> LoadingScreen()
        is CameraUiState.Success -> {
            val cameraData = stringResource(
                id = (uiState as CameraUiState.Success).cameraData
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PAGE_PADDING_HORIZONTAL),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = cameraData,
                    style = FontPickerDesignSystem.typography.bodyLarge,
                    color = FontPickerDesignSystem.colorScheme.onBackground
                )
            }
        }
        is CameraUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PAGE_PADDING_HORIZONTAL),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = (uiState as CameraUiState.Error).error ?: "Unknown Error",
                    style = FontPickerDesignSystem.typography.bodyLarge,
                    color = FontPickerDesignSystem.colorScheme.error
                )
            }
        }
    }
}