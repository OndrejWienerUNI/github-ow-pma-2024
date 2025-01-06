package com.mitch.fontpicker.ui.screens.gallery

sealed interface GalleryUiState {
    data object Loading : GalleryUiState

    data class Error(
        val error: String? = null
    ) : GalleryUiState

    data class Success(
        val fontPreviews: List<Int> // Replace with actual data type for font previews (will be something else)
    ) : GalleryUiState
}
