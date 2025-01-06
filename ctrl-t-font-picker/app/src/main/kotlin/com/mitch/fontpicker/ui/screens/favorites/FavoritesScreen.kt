package com.mitch.fontpicker.ui.screens.favorites

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
fun FavoritesScreen(viewModel: FavoritesViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is FavoritesUiState.Loading -> LoadingScreen()
        is FavoritesUiState.Success -> {
            val favoritesTitle = stringResource(
                id = (uiState as FavoritesUiState.Success).fontPreviews.first()
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
                    text = favoritesTitle,
                    style = FontPickerDesignSystem.typography.bodyLarge,
                    color = FontPickerDesignSystem.colorScheme.onBackground
                )
            }
        }
        is FavoritesUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PAGE_PADDING_HORIZONTAL),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = (uiState as FavoritesUiState.Error).error ?: "Unknown Error",
                    style = FontPickerDesignSystem.typography.bodyLarge,
                    color = FontPickerDesignSystem.colorScheme.error
                )
            }
        }
    }
}