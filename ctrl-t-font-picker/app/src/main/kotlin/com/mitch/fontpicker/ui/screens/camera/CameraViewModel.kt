package com.mitch.fontpicker.ui.screens.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.fontpicker.data.api.FontDownloaded
import com.mitch.fontpicker.data.api.FontResult
import com.mitch.fontpicker.data.images.BitmapToolkit
import com.mitch.fontpicker.ui.screens.camera.controlers.CameraController
import com.mitch.fontpicker.data.room.repository.FontPickerDatabaseRepository
import com.mitch.fontpicker.ui.screens.camera.controlers.FontRecognitionApiController
import com.mitch.fontpicker.ui.screens.camera.controlers.StorageController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

@Suppress("UNUSED")
class CameraViewModel(
    private val cameraController: CameraController,
    private val storageController: StorageController,
    private val fontRecognitionApiController: FontRecognitionApiController,
    private val fontDatabaseRepository: FontPickerDatabaseRepository,
    private val bitmapToolkit: BitmapToolkit
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

    private val _cameraPreview = MutableStateFlow<Preview?>(null)
    val cameraPreviewView: StateFlow<Preview?> = _cameraPreview

    init {
        viewModelScope.launch {
            _uiState.collect { newState ->
                Timber.d("CameraUiState changed to: $newState")

                // Reset _photoUri for specific states
                when (newState) {
                    is CameraUiState.Error,
                    is CameraUiState.CameraReady,
                    is CameraUiState.Success -> {
                        _photoUri.value = null
                        Timber.d("Photo URI reset due to state: $newState")
                    }
                    else -> {
                        // No action needed for other states
                    }
                }
            }
        }
    }

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
            val fileResult = runCatching { storageController.newFile(storageController.picturesDir) }
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
            viewModelScope.launch {
                val result = sendImageForProcessing(uri) // Make this a suspending call.

                // Ensure the UI state has been updated by waiting for the processing to complete.
                if (result.isSuccess) {
                    val fonts = result.getOrDefault(emptyList())
                    _uiState.value = if (fonts.isEmpty()) {
                        CameraUiState.CameraReady(cameraController.lensFacing)
                    } else {
                        CameraUiState.FontsReceived(fonts)
                    }

                    val currentState = _uiState.value
                    if (currentState is CameraUiState.FontsReceived) {
                        processReceivedFonts(currentState.fonts)
                    } else {
                        Timber.w("currentState was not set to CameraUiState.FontsReceived " +
                                "in onPhotoCaptured (actual value = $currentState).")
                    }
                } else {
                    onError("Failed to process captured image: ${result.exceptionOrNull()?.message}")
                }
            }
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
                // Copy the selected image to the pictures directory
                val copiedUri = storageController.copyImageToDirectory(context, uri, storageController.picturesDir)
                if (copiedUri == null) {
                    onError("Failed to prepare the selected image.")
                    return@launch
                }

                // Update state with the copied image URI
                _photoUri.value = copiedUri
                _isImageInUse.value = true
                _uiState.value = CameraUiState.ImageReady(copiedUri)
                Timber.d("Gallery image copied to pictures directory: $copiedUri")
                Timber.d("Gallery image selected: $uri, ready for display.")

                // Start processing the image
                _uiState.value = CameraUiState.Processing
                val result = sendImageForProcessing(copiedUri)

                // Handle the result of the image processing
                if (result.isSuccess) {
                    val fonts = result.getOrDefault(emptyList())
                    if (fonts.isNotEmpty()) {
                        _uiState.value = CameraUiState.FontsReceived(fonts)
                        processReceivedFonts(fonts)
                    } else {
                        Timber.d("No fonts detected. Returning to CameraReady state.")
                        _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
                    }
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error during image processing."
                    onError("Failed to process selected image: $errorMessage")
                    Timber.e("Error processing selected image: $errorMessage")
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
    private suspend fun sendImageForProcessing(uri: Uri): Result<List<FontResult>> {
        if (isProcessingImage) {
            Timber.w("Image processing already in progress.")
            return Result.failure(IllegalStateException("Image processing already in progress."))
        }

        return try {
            isProcessingImage = true
            _uiState.value = CameraUiState.Processing
            Timber.d("Processing image: $uri")

            // Process the image
            val result = withContext(Dispatchers.IO) {
                fontRecognitionApiController.processImage(uri)
            }

            // Update the UI state based on the result
            if (result.isFailure) {
                onError("Error processing image: ${result.exceptionOrNull()?.message}")
            } else {
                val fonts = result.getOrDefault(emptyList())
                _uiState.value = if (fonts.isEmpty()) {
                    Timber.d("No fonts detected. Showing empty font result.")
                    CameraUiState.CameraReady(cameraController.lensFacing)
                } else {
                    Timber.d("Font recognition successful. Fonts: \n$fonts")
                    CameraUiState.FontsReceived(fonts)
                }
            }

            result // Return the result
        } catch (e: Exception) {
            Timber.e(e, "Error processing image.")
            onError("Error processing image: ${e.message}")
            Result.failure(e)
        } finally {
            isProcessingImage = false
        }
    }

    /**
     * Process the fonts received from the font recognition API.
     *
     * @param fonts The list of FontResult objects received.
     */
    private fun processReceivedFonts(fonts: List<FontResult>) {
        if (_uiState.value !is CameraUiState.FontsReceived) {
            Timber.w("Attempted to process fonts in an invalid state: ${_uiState.value}")
            return
        }

        viewModelScope.launch {
            val downloadedFonts = mutableListOf<FontDownloaded>()
            try {
                _uiState.value = CameraUiState.DownloadingThumbnails(fonts)
                Timber.d("Starting to process received fonts.")

                withContext(Dispatchers.IO) {
                    fonts.forEach { font ->
                        val downloadedFont = convertFontResultToDownloaded(font)
                        if (downloadedFont != null) {
                            downloadedFonts.add(downloadedFont)
                        }
                    }
                }

                if (downloadedFonts.isNotEmpty()) {
                    _uiState.value = CameraUiState.OpeningFontsDialog(downloadedFonts)
                    Timber.d("Fonts processed successfully. Opening fonts dialog.")
                } else {
                    onError("Failed to download thumbnails for all fonts.")
                }
            } catch (e: Exception) {
                onError("Error while processing fonts: ${e.message}")
                Timber.e(e, "Error during fonts processing.")
                markThumbnailsAsNotInUse()
            }
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
     * Resets the view model's photo state and triggers a dir clear (optional).
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
            storageController.clearDirectory(storageController.picturesDir)
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

    private fun downloadThumbnails(fonts: List<FontResult>) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.DownloadingThumbnails(fonts)

            val downloadedFonts = mutableListOf<FontDownloaded>()

            withContext(Dispatchers.IO) {
                fonts.forEach { font ->
                    try {
                        val imageUrlBitmapPairs = listOf(font.imageUrl0, font.imageUrl1, font.imageUrl2).mapNotNull { imageUrl ->
                            val bitmap = bitmapToolkit.downloadAndProcess(
                                url = imageUrl,
                                makeJpegCompatible = true,
                                tempDirectory = storageController.thumbnailsDir,
                                targetHeight = 120,
                            )
                            if (bitmap != null) {
                                imageUrl to bitmap
                            } else {
                                null
                            }
                        }

                        if (imageUrlBitmapPairs.isNotEmpty()) {
                            val (imageUrls, bitmaps) = imageUrlBitmapPairs.unzip()
                            val downloadedFont = FontDownloaded(
                                title = font.title,
                                url = font.url,
                                imageUrls = imageUrls,
                                bitmaps = bitmaps
                            )
                            downloadedFonts.add(downloadedFont)
                        } else {
                            Timber.e("No thumbnails downloaded for font: ${font.title}")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error downloading thumbnails for font: ${font.title}")
                    }
                }
            }

            if (downloadedFonts.isNotEmpty()) {
                _uiState.value = CameraUiState.OpeningFontsDialog(downloadedFonts)
            } else {
                onError("Failed to download thumbnails for all fonts.")
            }
        }
    }

    private suspend fun convertFontResultToDownloaded(font: FontResult): FontDownloaded? {
        return try {
            // Create a list of image URLs from individual fields
            val imageUrls = listOfNotNull(font.imageUrl0, font.imageUrl1, font.imageUrl2)

            val imageUrlBitmapPairs = imageUrls.mapNotNull { imageUrl ->
                bitmapToolkit.downloadAndProcess(
                    url = imageUrl,
                    makeJpegCompatible = true,
                    tempDirectory = storageController.thumbnailsDir,
                    targetHeight = 120
                )?.let { bitmap -> imageUrl to bitmap }
            }

            if (imageUrlBitmapPairs.isNotEmpty()) {
                FontDownloaded(
                    title = font.title,
                    url = font.url,
                    imageUrls = imageUrlBitmapPairs.map { it.first },
                    bitmaps = imageUrlBitmapPairs.map { it.second }
                )
            } else {
                Timber.e("No thumbnails downloaded for font: ${font.title}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error converting font result to downloaded: ${font.title}")
            null
        }
    }

    /**
     * Called when the fonts dialog is dismissed without confirming selection.
     */
    fun onFontsDialogDismissed() {
        try{
            _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
            Timber.d("Fonts dialog dismissed. Returning to CameraReady state.")
        } finally {
            markThumbnailsAsNotInUse()
        }
    }

    /**
     * Called when the fonts dialog is confirmed with selected fonts.
     * @param selectedFonts The list of selected `FontDownloaded` items from the dialog.
     */
    fun onFontsDialogConfirmed(selectedFonts: List<FontDownloaded>) {
        viewModelScope.launch {
            try {
                _uiState.value = CameraUiState.SavingFavoriteFonts(selectedFonts)

                selectedFonts.forEach { font ->
                    fontDatabaseRepository.addFavoriteFont(font.title) // placeholder
                }

                Timber.d("Fonts dialog confirmed. Fonts saved.")
            } catch (e: Exception) {
                onError("Failed to confirm fonts selection: ${e.message}")
                Timber.e(e, "Error confirming fonts dialog.")
            } finally {
                markThumbnailsAsNotInUse()
                _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
            }
        }
    }


    /**
     * Clears the thumbnails directory and releases bitmaps.
     */
    private val thumbnailMutex = Mutex()

    private fun markThumbnailsAsNotInUse() {
        viewModelScope.launch {
            thumbnailMutex.withLock {
                try {
                    val currentState = _uiState.value

                    _uiState.value = CameraUiState.ClearingThumbnails

                    when (currentState) {
                        is CameraUiState.OpeningFontsDialog -> currentState.downloadedFonts
                        is CameraUiState.SavingFavoriteFonts -> currentState.downloadedFonts
                        else -> null
                    }?.forEach { font ->
                        font.bitmaps.forEach { bitmap ->
                            if (!bitmap.isRecycled) {
                                bitmap.recycle()
                                Timber.d("Bitmap recycled for font: ${font.title}")
                            }
                        }
                    }

                    storageController.clearDirectory(storageController.thumbnailsDir)
                    Timber.d("Thumbnails directory cleared.")
                } catch (e: Exception) {
                    Timber.e(e, "Error during thumbnail cleanup.")
                } finally {
                    _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
                }
            }
        }
    }
}
