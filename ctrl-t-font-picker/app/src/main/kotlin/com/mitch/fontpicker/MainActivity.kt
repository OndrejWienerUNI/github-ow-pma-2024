package com.mitch.fontpicker

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mitch.fontpicker.di.DependenciesProvider
import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.components.snackbars.FontPickerSnackbarHost
import com.mitch.fontpicker.ui.designsystem.components.snackbars.toVisuals
import com.mitch.fontpicker.ui.designsystem.theme.custom.LocalPadding
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding
import com.mitch.fontpicker.ui.navigation.FontPickerDestination
import com.mitch.fontpicker.ui.navigation.FontPickerNavHost
import com.mitch.fontpicker.ui.navigation.navigateTo
import com.mitch.fontpicker.ui.rememberFontPickerAppState
import com.mitch.fontpicker.ui.screens.permissions.PermissionsHandler
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var permissionsHandler: PermissionsHandler
    private lateinit var viewModel: MainActivityViewModel

    // Local state to store the current UI state
    private var uiState by mutableStateOf<MainActivityUiState>(MainActivityUiState.Loading)

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        super.onCreate(savedInstanceState)

        Timber.d("MainActivity onCreate started.")
        val dependenciesProvider = (application as FontPickerApplication).dependenciesProvider

        enableEdgeToEdge()
        Timber.d("Edge-to-edge enabled.")

        // Initialize the PermissionsHandler
        permissionsHandler = PermissionsHandler(
            context = this,
            activityResultRegistry = activityResultRegistry
        )
        Timber.d("PermissionsHandler initialized.")

        // Initialize the ViewModel
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    Timber.d("Creating ViewModel for ${modelClass.simpleName}")
                    if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MainActivityViewModel(
                            application = application,
                            userSettingsRepository = dependenciesProvider.userSettingsRepository,
                            permissionsHandler = permissionsHandler
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                }
            }
        )[MainActivityViewModel::class.java]
        Timber.d("ViewModel initialized: $viewModel")


        // Observe the UI state in the ViewModel
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { newState ->
                    Timber.d("UI state updated: $newState")
                    uiState = newState
                }
            }
        }

        // Set up the content
        setContent {
            Timber.d("Setting content.")
            EnforceTheme(uiState)

            MainContent(
                uiState = uiState,
                dependenciesProvider = dependenciesProvider,
                permissionsHandler = permissionsHandler,
                viewModel = viewModel
            )
        }

    }
}

@Composable
fun MainContent(
    uiState: MainActivityUiState,
    dependenciesProvider: DependenciesProvider,
    permissionsHandler: PermissionsHandler,
    viewModel: MainActivityViewModel
) {
    Timber.d("MainContent Composable started with uiState: $uiState")

    val allPermissionsGranted by permissionsHandler.allPermissionsGranted.collectAsState()

    // Ensure app directories only once when permissions are granted
    LaunchedEffect(allPermissionsGranted) {
        if (allPermissionsGranted) {
            viewModel.ensureAppDirectories()
        }
    }

    CompositionLocalProvider(LocalPadding provides padding) {
        FontPickerTheme(isThemeDark = themeInfo(uiState).isThemeDark) {
            val appState = rememberFontPickerAppState(
                networkMonitor = dependenciesProvider.networkMonitor
            )

            val startDestination = remember(allPermissionsGranted) {
                if (allPermissionsGranted) {
                    FontPickerDestination.Screen.Home.toString()
                } else {
                    FontPickerDestination.Screen.Permissions.toString()
                }
            }

            // Navigation logic for dynamic navigation
            LaunchedEffect(allPermissionsGranted) {
                if (allPermissionsGranted && appState.navController.currentDestination?.route == "permissions") {
                    Timber.d("Navigating to Home")
                    appState.navController.navigateTo("home", "permissions", inclusive = true)
                }
            }

            Scaffold(
                snackbarHost = { FontPickerSnackbarHost(appState.snackbarHostState) },
                contentWindowInsets = WindowInsets(0)
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                        )
                ) {
                    FontPickerNavHost(
                        onShowSnackbar = { event ->
                            appState.snackbarHostState.showSnackbar(event.toVisuals())
                        },
                        dependenciesProvider = dependenciesProvider,
                        navController = appState.navController,
                        startDestination = startDestination,
                        permissionsHandler = permissionsHandler
                    )
                }
            }
        }
    }
}


private data class ThemeInfo(val isThemeDark: Boolean, val shouldFollowSystem: Boolean)

@Composable
private fun themeInfo(uiState: MainActivityUiState): ThemeInfo {
    return when (uiState) {
        MainActivityUiState.Loading -> ThemeInfo(
            isThemeDark = isSystemInDarkTheme(),
            shouldFollowSystem = false
        )
        is MainActivityUiState.Success -> {
            val isThemeDark = uiState.theme == FontPickerThemePreference.Dark
            val shouldFollowSystem = uiState.theme == FontPickerThemePreference.FollowSystem
            ThemeInfo(
                isThemeDark = isThemeDark || (shouldFollowSystem && isSystemInDarkTheme()),
                shouldFollowSystem = shouldFollowSystem
            )
        }
    }
}

@Composable
fun EnforceTheme(uiState: MainActivityUiState) {
    val activity = LocalContext.current as? ComponentActivity
    val themeInfo = themeInfo(uiState)

    LaunchedEffect(themeInfo) {
        Timber.d("Applying theme: isThemeDark=${themeInfo.isThemeDark}, " +
                "shouldFollowSystem=${themeInfo.shouldFollowSystem}")

        activity?.let {
            // Apply edge-to-edge styles
            it.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT
                ) { themeInfo.isThemeDark },
                navigationBarStyle = SystemBarStyle.auto(
                    LightScrim,
                    DarkScrim
                ) { themeInfo.isThemeDark }
            )

            // Apply app theme
            setAppTheme(
                uiModeManager = it.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager,
                isThemeDark = themeInfo.isThemeDark,
                shouldFollowSystem = themeInfo.shouldFollowSystem
            )
        }
    }
}

@SuppressLint("ObsoleteSdkInt")
private fun setAppTheme(
    uiModeManager: UiModeManager,
    isThemeDark: Boolean,
    shouldFollowSystem: Boolean
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val mode = when {
            shouldFollowSystem -> UiModeManager.MODE_NIGHT_AUTO
            isThemeDark -> UiModeManager.MODE_NIGHT_YES
            else -> UiModeManager.MODE_NIGHT_NO
        }
        uiModeManager.setApplicationNightMode(mode)
    } else {
        val mode = when {
            shouldFollowSystem -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            isThemeDark -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}

private val LightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

