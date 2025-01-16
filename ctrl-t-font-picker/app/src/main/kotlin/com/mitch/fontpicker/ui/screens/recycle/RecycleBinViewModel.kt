package com.mitch.fontpicker.ui.screens.recycle

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.fontpicker.data.api.FontDownloaded
import com.mitch.fontpicker.data.images.BitmapToolkit
import com.mitch.fontpicker.data.room.repository.FontsDatabaseRepository
import com.mitch.fontpicker.ui.screens.favorites.components.FontCardListUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class RecycleBinViewModel(
    private val fontsDatabaseRepository: FontsDatabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FontCardListUiState>(FontCardListUiState.Loading)
    val uiState: StateFlow<FontCardListUiState> = _uiState

    private val _isRecycleBinEmpty = MutableStateFlow(true) // Initially assume the recycle bin is empty
    val isRecycleBinEmpty: StateFlow<Boolean> = _isRecycleBinEmpty

    // Make load delay accessible from the outside
    @Suppress("MemberVisibilityCanBePrivate")
    var screenLoadDelay: Long = 1000

    init {
        viewModelScope.launch {
            _uiState.collect { newState ->
                Timber.d("RecycleBinUiState changed to: $newState")
            }
        }
    }

    fun startObservingRecycleBin() {
        viewModelScope.launch {
            // Keep the loading state for one second
            _uiState.value = FontCardListUiState.Loading
            delay(screenLoadDelay)
            Timber.d("Initial delay completed. Starting to observe recycle bin.")
            observeRecycleBin()
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun observeRecycleBin() {
        viewModelScope.launch {
            try {
                Timber.d("Fetching recycle bin with assets.")
                fontsDatabaseRepository.getRecycleBinWithAssets()
                    .map { fontWithAssets ->
                        Timber.d("Mapping ${fontWithAssets.size} FontWithAssets to FontDownloaded.")
                        fontWithAssets.map { asset ->
                            val bitmaps = asset.bitmapData.firstOrNull()?.map { bitmapData ->
                                BitmapToolkit.decodeBinary(bitmapData.bitmap)
                            } ?: emptyList<Bitmap>().also { Timber.d("No binaries to be decoded.") }

                            val imageUrls = asset.imageUrls.firstOrNull()?.map { it.url } ?: emptyList()

                            Timber.d(
                                "Mapped FontWithAssets for font: ${asset.font.title}, ID=${asset.font.id}, " +
                                        "ImageUrls=${imageUrls.size}, Bitmaps=${bitmaps.size}"
                            )

                            FontDownloaded(
                                id = asset.font.id,
                                title = asset.font.title,
                                url = asset.font.url,
                                imageUrls = imageUrls,
                                bitmaps = bitmaps,
                                isLiked = mutableStateOf(false) // Fonts in recycle bin are not liked
                            )
                        }
                    }
                    .collect { fontPreviews ->
                        Timber.d("Collected ${fontPreviews.size} FontDownloaded instances.")
                        _isRecycleBinEmpty.value = fontPreviews.isEmpty()
                        _uiState.value = FontCardListUiState.Success(fontPreviews = fontPreviews)
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to observe recycle bin data.")
                _uiState.value = FontCardListUiState.Error(error = e.message)
            }
        }
    }

    fun restoreFont(font: FontDownloaded) {
        viewModelScope.launch {
            try {
                font.id?.let { fontId ->
                    Timber.d("Restoring font: ${font.title} (id=$fontId)")
                    fontsDatabaseRepository.moveToFavorites(fontId)
                    Timber.d("Font '${font.title}' restored to Favorites.")
                    observeRecycleBin() // Refresh the data
                } ?: Timber.e("Cannot restore font with null ID: ${font.title}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to restore font '${font.title}'.")
            }
        }
    }

    @Suppress("UNUSED")
    fun deleteFont(font: FontDownloaded) {
        viewModelScope.launch {
            try {
                font.id?.let { fontId ->
                    Timber.d("Deleting font: ${font.title} (id=$fontId) from Recycle Bin.")
                    fontsDatabaseRepository.deleteRecycledFont(fontId)
                    Timber.d("Font '${font.title}' deleted from Recycle Bin.")
                    observeRecycleBin() // Refresh the data
                } ?: Timber.e("Cannot delete font with null ID: ${font.title}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete font '${font.title}'.")
            }
        }
    }

    fun wipeRecycleBin() {
        viewModelScope.launch {
            try {
                Timber.d("Wiping all fonts from the recycle bin.")
                fontsDatabaseRepository.wipeRecycleBin()
                Timber.d("Recycle Bin cleared.")
                observeRecycleBin() // Refresh the data
            } catch (e: Exception) {
                Timber.e(e, "Failed to wipe recycle bin.")
            }
        }
    }
}
