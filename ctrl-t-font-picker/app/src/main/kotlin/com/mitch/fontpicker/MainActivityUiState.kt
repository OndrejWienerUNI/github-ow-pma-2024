package com.mitch.fontpicker

import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import timber.log.Timber

sealed class MainActivityUiState {
    data object Loading : MainActivityUiState() {
        init {
            Timber.d("MainActivityUiState.Loading initialized")
        }
    }

    data class Success(
        val theme: FontPickerThemePreference,
        val permissionsGranted: Boolean
    ) : MainActivityUiState() {
        init {
            Timber.d(
                "MainActivityUiState.Success initialized with theme: $theme, permissionsGranted: $permissionsGranted"
            )
        }
    }
}
