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
import timber.log.Timber

class MainActivityViewModel(
    application: Application,
    private val userSettingsRepository: UserSettingsRepository
) : AndroidViewModel(application) {

    init {
        Timber.d("MainActivityViewModel initialized with application: $application")
    }

    private val permissionsGrantedFlow = flow {
        val requiredPermission = android.Manifest.permission.CAMERA
        val isGranted = getApplication<Application>()
            .checkSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED
        Timber.d("Permission check for CAMERA: $isGranted")
        emit(isGranted)
    }

    val uiState: StateFlow<MainActivityUiState> = userSettingsRepository.preferences
        .combine(permissionsGrantedFlow) { preferences, permissionsGranted ->
            Timber.d("Combining preferences and permissions into uiState. Theme: ${preferences.theme}, PermissionsGranted: $permissionsGranted")
            MainActivityUiState.Success(
                theme = preferences.theme,
                permissionsGranted = permissionsGranted
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        ).also {
            Timber.d("uiState flow initialized with initial value: ${MainActivityUiState.Loading}")
        }
}
