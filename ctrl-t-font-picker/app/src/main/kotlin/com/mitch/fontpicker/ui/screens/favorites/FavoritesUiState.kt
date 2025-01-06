package com.mitch.fontpicker.ui.screens.favorites

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState

    data class Error(
        val error: String? = null
    ) : FavoritesUiState

    data class Success(
        val fontPreviews: List<Int> // Replace with actual data type for font previews (will be something else)
    ) : FavoritesUiState
}
