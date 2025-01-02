package com.mitch.fontpicker.ui.screens.home

import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import com.mitch.fontpicker.domain.models.FontPickerThemePreference

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Error(
        val error: String? = null
    ) : HomeUiState

    data class Success(
        val language: FontPickerLanguagePreference,
        val theme: FontPickerThemePreference
    ) : HomeUiState
}
