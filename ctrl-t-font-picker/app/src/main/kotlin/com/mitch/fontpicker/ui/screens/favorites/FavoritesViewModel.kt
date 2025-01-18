package com.mitch.fontpicker.ui.screens.favorites

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

class FavoritesViewModel(
    private val fontsDatabaseRepository: FontsDatabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FontCardListUiState>(FontCardListUiState.Loading)
    val uiState: StateFlow<FontCardListUiState> = _uiState

    // Make load delay accessible from the outside
    @Suppress("MemberVisibilityCanBePrivate")
    var screenLoadDelay: Long = 1000

    init {
        viewModelScope.launch {
            _uiState.collect { newState ->
                Timber.d("FavoritesUiState changed to: $newState")
            }
        }
    }

    fun startObservingFavorites() {
        viewModelScope.launch {
            // Keep the loading state for one second
            _uiState.value = FontCardListUiState.Loading
            delay(screenLoadDelay)
            Timber.d("Initial delay completed. Starting to observe favorites.")
            observeFavorites()
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun observeFavorites() {
        viewModelScope.launch {
            try {
                Timber.d("Fetching favorites with assets.")
                fontsDatabaseRepository.getFavoritesWithAssets()
                    .map { fontWithAssets: List<FontsDatabaseRepository.Companion.FontWithAssets> ->
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
                                isLiked = mutableStateOf(asset.font.categoryId == fontsDatabaseRepository.favoritesCategoryId)
                            )
                        }
                    }
                    .collect { fontPreviews ->
                        Timber.d("Collected ${fontPreviews.size} FontDownloaded instances.")
                        _uiState.value = FontCardListUiState.Success(fontPreviews = fontPreviews)
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to observe favorites data.")
                _uiState.value = FontCardListUiState.Error(errorMessage = e.message)
            }
        }
    }

    fun toggleLike(font: FontDownloaded) {
        viewModelScope.launch {
            try {
                font.id?.let { fontId ->
                    Timber.d("Toggling like for font: ${font.title} (id=$fontId)")
                    fontsDatabaseRepository.moveToRecycleBin(fontId)
                    Timber.d("Font '${font.title}' moved to Recycle Bin.")
                } ?: Timber.e("Cannot toggle like for font with null ID: ${font.title}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle like for font '${font.title}'.")
            }
        }
    }
}

