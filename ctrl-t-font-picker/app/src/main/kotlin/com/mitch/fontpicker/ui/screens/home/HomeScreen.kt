package com.mitch.fontpicker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.screens.camera.CameraScreen
import com.mitch.fontpicker.ui.screens.camera.CameraViewModel
import com.mitch.fontpicker.ui.screens.favorites.FavoritesScreen
import com.mitch.fontpicker.ui.screens.favorites.FavoritesViewModel
import com.mitch.fontpicker.ui.screens.home.components.HomeDrawer

val PAGE_PADDING_HORIZONTAL = 18.dp

@Composable
fun HomeRoute(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onChangeTheme = viewModel::updateTheme,
        onChangeLanguage = viewModel::updateLanguage
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
    // Semi-transparent box below the status bar
    Box(
        modifier = Modifier
            .zIndex(1f)
            .background(
                FontPickerDesignSystem.colorScheme.background.copy(alpha = 0.2f)
            )
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Drawer and main content
        HomeDrawer(
            uiState = uiState,
            onChangeTheme = onChangeTheme,
            onChangeLanguage = onChangeLanguage,
            currentPage = pagerState.currentPage,
            modifier = modifier
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> { CameraScreen(viewModel = CameraViewModel(), isPreview = isPreview) }
                    1 -> FavoritesScreen(viewModel = FavoritesViewModel())
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
