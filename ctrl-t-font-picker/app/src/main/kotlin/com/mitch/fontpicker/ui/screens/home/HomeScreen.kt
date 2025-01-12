package com.mitch.fontpicker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.screens.camera.CameraScreen
import com.mitch.fontpicker.ui.screens.camera.CameraViewModel
import com.mitch.fontpicker.ui.screens.favorites.FavoritesScreen
import com.mitch.fontpicker.ui.screens.favorites.FavoritesViewModel
import com.mitch.fontpicker.ui.designsystem.components.backgrounds.BackgroundWithTintedStatusBar
import com.mitch.fontpicker.ui.screens.home.components.drawers.HomeDrawer
import timber.log.Timber

val PAGE_PADDING_HORIZONTAL = 18.dp

@Composable
fun HomeRoute(viewModel: HomeViewModel) {
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
    isPreview: Boolean = false
) {
    Timber.d("Rendering HomeScreen with UI State: $uiState")
    BackgroundWithTintedStatusBar()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )
    Timber.d("Pager state initialized with initialPage = 0 and pageCount = 2")

    Box(modifier = Modifier.fillMaxSize()) {
        // Drawer and main content
        Timber.d("Rendering HomeDrawer")
        HomeDrawer(
            uiState = uiState,
            onChangeTheme = {
                Timber.d("HomeScreen: Theme change triggered with $it")
                onChangeTheme(it)
            },
            onChangeLanguage = {
                Timber.d("HomeScreen: Language change triggered with $it")
                onChangeLanguage(it)
            },
            currentPage = pagerState.currentPage,
            modifier = modifier
        ) {
            Timber.d("Rendering HorizontalPager")
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Timber.d("HorizontalPager: Rendering page $page")
                when (page) {
                    0 -> {
                        Timber.d("Rendering CameraScreen")
                        CameraScreen(viewModel = CameraViewModel(), isPreview = isPreview)
                    }
                    1 -> {
                        Timber.d("Rendering FavoritesScreen")
                        FavoritesScreen(viewModel = FavoritesViewModel())
                    }
                    else -> {
                        Timber.w("HorizontalPager: Unknown page $page")
                    }
                }
            }
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
            HomeScreen(
                uiState = HomeUiState.Success(
                    language = FontPickerLanguagePreference.English,
                    theme = FontPickerThemePreference.Light
                ),
                onChangeTheme = { /* Stub: handle theme change */ },
                onChangeLanguage = { /* Stub: handle language change */ },
                isPreview = true
            )
        }
    }
}
