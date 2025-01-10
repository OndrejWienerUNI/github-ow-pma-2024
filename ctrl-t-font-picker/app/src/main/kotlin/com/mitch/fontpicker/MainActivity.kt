package com.mitch.fontpicker

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
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
import com.mitch.fontpicker.ui.rememberFontPickerAppState
import com.mitch.fontpicker.ui.screens.home.HomeRoute
import com.mitch.fontpicker.ui.screens.home.HomeViewModel
import com.mitch.fontpicker.ui.screens.permissions.PermissionsHandler
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var permissionsHandler: PermissionsHandler
    private lateinit var viewModel: MainActivityViewModel

    // Local state to store the current UI state
    private var uiState by mutableStateOf<MainActivityUiState>(MainActivityUiState.Loading)

    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Timber.i("Camera permission granted.")
                proceedWithPermissions(this)
            } else {
                Timber.e("Camera permission denied.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val dependenciesProvider = (application as FontPickerApplication).dependenciesProvider

        enableEdgeToEdge()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dependenciesProvider.userSettingsRepository.initLocaleIfNeeded()
            }
        }

        super.onCreate(savedInstanceState)

        permissionsHandler = PermissionsHandler(
            context = this,
            permissionLauncher = cameraPermissionRequest,
            onPermissionGranted = { permission -> Timber.i("$permission granted.") },
            onPermissionDenied = { permission -> Timber.e("$permission denied.") },
            onAllPermissionsGranted = {
                Timber.i("All permissions granted.")
                proceedWithPermissions(this)
            }
        )

        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MainActivityViewModel(
                            application = application,
                            userSettingsRepository = dependenciesProvider.userSettingsRepository
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        )[MainActivityViewModel::class.java]

        permissionsHandler.relaxStrictMode()

        // Observe UI state from the ViewModel
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    uiState = it
                }
            }
        }

        setContent {
            val themeInfo = themeInfo(uiState)

            DisposableEffect(themeInfo.isThemeDark, themeInfo.shouldFollowSystem) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT
                    ) { themeInfo.isThemeDark },
                    navigationBarStyle = SystemBarStyle.auto(
                        LightScrim,
                        DarkScrim
                    ) { themeInfo.isThemeDark }
                )
                setAppTheme(
                    uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager,
                    isThemeDark = themeInfo.isThemeDark,
                    shouldFollowSystem = themeInfo.shouldFollowSystem
                )
                onDispose { }
            }

            MainContent(
                uiState = uiState,
                dependenciesProvider = dependenciesProvider,
                permissionsHandler = permissionsHandler
            )
        }
    }

    private fun ensureAppDirectories(context: Context) {
        lifecycleScope.launch {
            try {
                val picturesDir = File(context.getExternalFilesDir("Pictures"), "FontPicker")
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

    private fun proceedWithPermissions(context: Context) {
        ensureAppDirectories(context)
        permissionsHandler.restoreStrictMode()
        proceedToHomeScreen()
    }

    private fun proceedToHomeScreen() {
        val dependenciesProvider = (application as FontPickerApplication).dependenciesProvider
        setContent {
            FontPickerTheme {
                HomeRoute(
                    viewModel = HomeViewModel(
                        userSettingsRepository = dependenciesProvider.userSettingsRepository
                    )
                )
            }
        }
    }
}


@Composable
fun MainContent(
    uiState: MainActivityUiState,
    dependenciesProvider: DependenciesProvider,
    permissionsHandler: PermissionsHandler
) {
    EnforceTheme(uiState)

    CompositionLocalProvider(LocalPadding provides padding) {
        FontPickerTheme(isThemeDark = themeInfo(uiState).isThemeDark) {
            val appState = rememberFontPickerAppState(
                networkMonitor = dependenciesProvider.networkMonitor
            )

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
                        startDestination = if (permissionsHandler.isPermissionGrantedForAll()) {
                            FontPickerDestination.Screen.Home
                        } else {
                            FontPickerDestination.Screen.Permissions
                        },
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

    DisposableEffect(activity, themeInfo.isThemeDark, themeInfo.shouldFollowSystem) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ) { themeInfo.isThemeDark },
            navigationBarStyle = SystemBarStyle.auto(
                LightScrim,
                DarkScrim
            ) { themeInfo.isThemeDark }
        )

        activity?.let {
            setAppTheme(
                uiModeManager = it.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager,
                isThemeDark = themeInfo.isThemeDark,
                shouldFollowSystem = themeInfo.shouldFollowSystem
            )
        }

        onDispose { }
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

