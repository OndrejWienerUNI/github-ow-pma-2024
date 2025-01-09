package com.mitch.fontpicker

import com.mitch.fontpicker.domain.models.FontPickerThemePreference

sealed class MainActivityUiState {
    data object Loading : MainActivityUiState()
    data class Success(
        val theme: FontPickerThemePreference,
        val permissionsGranted: Boolean
    ) : MainActivityUiState()
}
