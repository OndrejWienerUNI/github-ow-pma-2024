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

@Composable
@Suppress("UNUSED_PARAMETER")
fun FontPickerNavHost(
    onShowSnackbar: suspend (SnackbarEvent) -> SnackbarResult,
    dependenciesProvider: DependenciesProvider,
    navController: NavHostController,
    startDestination: Screen,
    permissionsHandler: PermissionsHandler
) {
    NavHost(
        navController = navController,
        startDestination = when (startDestination) {
            Screen.Home -> "home"
            Screen.Permissions -> "permissions"
        }
    ) {
        // Permissions Screen Route
        composable("permissions") {
            val viewModel: PermissionsViewModel = viewModel(
                factory = PermissionsViewModelFactory(
                    permissionsHandler = permissionsHandler,
                    onPermissionsGranted = {
                        navController.navigate("home") {
                            popUpTo("permissions") { inclusive = true }
                        }
                    }
                )
            )
            PermissionsRoute(
                viewModel = viewModel,
                onPermissionsGranted = {
                    navController.navigate("home") {
                        popUpTo("permissions") { inclusive = true }
                    }
                }
            )
        }

        // Home Screen Route
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
