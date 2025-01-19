package com.mitch.fontpicker.ui.screens.camera

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mitch.fontpicker.data.api.FontDownloaded
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.components.dialogs.ErrorDialog
import com.mitch.fontpicker.ui.designsystem.components.dialogs.FontCardSelectionDialog
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding
import com.mitch.fontpicker.ui.screens.camera.components.CameraActionRow
import com.mitch.fontpicker.ui.screens.camera.components.CameraLiveView
import com.mitch.fontpicker.ui.screens.camera.components.CameraLiveViewPlaceholder
import kotlinx.coroutines.launch
import timber.log.Timber

private val TOP_PADDING = 84.dp

@Composable
fun CameraRoute(
    viewModel: CameraViewModel,
) {
    Timber.d("Rendering CameraScreenRoute.")

    CameraScreen(
        viewModel = viewModel
    )
}

@Composable
fun CameraScreen(
    viewModel: CameraViewModel
) {
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val cameraPreviewView by viewModel.cameraPreviewView.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Timber.d("CameraScreen: Preview state = $cameraPreviewView, UI State = $uiState")

    BackHandler(enabled = true) {
        if (uiState is CameraUiState.OpeningFontsDialog) {
            viewModel.onFontsDialogDismissed()
        } else {
            val resetScreen = when (uiState) {
                is CameraUiState.CameraReady -> false
                is CameraUiState.Success -> false
                else -> true
            }
            if (resetScreen) {
                viewModel.cancelProcessing()
                Timber.d("Back button pressed. Resetting image state.")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetImageState() }
    }

    DisposableEffect(context) {
        val lifecycle = (context as? androidx.activity.ComponentActivity)?.lifecycle
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
                viewModel.resetImageState()
            }
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onGalleryImageSelected(context, it) }
    }

    val galleryPickerEvent by viewModel.galleryPickerEvent.collectAsState()
    if (galleryPickerEvent) {
        galleryLauncher.launch("image/*")
        viewModel.resetGalleryPickerEvent()
    }

    // Call the UI content
    CameraScreenContent(
        uiState = uiState,
        isPreview = isPreview,
        imageUri = imageUri,
        cameraPreviewView = cameraPreviewView,
        onCapturePhoto = { coroutineScope.launch { viewModel.capturePhoto(context) } },
        onFlipCamera = { viewModel.flipCamera(context, lifecycleOwner) },
        onOpenGallery = { viewModel.onOpenGallery() },
        onDismissError = { viewModel.resetErrorState() },
        onDismissFontsDialog = { viewModel.onFontsDialogDismissed() },
        onConfirmFontsDialog = { fonts ->
            val likedFonts = fonts.filter { it.isLiked.value }
            viewModel.onFontsDialogConfirmed(likedFonts)
        }
    )
}

@Composable
private fun CameraScreenContent(
    uiState: CameraUiState,
    isPreview: Boolean,
    imageUri: Uri?,
    cameraPreviewView: androidx.camera.core.Preview?,
    onCapturePhoto: () -> Unit,
    onFlipCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onDismissError: () -> Unit,
    onDismissFontsDialog: () -> Unit,
    onConfirmFontsDialog: (List<FontDownloaded>) -> Unit
) {
    LaunchedEffect(uiState) {
        Timber.d("CameraScreenContent: Current UI State: $uiState")
        if (uiState is CameraUiState.Error) {
            Timber.d("CameraScreenContent: Displaying error overlay: ${uiState.errorMessage}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FontPickerDesignSystem.colorScheme.background)
            .padding(top = TOP_PADDING)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = padding.medium)
        ) {
            val isLoading = when (uiState) {
                is CameraUiState.Processing -> true
                is CameraUiState.CameraReady -> false
                is CameraUiState.Success -> false
                is CameraUiState.Error -> false
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(FontPickerDesignSystem.shapes.large)
            ) {
                if (isPreview) {
                    CameraLiveViewPlaceholder()
                } else {
                    CameraLiveView(
                        cameraPreviewView = cameraPreviewView,
                        modifier = Modifier.fillMaxSize(),
                        isLoading = isLoading,
                        imageUri = imageUri
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            CameraActionRow(
                onShoot = onCapturePhoto,
                onGallery = onOpenGallery,
                onFlip = onFlipCamera,
                isLoading = isLoading
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        if (uiState is CameraUiState.Error) {
            ErrorDialog(
                errorMessage = uiState.errorMessage,
                onDismiss = onDismissError
            )
        }

        if (uiState is CameraUiState.OpeningFontsDialog) {
            FontCardSelectionDialog(
                fonts = uiState.downloadedFonts,
                onDismiss = onDismissFontsDialog,
                onConfirm = {
                    onConfirmFontsDialog(uiState.downloadedFonts)
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
fun CameraScreenContentPreview() {
    FontPickerTheme {
        // Mock dependencies for preview
        val mockUiState = CameraUiState.CameraReady()
        val mockImageUri: Uri? = null
        val mockCameraPreviewView: androidx.camera.core.Preview? = null

        // Render the UI
        CameraScreenContent(
            uiState = mockUiState,
            isPreview = true,
            imageUri = mockImageUri,
            cameraPreviewView = mockCameraPreviewView,
            onCapturePhoto = {},
            onFlipCamera = {},
            onOpenGallery = {},
            onDismissError = {},
            onDismissFontsDialog = {},
            onConfirmFontsDialog = {}
        )
    }
}
