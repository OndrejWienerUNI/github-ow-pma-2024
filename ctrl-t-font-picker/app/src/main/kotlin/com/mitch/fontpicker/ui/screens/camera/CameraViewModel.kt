package com.mitch.fontpicker.ui.screens.camera

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.fontpicker.di.DependenciesProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

@Suppress("UNUSED")
class CameraViewModel(
    private val dependenciesProvider: DependenciesProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.CameraReady())
    val uiState: StateFlow<CameraUiState> = _uiState

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri

    private val _galleryPickerEvent = MutableStateFlow(false)
    val galleryPickerEvent: StateFlow<Boolean> = _galleryPickerEvent

    private val _isImageInUse = MutableStateFlow(false)
    val isImageInUse: StateFlow<Boolean> = _isImageInUse

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var isProcessingImage = false

    private val _preview = MutableStateFlow<androidx.camera.core.Preview?>(null)
    val cameraPreviewView: StateFlow<androidx.camera.core.Preview?> = _preview

    private var imageCapture: ImageCapture? = null


    @OptIn(ExperimentalZeroShutterLag::class)
    fun loadCameraProvider(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            Timber.d("Starting camera provider initialization.")
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        Timber.d("CameraProvider obtained successfully.")

                        // Initialize Preview use case
                        val cameraPreviewViewUseCase = androidx.camera.core.Preview.Builder()
                            .build().also {
                            _preview.value = it
                            Timber.d("Preview use case initialized and set to _preview.")
                        }

                        // Initialize ImageCapture use case
                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(CAPTURE_MODE_ZERO_SHUTTER_LAG)
                            .build()
                            .also {
                            Timber.d("ImageCapture use case initialized.")
                        }

                        // Select the camera by lensFacing
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                        Timber.d("CameraSelector created with lensFacing: $lensFacing")

                        // Bind use cases to lifecycle
                        cameraProvider.unbindAll()
                        Timber.d("Unbound all use cases.")

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            cameraPreviewViewUseCase,
                            imageCapture
                        )
                        Timber.d("Use cases bound to lifecycle successfully.")

                        _uiState.value = CameraUiState.CameraReady(lensFacing)
                        Timber.d("UI State updated to CameraReady.")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to load and bind camera provider.")
                        onError("Failed to load and bind camera provider: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(context))
            } catch (e: Exception) {
                Timber.e(e, "Error initializing camera provider future.")
                onError("Error initializing camera provider: ${e.message}")
            }
        }
    }

    @OptIn(ExperimentalZeroShutterLag::class)
    fun flipCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        Timber.d("Camera flipped to " +
                "${if (lensFacing == CameraSelector.LENS_FACING_BACK) "BACK" else "FRONT"}.")

        viewModelScope.launch(Dispatchers.Main) {
            Timber.d("Starting camera flipping process.")
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        Timber.d("CameraProvider obtained for flipping.")

                        // Reinitialize Preview use case
                        val previewUseCase = androidx.camera.core.Preview.Builder().build().also {
                            _preview.value = it
                            Timber.d("Preview use case re-initialized for flipping.")
                        }

                        // Reinitialize ImageCapture use case
                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(CAPTURE_MODE_ZERO_SHUTTER_LAG)
                            .build().also {
                            Timber.d("ImageCapture use case re-initialized for flipping.")
                        }

                        // Select the new camera by lensFacing
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                        Timber.d("CameraSelector created " +
                                "for flipping with lensFacing: $lensFacing")

                        // Rebind use cases with the new lensFacing
                        cameraProvider.unbindAll()
                        Timber.d("Unbound all use cases for flipping.")
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            previewUseCase,
                            imageCapture
                        )
                        Timber.d("Use cases rebound " +
                                "to lifecycle successfully after flipping.")

                        _uiState.value = CameraUiState.CameraReady(lensFacing)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to flip camera.")
                        onError("Failed to flip camera: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(context))
            } catch (e: Exception) {
                Timber.e(e, "Error during camera flipping.")
                onError("Error during camera flipping: ${e.message}")
            }
        }
    }

    private fun File.toContentUri(context: Context): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            this
        )
    }

    private suspend fun createPhotoFile(): File = withContext(Dispatchers.IO) {
        // The directory you already manage in dependenciesProvider
        val picturesDir = dependenciesProvider.picturesDir
        if (!picturesDir.exists()) {
            // Or consider picturesDir.mkdirs() if it might not exist
            throw IllegalStateException("Pictures directory does not exist. Ensure it's created on app start.")
        }

        // Create a unique file in that directory
        val fileName = "fp_${System.currentTimeMillis()}.jpg"
        File(picturesDir, fileName)
    }

    suspend fun capturePhoto(context: Context) {
        val photoFile = createPhotoFile()
        val contentUri = photoFile.toContentUri(context)

        val imageCaptureUseCase = imageCapture
        if (imageCaptureUseCase == null) {
            Timber.e("ImageCapture use case is not initialized.")
            onError("ImageCapture use case is not initialized.")
            return
        }

        _photoUri.value = contentUri
        Timber.d("Starting photo capture. File=$photoFile, contentUri=$contentUri")
        _uiState.value = CameraUiState.ImageReady(contentUri)

        // 4) Pass the *actual file* to CameraX
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCaptureUseCase.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Timber.d("Photo captured successfully: $contentUri")
                    onPhotoCaptured()
                }

                override fun onError(exception: ImageCaptureException) {
                    Timber.e(exception, "Photo capture failed.")
                    onError("Photo capture failed: ${exception.message}")
                }
            }
        )
    }

    fun onPhotoCaptured() {
        val uri = _photoUri.value
        if (uri != null) {
            sendImageForProcessing(uri)
        } else {
            onError("No photo URI available after capture.")
        }
    }

    fun resetGalleryPickerEvent() {
        _galleryPickerEvent.value = false
    }

    fun onOpenGallery() {
        _galleryPickerEvent.value = true
    }

    fun onGalleryImageSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val copiedUri = copyImageToPicturesDir(context, uri)
                _uiState.value = CameraUiState.ImageReady(uri)

                if (copiedUri != null) {
                    _photoUri.value = copiedUri
                    _isImageInUse.value = true
                    Timber.d("Gallery image copied to pictures directory: $copiedUri")
                    Timber.d("Gallery image selected: $uri, ready for display.")

                    _uiState.value = CameraUiState.Processing
                    sendImageForProcessing(copiedUri)
                } else {
                    Timber.e("Copied image's URI was not acquired. " +
                            "Cannot continue processing.")
                    onError("Failed to prepare the selected image.")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling selected gallery image.")
                onError("Failed to handle the selected image: ${e.message}")
            }
        }
    }

    private suspend fun copyImageToPicturesDir(context: Context, sourceUri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            val picturesDir = dependenciesProvider.picturesDir
            val fileName = "gallery_image_${System.currentTimeMillis()}.jpg"
            val destinationFile = File(picturesDir, fileName)

            try {
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                val outputStream = destinationFile.outputStream()
                inputStream?.copyTo(outputStream)
                outputStream.close()
                inputStream?.close()

                Timber.d("Image copied to: ${destinationFile.absolutePath}")
                FileProvider.getUriForFile(context, "${context.packageName}.provider",
                    destinationFile)
            } catch (e: Exception) {
                onError("Error copying image to the app's temporary directory. " +
                        "Images can't be processed without proper permissions.")
                Timber.e("Detailed exception:\n$e")
                null
            }
        }
    }

    private suspend fun clearPicturesDir() {
        withContext(Dispatchers.IO) {
            val picturesDir = dependenciesProvider.picturesDir

            if (picturesDir.exists() && picturesDir.isDirectory) {
                val files = picturesDir.listFiles()
                Timber.d("Clearing directory: ${picturesDir.absolutePath}")
                files?.forEach { file ->
                    try {
                        if (file.exists() && file.delete()) {
                            Timber.d("Deleted file: ${file.absolutePath}")
                        } else {
                            Timber.e("Failed to delete file: ${file.absolutePath}")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error deleting file: ${file.absolutePath}")
                    }
                }
            } else {
                Timber.e("The directory does not exist or is not " +
                        "a directory: ${picturesDir.absolutePath}")
            }
        }
    }

    private fun sendImageForProcessing(uri: Uri) {
        if (isProcessingImage) {
            Timber.w("Image processing already in progress.")
            return
        }

        isProcessingImage = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = CameraUiState.Processing
                Timber.d("Processing image: $uri")
                delay(2000) // Simulate processing delay
                withContext(Dispatchers.Main) {
                    _uiState.value = CameraUiState.CameraReady(lensFacing)
                    resetImageAfterLoading()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Error processing image: ${e.message}")
                }
            } finally {
                isProcessingImage = false
            }
        }
    }

    private fun resetImageAfterLoading() {
        if (_uiState.value is CameraUiState.CameraReady) {
            _photoUri.value = null
            _isImageInUse.value = false
            Timber.d("Image reset after loading phase.")
            markImageAsNotInUse()
        }
    }

    fun resetImageState() {
        _photoUri.value = null
        _uiState.value = CameraUiState.CameraReady(lensFacing)
        markImageAsNotInUse()
    }

    private fun markImageAsNotInUse() {
        _isImageInUse.value = false
        viewModelScope.launch {
            delay(1000) // Optional delay to ensure UI updates complete
            clearPicturesDir()
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun onError(message: String) {
        _uiState.value = CameraUiState.Error(error = message)
        Timber.e(message)
    }

    fun resetErrorState() {
        _uiState.value = CameraUiState.CameraReady(lensFacing)
        Timber.d("Error state reset to success.")
    }
}

