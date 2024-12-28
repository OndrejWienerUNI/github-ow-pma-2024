package com.mitch.christmas.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.christmas.data.settings.UserSettingsRepository
import com.mitch.christmas.domain.models.ChristmasLanguagePreference
import com.mitch.christmas.domain.models.ChristmasThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = userSettingsRepository.preferences
        .map {
            HomeUiState.Success(
                language = it.language,
                theme = it.theme
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun updateTheme(theme: ChristmasThemePreference) {
        viewModelScope.launch {
            userSettingsRepository.setTheme(theme)
        }
    }

    fun updateLanguage(language: ChristmasLanguagePreference) {
        viewModelScope.launch {
            userSettingsRepository.setLanguage(language)
        }
    }
}
