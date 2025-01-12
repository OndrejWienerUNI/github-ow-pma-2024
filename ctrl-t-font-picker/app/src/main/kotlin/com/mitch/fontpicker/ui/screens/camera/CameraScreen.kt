package com.mitch.fontpicker.ui.screens.camera

import android.content.Context
import android.net.Uri
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.components.overlays.ErrorOverlay
import com.mitch.fontpicker.ui.screens.camera.components.CameraActionRow
import com.mitch.fontpicker.ui.screens.camera.components.CameraLiveView
import com.mitch.fontpicker.ui.screens.camera.components.CameraLiveViewPlaceholder
import com.mitch.fontpicker.ui.screens.home.PAGE_PADDING_HORIZONTAL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

private val TOP_PADDING = 84.dp

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    isPreview: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Ensure the camera provider is loaded
    LaunchedEffect(Unit) {
        Timber.d("Loading camera provider")
        viewModel.loadCameraProvider(context)
    }

    // Photo capture launcher
    val photoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let {
                Timber.d("Photo capture success. Processing captured photo.")
                viewModel.processCapturedPhoto(context, it)
            }
        } else {
            Timber.w("Photo capture failed.")
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Timber.i("Gallery image selected: $it")
            viewModel.handleGalleryImageSelection(it, context)
        } ?: Timber.w("No gallery image selected.")
    }

    val galleryPickerEvent by viewModel.galleryPickerEvent.collectAsState()
    if (galleryPickerEvent) {
        Timber.d("Launching gallery picker.")
        galleryLauncher.launch("image/*")
        viewModel.resetGalleryPickerEvent()
    }

    CameraScreenContent(
        viewModel = viewModel,
        isPreview = isPreview,
        photoUri = photoUri,
        onCapturePhoto = {
            Timber.d("Attempting to capture photo.")
            viewModel.createPhotoUri(context)?.let {
                photoUri = it
                photoCaptureLauncher.launch(it)
            } ?: Timber.e("Failed to create photo URI for capture.")
        },
        onFlipCamera = {
            Timber.d("Flipping camera.")
            viewModel.flipCamera()
        }
    )
}

private fun CameraViewModel.processCapturedPhoto(context: Context, uri: Uri) {
    viewModelScope.launch {
        try {
            onCapturePhoto(context) { capturedUri ->
                capturedUri?.let { Timber.i("Captured photo processed: $it") }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error processing captured photo.")
        }
    }
}

@Composable
private fun CameraScreenContent(
    viewModel: CameraViewModel,
    isPreview: Boolean,
    photoUri: Uri?,
    onCapturePhoto: () -> Unit,
    onFlipCamera: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FontPickerDesignSystem.colorScheme.background)
            .padding(top = TOP_PADDING)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PAGE_PADDING_HORIZONTAL)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3 / 4f)
                    .clip(FontPickerDesignSystem.shapes.large)
            ) {
                if (isPreview) {
                    CameraLiveViewPlaceholder(modifier = Modifier.fillMaxSize())
                } else {
                    CameraLiveView(
                        modifier = Modifier.fillMaxSize(),
                        lensFacing = viewModel.lensFacing,
                        isLoading = false,
                        photoUri = photoUri,
                        onCameraReady = { ready ->
                            viewModel.viewModelScope.launch(Dispatchers.IO) {
                                if (!ready) viewModel.onError("Camera failed to initialize")
                            }
                        },
                        onError = { error ->
                            viewModel.viewModelScope.launch(Dispatchers.IO) {
                                viewModel.onError(error)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            CameraActionRow(
                onShoot = { onCapturePhoto() },
                onGallery = { viewModel.onOpenGallery() },
                onFlip = { onFlipCamera() }
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Handle errors
        if (!isPreview) {
            if (uiState is CameraUiState.Error) {
                val errorMessage: String? = (uiState as CameraUiState.Error).error
                if (errorMessage is String) {
                    ErrorOverlay(
                        errorMessage = errorMessage,
                        closable = true,
                        verticalBias = 0.1f,
                        onClose = {
                            Timber.i("ErrorOverlay closed. Resetting error state.")
                            viewModel.resetErrorState()
                        }
                    )
                }
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
            val mockViewModel = CameraViewModel()
            CameraScreen(
                viewModel = mockViewModel,
                isPreview = true
            )
        }
    }
}
