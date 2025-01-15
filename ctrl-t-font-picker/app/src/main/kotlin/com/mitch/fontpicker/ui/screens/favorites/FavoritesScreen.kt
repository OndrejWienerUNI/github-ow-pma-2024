package com.mitch.fontpicker.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.components.loading.LoadingScreen
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding
import timber.log.Timber

@Composable
fun FavoritesRoute(
    viewModel: FavoritesViewModel
) {
    Timber.d("Rendering CameraScreenRoute.")
    FavoritesScreen(
        viewModel = viewModel
    )
}

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel
) {
//    val isPreview = LocalInspectionMode.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreenContent(uiState = uiState)
}

@Composable
fun FavoritesScreenContent(
    uiState: FavoritesUiState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is FavoritesUiState.Loading -> LoadingScreen()
        is FavoritesUiState.Success -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = padding.medium),
                verticalArrangement = Arrangement.spacedBy(padding.small),
            ) {
                items(uiState.fontPreviews) { fontPreview ->
                    Text(
                        text = stringResource(id = fontPreview),
                        style = FontPickerDesignSystem.typography.bodyLarge,
                        color = FontPickerDesignSystem.colorScheme.onBackground,
                        modifier = Modifier.padding(padding.extraSmall)
                    )
                }
            }
        }
        is FavoritesUiState.Error -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = padding.medium),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.error ?: "Unknown Error",
                    style = FontPickerDesignSystem.typography.bodyLarge,
                    color = FontPickerDesignSystem.colorScheme.error
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun FavoritesScreenContentPreview() {
    FontPickerTheme {
        val mockUiState = FavoritesUiState.Success(
            fontPreviews = listOf(
                android.R.string.ok,
                android.R.string.cancel,
                android.R.string.untitled
            )
        )
        FavoritesScreenContent(
            uiState = mockUiState
        )
    }
}
