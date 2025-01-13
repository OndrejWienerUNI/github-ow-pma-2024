package com.mitch.fontpicker.ui.screens.camera

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Preview
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitch.fontpicker.di.DefaultDependenciesProvider
import com.mitch.fontpicker.di.DependenciesProvider
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.components.overlays.ErrorOverlay
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding
import com.mitch.fontpicker.ui.screens.camera.components.CameraActionRow
import com.mitch.fontpicker.ui.screens.camera.components.CameraLiveView
import com.mitch.fontpicker.ui.screens.camera.components.CameraLiveViewPlaceholder
import com.mitch.fontpicker.ui.util.viewModelProviderFactory
import timber.log.Timber

private val TOP_PADDING = 84.dp

@Composable
fun CameraScreenRoute(
    dependenciesProvider: DependenciesProvider,
    isPreview: Boolean
) {
    // Create the ViewModel with a short inline factory
    val cameraViewModel: CameraViewModel = viewModel(
        factory = viewModelProviderFactory {
            CameraViewModel(dependenciesProvider)
        }
    )
    Timber.d("Rendering CameraScreenRoute")
    cameraViewModel.loadCameraProvider(LocalContext.current, LocalLifecycleOwner.current)
    CameraScreen(viewModel = cameraViewModel, isPreview = isPreview)
}

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    isPreview: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()
    val preview by viewModel.preview.collectAsState()

    Timber.d("CameraScreen: Preview state = $preview, UI State = $uiState")

    // Dispose of the image state when the CameraScreen is removed from the composition tree
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetImageState()
        }
    }

    // Add a lifecycle observer for cleanup when the app is terminated
    DisposableEffect(context) {
        val lifecycle = (context as? androidx.activity.ComponentActivity)?.lifecycle
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
                viewModel.resetImageState()
            }
        }

        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onGalleryImageSelected(context, it) }
    }

    // Trigger gallery picker if requested
    val galleryPickerEvent by viewModel.galleryPickerEvent.collectAsState()
    if (galleryPickerEvent) {
        galleryLauncher.launch("image/*")
        viewModel.resetGalleryPickerEvent()
    }

    CameraScreenContent(
        viewModel = viewModel,
        uiState = uiState,
        isPreview = isPreview,
        photoUri = photoUri,
        preview = preview,
        onCapturePhoto = { viewModel.capturePhoto(context) },
        onFlipCamera = { viewModel.flipCamera(context, lifecycleOwner) },
        onOpenGallery = { viewModel.onOpenGallery() }
    )
}

@Composable
private fun CameraScreenContent(
    viewModel: CameraViewModel,
    uiState: CameraUiState,
    isPreview: Boolean,
    photoUri: Uri?,
    preview: Preview?,
    onCapturePhoto: () -> Unit,
    onFlipCamera: () -> Unit,
    onOpenGallery: () -> Unit
) {
    // Log UI state changes
    LaunchedEffect(uiState) {
        Timber.d("CameraScreenContent: Current UI State: $uiState")
        if (uiState is CameraUiState.Error) {
            Timber.d("CameraScreenContent: Displaying error overlay: ${uiState.error}")
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(FontPickerDesignSystem.shapes.large)
            ) {
                if (isPreview) {
                    Timber.d("CameraScreenContent: Displaying CameraLiveViewPlaceholder.")
                    CameraLiveViewPlaceholder()
                } else {
                    Timber.d("CameraScreenContent: Displaying CameraLiveView.")
                    CameraLiveView(
                        preview = preview,
                        modifier = Modifier.fillMaxSize(),
                        isLoading = uiState is CameraUiState.Processing,
                        photoUri = photoUri
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            CameraActionRow(
                onShoot = onCapturePhoto,
                onGallery = onOpenGallery,
                onFlip = onFlipCamera,
                isLoading = uiState is CameraUiState.Processing
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Handle errors: this will be displayed anytime the UI state is an error
        if (uiState is CameraUiState.Error) {
            Timber.d("CameraScreenContent: Showing ErrorOverlay.")
            ErrorOverlay(
                errorMessage = uiState.error,
                closable = true,
                verticalBias = 0.1f,
                onClose = {
                    Timber.d("CameraScreenContent: ErrorOverlay closed.")
                    viewModel.resetErrorState()
                }
            )
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
            // Using mock ViewModel for the preview
            val mockViewModel = CameraViewModel(
                DefaultDependenciesProvider(LocalContext.current)
            )
            CameraScreen(
                viewModel = mockViewModel,
                isPreview = true
            )
        }
    }
}