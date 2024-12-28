package com.mitch.christmas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.christmas.data.settings.UserSettingsRepository
import com.mitch.christmas.domain.models.ChristmasThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainActivityViewModel(
    userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    /**
     * Initial [MainActivity] ui state is set to [MainActivityUiState.Loading] and mapped to
     * [MainActivityUiState.Success] once the [ChristmasThemePreference] data is retrieved
     */
    val uiState: StateFlow<MainActivityUiState> = userSettingsRepository.preferences
        .map { MainActivityUiState.Success(theme = it.theme) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )
}

sealed class MainActivityUiState {
    data object Loading : MainActivityUiState()
    data class Success(val theme: ChristmasThemePreference) : MainActivityUiState()
}
