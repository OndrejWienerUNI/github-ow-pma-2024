package com.mitch.fontpicker.ui.screens.gallery

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
fun GalleryScreen(viewModel: GalleryViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is GalleryUiState.Loading -> LoadingScreen()
        is GalleryUiState.Success -> {
            val galleryTitle = stringResource(
                id = (uiState as GalleryUiState.Success).fontPreviews.first()
            )
            // Centering the text
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PAGE_PADDING_HORIZONTAL),
                verticalArrangement = Arrangement.Center, // Center vertically
                horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
            ) {
                Text(
                    text = galleryTitle,
                    style = FontPickerDesignSystem.typography.bodyLarge,
                    color = FontPickerDesignSystem.colorScheme.onBackground
                )
            }
        }
        is GalleryUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PAGE_PADDING_HORIZONTAL),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = (uiState as GalleryUiState.Error).error ?: "Unknown Error",
                    style = FontPickerDesignSystem.typography.bodyLarge,
                    color = FontPickerDesignSystem.colorScheme.error
                )
            }
        }
    }
}