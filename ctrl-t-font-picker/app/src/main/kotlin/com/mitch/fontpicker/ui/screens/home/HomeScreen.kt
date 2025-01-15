package com.mitch.fontpicker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitch.fontpicker.data.images.BitmapToolkit
import com.mitch.fontpicker.di.DefaultDependenciesProvider
import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.components.backgrounds.BackgroundWithTintedStatusBar
import com.mitch.fontpicker.ui.screens.camera.CameraViewModel
import com.mitch.fontpicker.ui.screens.camera.controlers.CameraController
import com.mitch.fontpicker.ui.screens.camera.controlers.FontRecognitionApiController
import com.mitch.fontpicker.ui.screens.camera.controlers.StorageController
import com.mitch.fontpicker.ui.screens.favorites.FavoritesViewModel
import com.mitch.fontpicker.ui.screens.home.components.drawers.HomeDrawer
import com.mitch.fontpicker.ui.screens.home.components.pagers.HomePager
import com.mitch.fontpicker.ui.screens.home.components.pagers.HomePagerPreview
import com.mitch.fontpicker.ui.util.viewModelProviderFactory
import timber.log.Timber

@Composable
fun HomeRoute(
    viewModel: HomeViewModel
) {
    Timber.d("Rendering HomeRoute")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Timber.d("HomeRoute: UI State = $uiState")

    HomeScreen(
        uiState = uiState,
        onChangeTheme = {
            Timber.d("HomeRoute: Theme changed to $it")
            viewModel.updateTheme(it)
        },
        onChangeLanguage = {
            Timber.d("HomeRoute: Language changed to $it")
            viewModel.updateLanguage(it)
        }
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onChangeTheme: (FontPickerThemePreference) -> Unit,
    onChangeLanguage: (FontPickerLanguagePreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val dependenciesProvider = DefaultDependenciesProvider(context)

    // Create or remember all the controllers
    val cameraController = remember { CameraController() }
    val storageController = remember { StorageController(dependenciesProvider) }
    val fontRecognitionApiController = remember {
        FontRecognitionApiController(dependenciesProvider, context) }
    val fontsDatabaseRepository = remember { dependenciesProvider.databaseRepository }
    val bitmapToolkit = remember { BitmapToolkit(dependenciesProvider) }

    val cameraViewModel: CameraViewModel = viewModel(
        factory = viewModelProviderFactory {
            CameraViewModel(
                cameraController,
                storageController,
                fontRecognitionApiController,
                fontsDatabaseRepository,
                bitmapToolkit
            )
        }
    )
    cameraViewModel.loadCameraProvider(context, lifecycleOwner)

    val favoritesViewModel: FavoritesViewModel = viewModel(
        factory = viewModelProviderFactory {
            FavoritesViewModel(
                fontsDatabaseRepository,
                bitmapToolkit
            )
        }
    )

    Timber.d("Rendering HomeScreen with UI State: $uiState")
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )

    val horizontalPager: @Composable () -> Unit = if (isPreview) {
        { HomePagerPreview(pagerState = pagerState) }
    } else {
        {
            HomePager(
                pagerState = pagerState,
                cameraViewModel = cameraViewModel,
                favoritesViewModel = favoritesViewModel
            )
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onChangeTheme = onChangeTheme,
        onChangeLanguage = onChangeLanguage,
        modifier = modifier,
        horizontalPager = horizontalPager
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onChangeTheme: (FontPickerThemePreference) -> Unit,
    onChangeLanguage: (FontPickerLanguagePreference) -> Unit,
    horizontalPager: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Timber.d("Rendering HomeScreenContent with UI State: $uiState")
    BackgroundWithTintedStatusBar()

    Box(modifier = Modifier.fillMaxSize()) {
        Timber.d("Rendering HomeDrawer")
        HomeDrawer(
            uiState = uiState,
            onChangeTheme = {
                Timber.d("HomeScreenContent: Theme change triggered with $it")
                onChangeTheme(it)
            },
            onChangeLanguage = {
                Timber.d("HomeScreenContent: Language change triggered with $it")
                onChangeLanguage(it)
            },
            currentPage = 0, // Pager's current page should be managed outside if required
            modifier = modifier
        ) {
            Timber.d("Rendering HorizontalPager")
            horizontalPager() // Call the provided pager composable
        }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun HomeScreenContentPreview() {
    FontPickerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FontPickerDesignSystem.colorScheme.background)
        ) {
            HomeScreenContent(
                uiState = HomeUiState.Success(
                    language = FontPickerLanguagePreference.English,
                    theme = FontPickerThemePreference.Light
                ),
                onChangeTheme = { /* Stub: handle theme change */ },
                onChangeLanguage = { /* Stub: handle language change */ },
                horizontalPager = @Composable { HomePagerPreview() }
            )
        }
    }
}
