package com.mitch.fontpicker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitch.fontpicker.data.settings.UserSettingsRepository
import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MainActivityViewModel(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    // Mutable state to track app-level states
    private val _permissionsGranted = MutableStateFlow(false)
    private val _showHomeScreen = MutableStateFlow(false)
    private val _directoriesInitialized = MutableStateFlow(false)

    /**
     * Combine the preferences from [UserSettingsRepository] with the permissions state
     * and other app states to produce the [MainActivityUiState].
     */
    val uiState: StateFlow<MainActivityUiState> = combine(
        userSettingsRepository.preferences.map { it.theme },
        _permissionsGranted,
        _showHomeScreen,
        _directoriesInitialized
    ) { theme, permissionsGranted, showHomeScreen, directoriesInitialized ->
        if (!directoriesInitialized) {
            MainActivityUiState.Loading
        } else {
            MainActivityUiState.Success(
                theme = theme,
                permissionsGranted = permissionsGranted,
                showHomeScreen = showHomeScreen
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainActivityUiState.Loading
    )

    /**
     * Updates the permissions granted state to [granted].
     */
    fun updatePermissions(granted: Boolean) {
        Timber.i("Permissions granted updated: $granted")
        _permissionsGranted.value = granted
        if (granted) {
            proceedToHomeScreen()
        }
    }

    /**
     * Proceed to the home screen if permissions are granted.
     */
    private fun proceedToHomeScreen() {
        Timber.i("Proceeding to Home Screen.")
        _showHomeScreen.value = true
    }

    /**
     * Ensure application directories are initialized.
     */
    fun ensureAppDirectories(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val picturesDir = File(context.getExternalFilesDir("Pictures"), "FontPicker")
                if (!picturesDir.exists()) {
                    Timber.i("Creating directory at: ${picturesDir.absolutePath}")
                    picturesDir.mkdirs()
                } else {
                    Timber.i("Directory already exists: ${picturesDir.absolutePath}")
                }
                _directoriesInitialized.value = true
            } catch (e: Exception) {
                Timber.e(e, "Error ensuring directories.")
            }
        }
    }
}


sealed class MainActivityUiState {
    data object Loading : MainActivityUiState()
    data class Success(
        val theme: FontPickerThemePreference,
        val permissionsGranted: Boolean,
        val showHomeScreen: Boolean = false
    ) : MainActivityUiState()
}

