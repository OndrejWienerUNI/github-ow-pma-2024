package com.mitch.fontpicker.ui.screens.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.FileProvider
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

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK // Default to back camera
    private var isProcessingImage = false

    // Validates if the camera provider is available
    fun loadCameraProvider(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ProcessCameraProvider.getInstance(context).get() // Simply validate the provider
                withContext(Dispatchers.Main) {
                    Timber.d("Camera provider loaded successfully.")
                    _uiState.value = CameraUiState.CameraReady(lensFacing)
                }
            } catch (e: Exception) {
                onError("Failed to load camera provider: ${e.message}")
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
        _uiState.value = CameraUiState.CameraReady(lensFacing)
    }

    fun createPhotoUri(context: Context): Uri? {
        return try {
            val picturesDir = dependenciesProvider.picturesDir
            if (!picturesDir.exists()) {
                throw IllegalStateException("Pictures directory does not exist. Ensure it's created on app start.")
            }
            val file = File(picturesDir, "fp_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            Timber.e(e, "Error creating photo URI.")
            null
        }
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

                if (copiedUri != null) {
                    _photoUri.value = copiedUri
                    _isImageInUse.value = true
                    Timber.d("Gallery image copied to pictures directory: $copiedUri")
                    Timber.d("Gallery image selected: $uri, ready for display.")

                    _uiState.value = CameraUiState.Loading
                    sendImageForProcessing(copiedUri)
                } else {
                    Timber.e("Copied image's URI was not acquired. Cannot continue processing.")
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
                FileProvider.getUriForFile(context, "${context.packageName}.provider", destinationFile)
            } catch (e: Exception) {
                onError("Error copying image to the app's temporary directory. Images can't be processed without proper permissions.")
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
                Timber.e("The directory does not exist or is not a directory: ${picturesDir.absolutePath}")
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
                _uiState.value = CameraUiState.Loading
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
