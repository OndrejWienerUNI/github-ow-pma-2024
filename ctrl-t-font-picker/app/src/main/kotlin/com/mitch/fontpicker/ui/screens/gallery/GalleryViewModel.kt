package com.mitch.fontpicker.ui.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.fontpicker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState

    init {
        loadGalleryData()
    }

    private fun loadGalleryData() {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(3000) // Simulated delay of 3 seconds
                _uiState.value = GalleryUiState.Success(
                    fontPreviews = listOf(R.string.screen_desc_gallery)
                )
            } catch (e: Exception) {
                _uiState.value = GalleryUiState.Error(error = e.message)
            }
        }
    }
}


