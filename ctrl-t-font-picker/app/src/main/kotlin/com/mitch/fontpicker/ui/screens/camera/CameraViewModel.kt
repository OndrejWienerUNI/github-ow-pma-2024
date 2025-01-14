package com.mitch.fontpicker.ui.screens.camera

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.fontpicker.ui.screens.camera.controlers.CameraController
import com.mitch.fontpicker.data.room.repository.FontPickerDatabaseRepository
import com.mitch.fontpicker.ui.screens.camera.controlers.FontRecognitionApiController
import com.mitch.fontpicker.ui.screens.camera.controlers.StorageController
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
    private val cameraController: CameraController,
    private val storageController: StorageController,
    private val fontRecognitionApiController: FontRecognitionApiController,
    private val fontDatabaseRepository: FontPickerDatabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.CameraReady())
    val uiState: StateFlow<CameraUiState> = _uiState

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri

    private val _galleryPickerEvent = MutableStateFlow(false)
    val galleryPickerEvent: StateFlow<Boolean> = _galleryPickerEvent

    private val _isImageInUse = MutableStateFlow(false)
    val isImageInUse: StateFlow<Boolean> = _isImageInUse

    private var isProcessingImage = false

    private val _cameraPreview = MutableStateFlow<androidx.camera.core.Preview?>(null)
    val cameraPreviewView: StateFlow<androidx.camera.core.Preview?> = _cameraPreview

    /**
     * Load/Initialize the camera for the first time.
     */
    fun loadCameraProvider(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            val result = cameraController.initializeCamera(context, lifecycleOwner)
            if (result.isSuccess) {
                // <-- Set the new preview reference so the UI sees it
                _cameraPreview.value = cameraController.preview
                _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
            } else {
                onError("Failed to load camera provider: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    /**
     * Flip the camera lens (front/back).
     */
    fun flipCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            val flipResult = cameraController.flipCamera(context, lifecycleOwner)
            if (flipResult.isSuccess) {
                // Update the preview Flow again
                _cameraPreview.value = cameraController.preview

                _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
            } else {
                onError("Failed to flip camera: ${flipResult.exceptionOrNull()?.message}")
            }
        }
    }

    /**
     * Capture a photo using CameraX, then proceed with UI updates.
     */
    fun capturePhoto(context: Context) {
        viewModelScope.launch {
            val fileResult = runCatching { storageController.createPhotoFile() }
            if (fileResult.isFailure) {
                onError("Failed to create photo file: ${fileResult.exceptionOrNull()?.message}")
                return@launch
            }

            val photoFile: File = fileResult.getOrThrow()
            _uiState.value = CameraUiState.Processing // or whatever state you prefer

            val captureResult = cameraController.capturePhoto(context, photoFile)
            if (captureResult.isFailure) {
                onError("Photo capture failed: ${captureResult.exceptionOrNull()?.message}")
                return@launch
            }

            val capturedFile = captureResult.getOrThrow()
            val capturedUri = Uri.fromFile(capturedFile)  // or convert to a content URI if needed
            _photoUri.value = capturedUri
            Timber.d("Starting photo capture. File=$photoFile, contentUri=$capturedUri")
            _uiState.value = CameraUiState.ImageReady(capturedUri)

            onPhotoCaptured()
        }
    }

    /**
     * Called when a photo was successfully captured.
     */
    private fun onPhotoCaptured() {
        val uri = _photoUri.value
        if (uri != null) {
            sendImageForProcessing(uri)
        } else {
            onError("No photo URI available after capture.")
        }
    }

    /**
     * Reset the gallery picker event flag.
     */
    fun resetGalleryPickerEvent() {
        _galleryPickerEvent.value = false
    }

    /**
     * Signal that the user wants to pick from the gallery.
     */
    fun onOpenGallery() {
        _galleryPickerEvent.value = true
    }

    /**
     * Handle a selected gallery image by copying it to pictures directory,
     * then updating the UI state.
     */
    fun onGalleryImageSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val copiedUri = storageController.copyImageToPicturesDir(context, uri)
                _uiState.value = CameraUiState.ImageReady(uri)

                if (copiedUri != null) {
                    _photoUri.value = copiedUri
                    _isImageInUse.value = true
                    Timber.d("Gallery image copied to pictures directory: $copiedUri")
                    Timber.d("Gallery image selected: $uri, ready for display.")

                    _uiState.value = CameraUiState.Processing
                    sendImageForProcessing(copiedUri)
                } else {
                    onError("Failed to prepare the selected image.")
                }
            } catch (e: Exception) {
                onError("Failed to handle the selected image: ${e.message}")
                Timber.e(e, "Error handling selected gallery image.")
            }
        }
    }

    /**
     * Process the captured (or gallery-selected) image.
     */
    private fun sendImageForProcessing(uri: Uri) {
        if (isProcessingImage) {
            Timber.w("Image processing already in progress.")
            return
        }

        isProcessingImage = true
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = CameraUiState.Processing
            Timber.d("Processing image: $uri")
            val result = fontRecognitionApiController.processImage(uri)
            withContext(Dispatchers.Main) {
                if (result.isFailure) {
                    onError("Error processing image: ${result.exceptionOrNull()?.message}")
                } else {
                    _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
                    resetImageAfterLoading()
                }
            }
            isProcessingImage = false
        }
    }

    /**
     * Called after we've finished processing the image, to clear state.
     */
    private fun resetImageAfterLoading() {
        if (_uiState.value is CameraUiState.CameraReady) {
            _photoUri.value = null
            _isImageInUse.value = false
            Timber.d("Image reset after loading phase.")
            markImageAsNotInUse()
        }
    }

    /**
     * Resets the viewmodel's photo state and triggers a dir clear (optional).
     */
    fun resetImageState() {
        _photoUri.value = null
        _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
        markImageAsNotInUse()
    }

    /**
     * Actually remove the images from the pictures directory, if needed.
     */
    private fun markImageAsNotInUse() {
        _isImageInUse.value = false
        viewModelScope.launch {
            delay(1000) // Optional delay to ensure UI updates complete
            storageController.clearPicturesDir()
        }
    }

    /**
     * Generic error handler that updates UI state.
     */
    private fun onError(message: String) {
        _uiState.value = CameraUiState.Error(error = message)
        Timber.e(message)
    }

    /**
     * Resets error state to CameraReady if needed.
     */
    fun resetErrorState() {
        _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
        Timber.d("Error state reset to success.")
    }

    // Example DB usage: Adding/Removing Favorite Fonts
    fun addFavoriteFont(fontName: String) {
        viewModelScope.launch {
            fontDatabaseRepository.addFavoriteFont(fontName)
        }
    }

    fun removeFavoriteFont(fontName: String) {
        viewModelScope.launch {
            fontDatabaseRepository.removeFavoriteFont(fontName)
        }
    }
}
