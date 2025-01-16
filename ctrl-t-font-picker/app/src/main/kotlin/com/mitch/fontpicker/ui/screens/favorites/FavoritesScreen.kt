package com.mitch.fontpicker.ui.screens.favorites

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitch.fontpicker.data.api.FontDownloaded
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.screens.favorites.components.FontCardListScreenContent
import com.mitch.fontpicker.ui.screens.favorites.components.FontCardListUiState
import timber.log.Timber


@Composable
fun FavoritesRoute(
    viewModel: FavoritesViewModel
) {
    Timber.d("Rendering FavoritesRoute.")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Timber.d("Current uiState: $uiState")

    FavoritesScreen(
        uiState = uiState,
        onToggleLike = { font ->
            Timber.d("onToggleLike called for font: ${font.title} with id: ${font.id}")
            viewModel.toggleLike(font)
        },
        onRenderStart = {
            viewModel.startObservingFavorites(lastToFirst = true)
        },
        onRetry = {
            viewModel.startObservingFavorites(lastToFirst = true)
        },
    )
}

@Composable
fun FavoritesScreen(
    uiState: FontCardListUiState,
    onToggleLike: (FontDownloaded) -> Unit,
    onRenderStart: () -> Unit,
    onRetry: () -> Unit
) {
    Timber.d("FavoritesScreen rendering with uiState: $uiState")

    // Persist state across configuration changes or process recreation
    val hasRendered = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasRendered.value) {
            Timber.d("Starting to observe favorites on initial screen load.")
            onRenderStart()
            hasRendered.value = true
        }
    }

    FavoritesScreenContent(
        uiState = uiState,
        onToggleLike = onToggleLike,
        onRetry = onRetry
    )
}

@Composable
fun FavoritesScreenContent(
    uiState: FontCardListUiState,
    onToggleLike: (FontDownloaded) -> Unit,
    onRetry: () -> Unit
){
    FontCardListScreenContent(
        uiState = uiState,
        onToggleLike = onToggleLike,
        onRetry = onRetry
    )
}


@PreviewLightDark
@Composable
fun FavoritesScreenContentPreview() {
    FontPickerTheme {
        val sampleBitmap = Bitmap.createBitmap(500, 120, Bitmap.Config.ARGB_8888)
        val mockFonts = List(5) {
            FontDownloaded(
                title = "Mock Font ${it + 1}",
                url = "https://example.com/font/${it + 1}",
                imageUrls = emptyList(),
                bitmaps = listOf(sampleBitmap, sampleBitmap, sampleBitmap),
                isLiked = mutableStateOf(false)
            )
        }

        val mockUiState = FontCardListUiState.Success(fontPreviews = mockFonts)
        Timber.d("Previewing FavoritesScreenContent with mock fonts: $mockFonts")
        FavoritesScreenContent(
            uiState = mockUiState,
            onToggleLike = { Timber.d("Mock onToggleLike called for font: ${it.title}") },
            onRetry = { Timber.d("Mock onRetry triggered.") }
        )
    }
}
