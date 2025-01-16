package com.mitch.fontpicker.ui.screens.recycle

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitch.fontpicker.data.api.FontDownloaded
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.components.dialogs.WipeRecycleBinDialog
import com.mitch.fontpicker.ui.screens.favorites.components.FontCardListScreenContent
import com.mitch.fontpicker.ui.screens.favorites.components.FontCardListUiState
import com.mitch.fontpicker.ui.screens.recycle.components.WipeRecycleBinButton
import timber.log.Timber

@Composable
fun RecycleBinRoute(
    viewModel: RecycleBinViewModel
) {
    Timber.d("Rendering RecycleBinRoute.")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Timber.d("Current uiState: $uiState")

    RecycleBinScreen(
        uiState = uiState,
        onRestore = { font ->
            Timber.d("onRestore called for font: ${font.title} with id: ${font.id}")
            viewModel.restoreFont(font)
        },
        onRenderStart = {
            viewModel.startObservingRecycleBin(lastToFirst = true)
        },
        onRetry = {
            viewModel.startObservingRecycleBin(lastToFirst = true)
        },
        recycleBinEmpty = viewModel.isRecycleBinEmpty.collectAsState().value,
        onWipeRecycleBin = {
            viewModel.wipeRecycleBin()
        }
    )
}

@Composable
fun RecycleBinScreen(
    uiState: FontCardListUiState,
    onRestore: (FontDownloaded) -> Unit,
    onRenderStart: () -> Unit,
    onRetry: () -> Unit,
    recycleBinEmpty: Boolean,
    onWipeRecycleBin: () -> Unit
) {
    Timber.d("RecycleBinScreen rendering with uiState: $uiState")

    // Persist state across configuration changes or process recreation
    val hasRendered = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasRendered.value) {
            Timber.d("Starting to observe recycle bin on initial screen load.")
            onRenderStart()
            hasRendered.value = true
        }
    }

    RecycleBinScreenContent(
        uiState = uiState,
        onRestore = onRestore,
        onRetry = onRetry,
        recycleBinEmpty = recycleBinEmpty,
        onWipeRecycleBin = onWipeRecycleBin
    )
}

@Composable
fun RecycleBinScreenContent(
    uiState: FontCardListUiState,
    onRestore: (FontDownloaded) -> Unit,
    onRetry: () -> Unit,
    recycleBinEmpty: Boolean,
    onWipeRecycleBin: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FontPickerDesignSystem.colorScheme.background)
    ) {
        FontCardListScreenContent(
            uiState = uiState,
            onToggleLike = onRestore,
            onRetry = onRetry,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        WipeRecycleBinButton(
            onClick = { showDialog = true },
            recycleBinEmpty = recycleBinEmpty, // Forwarding the state
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        )
    }

    if (showDialog) {
        WipeRecycleBinDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                onWipeRecycleBin()
            }
        )
    }
}



@PreviewLightDark
@Composable
fun RecycleBinScreenContentPreview() {
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
        Timber.d("Previewing RecycleBinScreenContent with mock fonts: $mockFonts")
        RecycleBinScreenContent(
            uiState = mockUiState,
            onRestore = { Timber.d("Mock onRestore called for font: ${it.title}") },
            onRetry = { Timber.d("Mock onRetry triggered.") },
            recycleBinEmpty = false,
            onWipeRecycleBin = { Timber.d("Mock wipe recycle bin triggered.") }
        )
    }
}
