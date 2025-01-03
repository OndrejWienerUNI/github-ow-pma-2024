package com.mitch.fontpicker.ui.navigation

import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mitch.fontpicker.di.DependenciesProvider
import com.mitch.fontpicker.ui.designsystem.components.snackbars.SnackbarEvent
import com.mitch.fontpicker.ui.navigation.FontPickerDestination.Screen
import com.mitch.fontpicker.ui.screens.home.HomeRoute
import com.mitch.fontpicker.ui.screens.home.HomeViewModel
import com.mitch.fontpicker.ui.util.viewModelProviderFactory

@Composable
@Suppress("UNUSED_PARAMETER")
fun FontPickerNavHost(
    onShowSnackbar: suspend (SnackbarEvent) -> SnackbarResult,
    dependenciesProvider: DependenciesProvider,
    navController: NavHostController,
    startDestination: FontPickerDestination
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Home> {
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

// dropUnlessResumed is used to avoid navigating multiple times to the same destination or
// popping the backstack when the destination is already on top.
@Composable
@Suppress("Unused")
fun NavController.navigateTo(
    destination: FontPickerDestination,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
): () -> Unit = dropUnlessResumed {
    this.navigate(destination, navOptions, navigatorExtras)
}
