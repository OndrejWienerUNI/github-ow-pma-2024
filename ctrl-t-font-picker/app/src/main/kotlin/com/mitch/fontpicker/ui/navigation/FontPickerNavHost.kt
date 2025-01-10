package com.mitch.fontpicker.ui.navigation

import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mitch.fontpicker.di.DependenciesProvider
import com.mitch.fontpicker.ui.designsystem.components.snackbars.SnackbarEvent
import com.mitch.fontpicker.ui.navigation.FontPickerDestination.Screen
import com.mitch.fontpicker.ui.screens.home.HomeRoute
import com.mitch.fontpicker.ui.screens.home.HomeViewModel
import com.mitch.fontpicker.ui.screens.permissions.PermissionsHandler
import com.mitch.fontpicker.ui.screens.permissions.PermissionsRoute
import com.mitch.fontpicker.ui.screens.permissions.PermissionsViewModel
import com.mitch.fontpicker.ui.screens.permissions.PermissionsViewModelFactory
import com.mitch.fontpicker.ui.util.viewModelProviderFactory
import timber.log.Timber

/**
 * Custom extension to safely navigate using `dropUnlessResumed`.
 */
fun NavHostController.navigateTo(
    route: String,
    popUpToRoute: String? = null,
    inclusive: Boolean = false
) {
    if (currentBackStackEntry?.lifecycle?.currentState?.
        isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) == true) {
        this.navigate(route) {
            popUpToRoute?.let {
                popUpTo(it) { this.inclusive = inclusive }
            }
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun FontPickerNavHost(
    onShowSnackbar: suspend (SnackbarEvent) -> SnackbarResult,
    dependenciesProvider: DependenciesProvider,
    navController: NavHostController,
    startDestination: Screen,
    permissionsHandler: PermissionsHandler
) {
    Timber.i("FontPickerNavHost: startDestination is $startDestination")

    NavHost(
        navController = navController,
        startDestination = when (startDestination) {
            Screen.Home -> "home"
            Screen.Permissions -> "permissions"
        }
    ) {
        composable("permissions") {
            Timber.i("Rendering PermissionsRoute")
            val viewModel: PermissionsViewModel = viewModel(
                factory = PermissionsViewModelFactory(
                    permissionsHandler = permissionsHandler,
                    onPermissionsGranted = {
                        Timber.i("onPermissionsGranted: Navigating to Home Screen from Permissions")
                        navController.navigateTo("home", popUpToRoute = "permissions", inclusive = true)
                    }
                )
            )
            PermissionsRoute(
                viewModel = viewModel,
                onPermissionsGranted = {
                    Timber.i("PermissionsRoute: onPermissionsGranted triggered")
                    navController.navigateTo("home", popUpToRoute = "permissions", inclusive = true)
                }
            )
        }

        composable("home") {
            Timber.i("Rendering HomeRoute")
            HomeRoute(
                viewModel = viewModel(
                    factory = viewModelProviderFactory {
                        HomeViewModel(
                            userSettingsRepository = dependenciesProvider.userSettingsRepository
                        )
                    }
                )
            )
        }
    }
}
