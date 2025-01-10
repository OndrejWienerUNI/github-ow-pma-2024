package com.mitch.fontpicker

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.fontpicker.data.settings.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class MainActivityViewModel(
    application: Application,
    private val userSettingsRepository: UserSettingsRepository
) : AndroidViewModel(application) {

    private val permissionsGrantedFlow = flow {
        val requiredPermission = android.Manifest.permission.CAMERA
        val isGranted = getApplication<Application>()
            .checkSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED
        emit(isGranted)
    }

    val uiState: StateFlow<MainActivityUiState> = userSettingsRepository.preferences
        .combine(permissionsGrantedFlow) { preferences, permissionsGranted ->
            MainActivityUiState.Success(
                theme = preferences.theme,
                permissionsGranted = permissionsGranted
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )
}
