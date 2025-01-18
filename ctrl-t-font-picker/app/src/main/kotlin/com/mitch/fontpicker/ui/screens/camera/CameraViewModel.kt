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
import com.mitch.fontpicker.data.room.model.BitmapData
import com.mitch.fontpicker.data.room.model.Font
import com.mitch.fontpicker.data.room.model.ImageUrl
import com.mitch.fontpicker.data.room.repository.FontsDatabaseRepository
import com.mitch.fontpicker.data.room.repository.FontsDatabaseRepository.Companion.CATEGORY_FAVORITES_NAME
import com.mitch.fontpicker.ui.screens.camera.controlers.CameraController
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


class CameraViewModel(
    private val cameraController: CameraController,
    private val storageController: StorageController,
    private val fontRecognitionApiController: FontRecognitionApiController,
    private val fontsDatabaseRepository: FontsDatabaseRepository,
    private val bitmapToolkit: BitmapToolkit
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.CameraReady())
    val uiState: StateFlow<CameraUiState> = _uiState

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri

    private val _galleryPickerEvent = MutableStateFlow(false)
    val galleryPickerEvent: StateFlow<Boolean> = _galleryPickerEvent

    private val _isImageInUse = MutableStateFlow(false)
    @Suppress("UNUSED")
    val isImageInUse: StateFlow<Boolean> = _isImageInUse

    private var isProcessingImage = false

    private val _cameraPreviewView = MutableStateFlow<Preview?>(null)
    val cameraPreviewView: StateFlow<Preview?> = _cameraPreviewView

    init {
        viewModelScope.launch {
            _uiState.collect { newState ->
                Timber.d("CameraUiState changed to: $newState")

                // Reset _imageUri for specific states
                when (newState) {
                    is CameraUiState.Error,
                    is CameraUiState.CameraReady,
                    is CameraUiState.Success -> {
                        _imageUri.value = null
                        _isImageInUse.value = false
                        Timber.d("Photo URI reset due to state: $newState. Releasing resources.")
                    }
                    else -> {
                        // No action needed for other states
                    }
                }
            }
        }
    }

    fun onBackHandler(){
        _uiState.value = CameraUiState.Success(
            "Back handler has interrupted camera operations. User hit back."
        )
    }

    /**
     * Load/Initialize the camera for the first time.
     */
    fun loadCameraProvider(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            val result = cameraController.initializeCamera(context, lifecycleOwner)
            if (result.isSuccess) {
                // <-- Set the new preview reference so the UI sees it
                _cameraPreviewView.value = cameraController.preview
                _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
            } else {
                onError("Failed to load camera provider: ${result.exceptionOrNull()?.message}")
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
                _cameraPreviewView.value = cameraController.preview

                _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
            } else {
                onError("Failed to flip camera: ${flipResult.exceptionOrNull()?.message}.")
            }
        }
    }

    /**
     * Converts the given image file to black and white with elevated contrast.
     *
     * @param filePath The path to the image file.
     * @return The processed image file.
     * @throws Exception If the processing fails.
     */
    private suspend fun makeImageHighContrastBw(filePath: String): File = withContext(Dispatchers.IO) {
        val bitmap = BitmapToolkit.getBitmapFromPath(filePath)

        val bitmapBw = BitmapToolkit.makeHighContrastBw(bitmap)
            ?: throw Exception("Failed to convert image to black and white.")

        val file = File(filePath)
        storageController.replaceJpegFileWithBitmap(file, bitmapBw)
        return@withContext file
    }

    /**
     * Capture a photo using CameraX, process it to black and white with elevated contrast, then proceed with UI updates.
     */
    fun capturePhoto(context: Context) {
        viewModelScope.launch {
            val fileResult = runCatching { storageController.newFile(storageController.picturesDir) }
            if (fileResult.isFailure) {
                onError("Failed to create photo file: ${fileResult.exceptionOrNull()?.message}.")
                return@launch
            }

            val photoFile: File = fileResult.getOrThrow()
            _uiState.value = CameraUiState.Processing

            val captureResult: Result<File> = cameraController.capturePhoto(context, photoFile)
            if (captureResult.isFailure) {
                onError("Photo capture failed: ${captureResult.exceptionOrNull()?.message}.")
                return@launch
            }

            val capturedFile = captureResult.getOrThrow()

            val processedResult = runCatching {
                makeImageHighContrastBw(capturedFile.absolutePath)
            }

            if (processedResult.isFailure) {
                onError("Failed to process image: ${processedResult.exceptionOrNull()?.message}.")
                return@launch
            }

            val processedFile = processedResult.getOrThrow()
            val capturedUri = Uri.fromFile(processedFile)
            _imageUri.value = capturedUri
            Timber.d("Photo captured and processed. File=$processedFile, contentUri=$capturedUri")
            _uiState.value = CameraUiState.ImageReady(capturedUri)

            onImageReady()
        }
    }

    /**
     * Called when a photo was successfully captured.
     */
    private fun onImageReady() {
        val uri = _imageUri.value
        if (uri != null) {
            viewModelScope.launch {
                val result = sendImageForProcessing(uri)

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
                    var errorMessage = result.exceptionOrNull()?.message
                    if (errorMessage == null) {
                        errorMessage = ("No response from server. " +
                                "\nPlease make sure that you're connected to the internet.")
                        onError(message = errorMessage)
                    } else {
                        onError(message = errorMessage)
                    }
                }
            }
        } else {
            onError("No photo URI available after capture.")
        }
    }

    /**
     * Handle a selected gallery image by copying it to pictures directory,
     * then updating the UI state.
     */
    fun onGalleryImageSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val copiedUri = storageController.copyImageToDirectory(context, uri, storageController.picturesDir)
                Timber.d("Gallery image copied to pictures directory: $copiedUri")

                if (copiedUri == null) {
                    onError("Failed to prepare the selected image.")
                    return@launch
                }

                // Convert the copied image to black and white with high contrast
                val bwConversionResult = runCatching {
                    val filePath = copiedUri.path ?: throw Exception("Invalid file path for copied image.")
                    makeImageHighContrastBw(filePath)
                }

                if (bwConversionResult.isFailure) {
                    onError("Failed to process the selected image: ${bwConversionResult.exceptionOrNull()?.message}")
                    return@launch
                }

                val processedFile = bwConversionResult.getOrThrow()
                val processedUri = Uri.fromFile(processedFile)

                _imageUri.value = processedUri
                _isImageInUse.value = true
                _uiState.value = CameraUiState.ImageReady(processedUri)

                Timber.d("Gallery image selected and processed to black and white: $processedUri, ready for display.")

                val result = sendImageForProcessing(processedUri)

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
                    val errorMessage = result.exceptionOrNull()?.message ?: ("Unknown error during image processing.")
                    onError("Failed to process selected image ($errorMessage).")
                    Timber.e("Error processing selected image: $errorMessage")
                }

            } catch (e: Exception) {
                onError("Failed to handle the selected image: ${e.message}.")
                Timber.e(e, "Error handling selected gallery image.")
            }
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
                onError("An error was encountered in image processing " +
                        "(${result.exceptionOrNull()?.message}).")
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
            onError("Fatal error was encountered in image processing (${e.message}).")
            Result.failure(e)
        } finally {
            isProcessingImage = false
            markImageAsNotInUse()
        }
    }

    private suspend fun setInitialLikedStates(downloadedFonts: List<FontDownloaded>) {
        downloadedFonts.forEach { fontDownloaded ->
            val font = fontsDatabaseRepository.fontBeforeInsertion(
                title = fontDownloaded.title,
                url = fontDownloaded.url
            )
            fontsDatabaseRepository.handleIdenticalInRecycleBin(font)
            val shouldStartLiked = fontsDatabaseRepository.shouldStartAsLiked(font)
            if (shouldStartLiked) {
                fontDownloaded.isLiked.value = true
                Timber.d("Set initial liked state for font '${fontDownloaded.title}' to true")
            }
        }
    }

    /**
     * Process the fonts received from the font recognition API.
     *
     * @param fonts The list of FontResult objects received.
     */
    private fun processReceivedFonts(fonts: List<FontResult>) {
        viewModelScope.launch {
            try {
                _uiState.value = CameraUiState.DownloadingThumbnails(fonts)
                Timber.d("Starting to process received fonts.")

                val downloadedFonts = downloadThumbnails(fonts)

                if (downloadedFonts.isNotEmpty()) {
                    // Set the initial liked state for each downloaded font
                    setInitialLikedStates(downloadedFonts)

                    _uiState.value = CameraUiState.OpeningFontsDialog(downloadedFonts)
                    Timber.d("Fonts processed successfully. Opening fonts dialog.")
                } else {
                    onError("Failed to download thumbnails for all fonts.")
                }
            } catch (e: Exception) {
                onError("Error while processing fonts: ${e.message}.")
                Timber.e(e, "Error during fonts processing.")
                markThumbnailsAsNotInUse()
                _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
            }
        }
    }

    /**
     * Resets the view model's photo state and triggers a dir clear (optional).
     */
    fun resetImageState() {
        _imageUri.value = null
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
        _uiState.value = CameraUiState.Error(errorMessage = message)
        markImageAsNotInUse()
        markThumbnailsAsNotInUse()
        Timber.e(message)
    }

    /**
     * Resets error state to CameraReady if needed.
     */
    fun resetErrorState() {
        _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
        Timber.d("Error state reset to success.")
    }

    private fun addFavoriteFont(fontDownloaded: FontDownloaded) {
        viewModelScope.launch {
            try {
                // Get or create the "Favorites" category
                val favoritesCategoryId = withContext(Dispatchers.IO) {
                    fontsDatabaseRepository.getOrCreateCategoryByName(CATEGORY_FAVORITES_NAME)
                }

                val font = Font(
                    title = fontDownloaded.title,
                    url = fontDownloaded.url,
                    categoryId = favoritesCategoryId
                )

                val imageUrls = fontDownloaded.imageUrls.map { url ->
                    ImageUrl(fontId = 0, url = url)
                }

                val bitmapDataList = fontDownloaded.bitmaps.map { bitmap ->
                    BitmapData(fontId = 0, bitmap = BitmapToolkit.encodeBinary(bitmap))
                }

                withContext(Dispatchers.IO) {
                    fontsDatabaseRepository.insertFontWithAssets(font, imageUrls, bitmapDataList)
                }

                Timber.d("Successfully added font '${font.title}' to Favorites.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to add font '${fontDownloaded.title}' to Favorites.")
            }
        }
    }

    private suspend fun downloadThumbnails(fonts: List<FontResult>): List<FontDownloaded> {
        return fonts.mapNotNull { fontResult ->
            convertFontResultToDownloaded(fontResult)
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
            fontsDatabaseRepository.dismissRecycling()
            fontsDatabaseRepository.dismissRestoration()
            _uiState.value = CameraUiState.CameraReady(cameraController.lensFacing)
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

                selectedFonts.forEach { fontDownloaded ->
                    addFavoriteFont(fontDownloaded) // Add each selected font to Favorites
                }

                Timber.d("Fonts dialog confirmed. Fonts saved to Favorites.")
                fontsDatabaseRepository.attemptRecycling()
                fontsDatabaseRepository.attemptRestoration()
            } catch (e: Exception) {
                onError("Failed to confirm fonts selection: ${e.message}")
                Timber.e(e, "Error confirming fonts dialog.")
                fontsDatabaseRepository.dismissRecycling()
                fontsDatabaseRepository.dismissRestoration()
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
                    when (currentState) {
                        is CameraUiState.OpeningFontsDialog -> currentState.downloadedFonts
                        is CameraUiState.SavingFavoriteFonts -> currentState.downloadedFonts
                        else -> null
                    }?.forEach { font ->
                        font.bitmaps.forEach { bitmap ->
                            BitmapToolkit.cleanUpBitmap(bitmap)
                        }
                    }

                    storageController.clearDirectory(storageController.thumbnailsDir)
                    Timber.d("Thumbnails directory cleared.")
                } catch (e: Exception) {
                    Timber.e(e, "Error during thumbnail cleanup.")
                }
            }
        }
    }
}
