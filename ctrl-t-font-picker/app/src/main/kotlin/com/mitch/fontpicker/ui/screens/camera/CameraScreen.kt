package com.mitch.fontpicker.ui.screens.camera

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitch.fontpicker.data.images.BitmapToolkit
import com.mitch.fontpicker.di.DefaultDependenciesProvider
import com.mitch.fontpicker.di.DependenciesProvider
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.components.overlays.ErrorOverlay
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding
import com.mitch.fontpicker.ui.screens.camera.components.CameraActionRow
import com.mitch.fontpicker.ui.screens.camera.controlers.CameraController
import com.mitch.fontpicker.ui.screens.camera.components.CameraLiveView
import com.mitch.fontpicker.ui.screens.camera.components.CameraLiveViewPlaceholder
import com.mitch.fontpicker.data.room.repository.FontPickerDatabaseRepository
import com.mitch.fontpicker.ui.designsystem.components.dialogs.FontCardSelectionDialog
import com.mitch.fontpicker.ui.designsystem.components.cards.FontCardData
import com.mitch.fontpicker.ui.screens.camera.controlers.FontRecognitionApiController
import com.mitch.fontpicker.ui.screens.camera.controlers.StorageController
import com.mitch.fontpicker.ui.util.viewModelProviderFactory
import kotlinx.coroutines.launch
import timber.log.Timber

private val TOP_PADDING = 84.dp

@Composable
fun CameraRoute(
    dependenciesProvider: DependenciesProvider,
    isPreview: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create or remember all the controllers
    val cameraController = remember {
        CameraController()
    }
    val storageController = remember {
        StorageController(dependenciesProvider)
    }
    val fontRecognitionApiController = remember {
        FontRecognitionApiController(dependenciesProvider, context)
    }
    val fontDatabaseRepository = remember {
        FontPickerDatabaseRepository(dependenciesProvider)
    }
    val bitmapToolkit = remember {
        BitmapToolkit(dependenciesProvider)
    }

    val cameraViewModel: CameraViewModel = viewModel(
        factory = viewModelProviderFactory {
            CameraViewModel(
                cameraController,
                storageController,
                fontRecognitionApiController,
                fontDatabaseRepository,
                bitmapToolkit)
        }
    )

    Timber.d("Rendering CameraScreenRoute. isPreview = $isPreview")

    // Only load the camera if we're not in preview
    if (!isPreview) {
        cameraViewModel.loadCameraProvider(context, lifecycleOwner)
    }

    CameraScreen(
        viewModel = cameraViewModel,
        isPreview = isPreview
    )
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
    val cameraPreviewView by viewModel.cameraPreviewView.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Timber.d("CameraScreen: Preview state = $cameraPreviewView, UI State = $uiState")

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
        cameraPreviewView = cameraPreviewView,
        onCapturePhoto = { coroutineScope.launch { viewModel.capturePhoto(context) } },
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
    cameraPreviewView: androidx.camera.core.Preview?,
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
                    Timber.d("CameraScreenContent: Displaying CameraLiveViewPlaceholder.")
                    CameraLiveViewPlaceholder()
                } else {
                    Timber.d("CameraScreenContent: Displaying CameraLiveView.")
                    CameraLiveView(
                        cameraPreviewView = cameraPreviewView,
                        modifier = Modifier.fillMaxSize(),
                        isLoading = isLoading,
                        photoUri = photoUri
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

        if (uiState is CameraUiState.OpeningFontsDialog) {
            val downloadedFonts = uiState.downloadedFonts
            FontCardSelectionDialog(
                cards = downloadedFonts.map { font ->
                    FontCardData(
                        name = font.title,
                        images = font.bitmaps,
                        liked = false,
                        onLikeClick = { /* Handle Like Click */ },
                        onWebpageClick = { /* Handle Webpage Click */ }
                    )
                },
                onDismiss = {
                    Timber.d("CameraScreenContent: Fonts dialog dismissed.")
                    viewModel.onFontsDialogDismissed()
                },
                onConfirm = {
                    Timber.d("CameraScreenContent: Fonts dialog confirmed.")
                        viewModel.onFontsDialogConfirmed(downloadedFonts)
                }
            )
        }
    }
}


@PreviewLightDark
@Composable
fun CameraScreenPreview() {
    FontPickerTheme {
        val context = LocalContext.current
        val dependenciesProvider = DefaultDependenciesProvider(context)

        // Create or mock your controllers
        val cameraController = remember { CameraController() }
        val storageController = remember { StorageController(dependenciesProvider) }
        val fontRecognitionApiController = remember { FontRecognitionApiController(dependenciesProvider, context) }
        val fontDatabaseRepository = remember { FontPickerDatabaseRepository(dependenciesProvider) }
        val bitmapToolkit = remember { BitmapToolkit(dependenciesProvider) }

        // Create a “preview” VM
        val previewViewModel = remember {
            CameraViewModel(
                cameraController = cameraController,
                storageController = storageController,
                fontRecognitionApiController = fontRecognitionApiController,
                fontDatabaseRepository = fontDatabaseRepository,
                bitmapToolkit = bitmapToolkit
            )
        }

        // Render the same screen but pass isPreview = true
        CameraScreen(viewModel = previewViewModel, isPreview = true)
    }
}
