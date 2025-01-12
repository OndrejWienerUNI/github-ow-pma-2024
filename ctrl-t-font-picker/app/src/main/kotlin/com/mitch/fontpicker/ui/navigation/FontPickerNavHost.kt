package com.mitch.fontpicker.ui.navigation

import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mitch.fontpicker.di.DependenciesProvider
import com.mitch.fontpicker.ui.designsystem.components.snackbars.SnackbarEvent
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
    Timber.d("Navigating to $route. Current state: ${currentBackStackEntry?.lifecycle?.currentState}")
    if (currentBackStackEntry?.lifecycle?.currentState?.
        isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) == true) {
        Timber.d("Navigation allowed to $route")
        this.navigate(route) {
            popUpToRoute?.let {
                Timber.d("Pop up to route: $it, inclusive: $inclusive")
                popUpTo(it) { this.inclusive = inclusive }
            }
        }
    } else {
        Timber.w("Navigation to $route prevented. Current state is not RESUMED.")
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun FontPickerNavHost(
    onShowSnackbar: suspend (SnackbarEvent) -> SnackbarResult,
    dependenciesProvider: DependenciesProvider,
    navController: NavHostController,
    startDestination: String,
    permissionsHandler: PermissionsHandler
) {
    Timber.i("FontPickerNavHost initialized with startDestination: $startDestination")

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("permissions") {
            val viewModel: PermissionsViewModel = viewModel(
                factory = PermissionsViewModelFactory(
                    permissionsHandler = permissionsHandler
                )
            )
            PermissionsRoute(
                viewModel = viewModel
            )
        }

        composable("home") {
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

