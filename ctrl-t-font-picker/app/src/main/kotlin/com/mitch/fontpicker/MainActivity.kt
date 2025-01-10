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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var permissionsHandler: PermissionsHandler

    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Timber.i("Camera permission granted.")
                proceedWithPermissions(this) // Call proceedWithPermissions instead
            } else {
                Timber.e("Camera permission denied.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_FontPicker)
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawableResource(android.R.color.transparent)

        // Initialize PermissionsHandler
        permissionsHandler = PermissionsHandler(
            context = this,
            permissionLauncher = cameraPermissionRequest,
            onPermissionGranted = { permission -> Timber.i("$permission granted.") },
            onPermissionDenied = { permission -> Timber.e("$permission denied.") },
            onAllPermissionsGranted = {
                Timber.i("Permissions granted: Proceeding with app setup")
                proceedWithPermissions(this)
            }
        )

        // Relax StrictMode
        permissionsHandler.relaxStrictMode()

        // Set UI content
        val dependenciesProvider = (application as FontPickerApplication).dependenciesProvider
        setContent {
            MainContent(
                uiState = MainActivityUiState.Loading, // Replace with dynamic state if necessary
                dependenciesProvider = dependenciesProvider,
                permissionsHandler = permissionsHandler
            )
        }
    }

    private fun ensureAppDirectories(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            Timber.i("Attempting to ensure directories.")
            try {
                val picturesDir = File(context.getExternalFilesDir("Pictures"), "FontPicker")
                if (!picturesDir.exists()) {
                    Timber.i("Creating directory at: ${picturesDir.absolutePath}")
                    val created = picturesDir.mkdirs()
                    if (created) {
                        Timber.i("Directory successfully created.")
                    } else {
                        Timber.e("Failed to create directory.")
                    }
                } else {
                    Timber.i("Directory already exists: ${picturesDir.absolutePath}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error while ensuring directories.")
            }
        }
    }

    private fun proceedWithPermissions(context: Context) {
        Timber.i("proceedWithPermissions: Method called")
        try {
            Timber.i("proceedWithPermissions: Ensuring app directories.")
            ensureAppDirectories(context)

            Timber.i("proceedWithPermissions: Proceeding to Home Screen.")
            proceedToHomeScreen()
        } catch (e: Exception) {
            Timber.e(e, "Error in proceedWithPermissions")
        } finally {
            // Ensure strict mode is restored
            permissionsHandler.restoreStrictMode()
            Timber.i("proceedWithPermissions: Strict mode restored")
        }
    }

    private fun proceedToHomeScreen() {
        Timber.i("Proceeding to Home Screen.")
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
    // Ensure the theme and system bars are enforced
    EnforceTheme(uiState)

    CompositionLocalProvider(LocalPadding provides padding) {
        FontPickerTheme(isThemeDark = themeInfo(uiState).isThemeDark) {
            val appState = rememberFontPickerAppState(
                networkMonitor = dependenciesProvider.networkMonitor
            )
            val navController = appState.navController

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
                        navController = navController,
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

