package com.mitch.fontpicker.ui.screens.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Success(message = "Camera ready"))
    val uiState: StateFlow<CameraUiState> = _uiState

    private val _galleryPickerEvent = MutableStateFlow(false)
    val galleryPickerEvent: StateFlow<Boolean> = _galleryPickerEvent

    var lensFacing: Int = CameraSelector.LENS_FACING_BACK // Default to back camera
    private var tempPhotoFile: File? = null
    private val cameraProviderFuture = MutableStateFlow<ProcessCameraProvider?>(null)

    fun loadCameraProvider(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Timber.d("Loading camera provider...")
                val provider = ProcessCameraProvider.getInstance(context).get()
                withContext(Dispatchers.Main) {
                    cameraProviderFuture.value = provider
                    Timber.d("Camera provider loaded successfully.")
                    _uiState.value = CameraUiState.Success(message = "Camera initialized")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load camera provider.")
                withContext(Dispatchers.Main) {
                    onError("Failed to load camera provider: ${e.message}")
                }
            }
        }
    }

    fun flipCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        Timber.d("Camera flipped to ${if (lensFacing == CameraSelector.LENS_FACING_BACK) "BACK" else "FRONT"}.")
    }

    fun onCapturePhoto(context: Context, onPhotoCaptured: (Uri?) -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value = CameraUiState.Loading
                Timber.d("Starting photo capture...")

                val photoUri = withContext(Dispatchers.IO) { createPhotoUri(context) }
                if (photoUri == null) {
                    Timber.e("Photo URI creation failed.")
                    onError("Failed to create photo file.")
                    onPhotoCaptured(null)
                    return@launch
                }

                delay(2000) // Simulate processing delay

                withContext(Dispatchers.Main) {
                    _uiState.value = CameraUiState.Success(photoUri = photoUri)
                    onPhotoCaptured(photoUri)
                    _uiState.value = CameraUiState.Success(message = "Camera ready")
                    Timber.d("Photo capture completed: $photoUri")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during photo capture.")
                onError("Error capturing photo: ${e.message}")
                onPhotoCaptured(null)
            }
        }
    }

    private fun cleanupTempPhoto() {
        viewModelScope.launch(Dispatchers.IO) {
            tempPhotoFile?.let { file ->
                if (file.exists()) {
                    file.delete()
                    Timber.d("Temporary photo file deleted.")
                }
            }
            tempPhotoFile = null
        }
    }

    fun createPhotoUri(context: Context): Uri? {
        return try {
            Timber.d("Creating photo URI...")
            val appPicturesDir = File(context.getExternalFilesDir(null), "pictures")
            if (!appPicturesDir.exists()) {
                appPicturesDir.mkdirs()
                Timber.d("Created pictures directory: ${appPicturesDir.path}")
            }
            val photoFile = File(appPicturesDir, "fp_${System.currentTimeMillis()}.jpg")
            tempPhotoFile = photoFile
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                photoFile
            )
        } catch (e: Exception) {
            Timber.e(e, "Error creating photo URI.")
            onError("Error creating photo URI: ${e.message}")
            null
        }
    }

    fun handleGalleryImageSelection(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                Timber.d("Handling gallery image selection: $uri")
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        // Process the image if needed
                        Timber.d("Processing selected gallery image.")
                    }
                }

                withContext(Dispatchers.Main) {
                    _uiState.value = CameraUiState.Success(galleryUri = uri)
                    Timber.d("Gallery image processed and UI state updated.")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error selecting gallery image.")
                onError("Error selecting gallery image: ${e.message}")
            }
        }
    }

    fun onOpenGallery() {
        _galleryPickerEvent.value = true
    }

    fun resetGalleryPickerEvent() {
        _galleryPickerEvent.value = false
    }

    fun onError(message: String) {
        _uiState.value = CameraUiState.Error(error = message)
        Timber.e(message)
    }

    fun resetErrorState() {
        _uiState.value = CameraUiState.Success(message = "Error cleared")
        Timber.d("Error state reset to success.")
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            cleanupTempPhoto()
            Timber.d("Cleanup performed in onCleared.")
        }
    }
}
