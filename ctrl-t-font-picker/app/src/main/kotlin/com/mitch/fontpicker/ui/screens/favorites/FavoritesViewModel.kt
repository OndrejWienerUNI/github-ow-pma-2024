package com.mitch.fontpicker.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.fontpicker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState

    init {
        loadFavoritesData()
    }

    private fun loadFavoritesData() {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(3000) // Simulated delay of 3 seconds
                _uiState.value = FavoritesUiState.Success(
                    fontPreviews = listOf(R.string.screen_desc_favorites)
                )
            } catch (e: Exception) {
                _uiState.value = FavoritesUiState.Error(error = e.message)
            }
        }
    }
}


