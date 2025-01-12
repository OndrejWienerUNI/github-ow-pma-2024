package com.mitch.fontpicker.ui.screens.camera

import android.net.Uri
import android.os.StrictMode
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
import com.mitch.fontpicker.ui.screens.camera.components.CameraActionRow
import com.mitch.fontpicker.ui.screens.camera.components.CameraLiveView
import com.mitch.fontpicker.ui.screens.camera.components.CameraLiveViewPlaceholder
import com.mitch.fontpicker.ui.screens.camera.components.ErrorDisplayBox
import com.mitch.fontpicker.ui.screens.home.PAGE_PADDING_HORIZONTAL
import com.mitch.fontpicker.util.StrictModeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val TOP_PADDING = 84.dp

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    isPreview: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }


//    // TODO: delete this after violations are solved
//    StrictMode.setThreadPolicy(
//        StrictMode.ThreadPolicy.Builder()
//            .detectAll() // Detect all thread violations
//            .penaltyLog() // Log violations to Logcat
//            .penaltyDeath() // Crash on violations (remove this after debugging)
//            .build()
//    )


    // Ensure the camera provider is loaded
    LaunchedEffect(Unit) {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            viewModel.loadCameraProvider(context)
        }
    }

    // Photo capture launcher
    val photoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let {
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    viewModel.onCapturePhoto(context) { capturedUri ->
                        photoUri = capturedUri
                    }
                }
            }
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            StrictModeUtils.relax(diskReads = true)
            try {
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    viewModel.handleGalleryImageSelection(it, context)
                }
            } finally {
                StrictModeUtils.restore()
            }
        }
    }

    val galleryPickerEvent by viewModel.galleryPickerEvent.collectAsState()
    if (galleryPickerEvent) {
        galleryLauncher.launch("image/*")
        viewModel.resetGalleryPickerEvent()
    }

    CameraScreenContent(
        viewModel = viewModel,
        isPreview = isPreview,
        photoUri = photoUri,
        onCapturePhoto = {
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                photoUri = viewModel.createPhotoUri(context)
                photoUri?.let { photoCaptureLauncher.launch(it) }
            }
        },
        onFlipCamera = {
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                if (!isPreview) viewModel.flipCamera()
            }
        }
    )
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
                            if (!ready) viewModel.onError("Camera failed to initialize")
                        },
                        onError = { error -> viewModel.onError(error) }
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
                    ErrorDisplayBox(errorMessage)
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
