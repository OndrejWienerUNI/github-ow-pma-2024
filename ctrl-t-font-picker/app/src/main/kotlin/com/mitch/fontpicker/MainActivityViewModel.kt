package com.mitch.fontpicker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.fontpicker.data.settings.UserSettingsRepository
import com.mitch.fontpicker.di.DependenciesProvider
import com.mitch.fontpicker.ui.screens.permissions.PermissionsHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivityViewModel(
    application: Application,
    userSettingsRepository: UserSettingsRepository,
    permissionsHandler: PermissionsHandler
) : AndroidViewModel(application) {

    private val dependenciesProvider: DependenciesProvider by lazy {
        (getApplication<Application>() as FontPickerApplication).dependenciesProvider
    }

    init {
        Timber.d("MainActivityViewModel initialized with application: $application")
    }

    val uiState: StateFlow<MainActivityUiState> = userSettingsRepository.preferences
        .combine(permissionsHandler.allPermissionsGranted) { preferences, permissionsGranted ->
            Timber.d(
                "Combining preferences and permissions into uiState. " +
                        "Theme: ${preferences.theme}, PermissionsGranted: $permissionsGranted"
            )
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

    fun ensureAppDirectories() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Timber.d("Ensuring app directories.")
                val picturesDir = dependenciesProvider.picturesDir
                if (!picturesDir.exists() && picturesDir.mkdirs()) {
                    Timber.i("Directory created: ${picturesDir.absolutePath}")
                } else {
                    Timber.i("Directory already exists: ${picturesDir.absolutePath}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error ensuring directories.")
            }
        }
    }
}

