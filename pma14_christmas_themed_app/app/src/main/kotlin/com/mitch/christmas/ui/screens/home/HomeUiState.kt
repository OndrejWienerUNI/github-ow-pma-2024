package com.mitch.christmas.ui.screens.home

import com.mitch.christmas.domain.models.ChristmasLanguagePreference
import com.mitch.christmas.domain.models.ChristmasThemePreference

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Error(
        val error: String? = null
    ) : HomeUiState

    data class Success(
        val language: ChristmasLanguagePreference,
        val theme: ChristmasThemePreference
    ) : HomeUiState
}
