package com.mitch.fontpicker.ui.screens.favorites.components

import com.mitch.fontpicker.data.api.FontDownloaded

sealed interface FontCardListUiState {
    data object Loading : FontCardListUiState
    data class Error(
        val errorMessage: String? = null
    ) : FontCardListUiState
    data class Success(
        val fontPreviews: List<FontDownloaded>
    ) : FontCardListUiState
}

