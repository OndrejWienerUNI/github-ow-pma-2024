package com.mitch.fontpicker.ui.screens.home.components.pagers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.screens.camera.CameraRoute
import com.mitch.fontpicker.ui.screens.camera.CameraScreenContentPreview
import com.mitch.fontpicker.ui.screens.camera.CameraViewModel
import com.mitch.fontpicker.ui.screens.favorites.FavoritesRoute
import com.mitch.fontpicker.ui.screens.favorites.FavoritesScreenContentPreview
import com.mitch.fontpicker.ui.screens.favorites.FavoritesViewModel
import timber.log.Timber

@Composable
fun HomePager(
    pagerState: PagerState,
    cameraViewModel: CameraViewModel,
    favoritesViewModel: FavoritesViewModel,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.background(
            color = FontPickerDesignSystem.colorScheme.surface,
            shape = CardDefaults.shape)
    ) { page ->
        when (page) {
            0 -> CameraRoute(
                viewModel = cameraViewModel
            )
            1 -> FavoritesRoute(
                viewModel = favoritesViewModel
            )
            else -> Timber.w("HomePager: Unknown page $page")
        }
    }
}

@Preview
@Composable
fun HomePagerPreview(
    pagerState: PagerState
        = rememberPagerState(initialPage = 0, pageCount = { 1 }
    ),
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        when (page) {
            0 -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FontPickerDesignSystem.colorScheme.primary)
            ) {
                CameraScreenContentPreview()
            }
            1 -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FontPickerDesignSystem.colorScheme.secondary)
            ) {
                FavoritesScreenContentPreview()
            }
            else -> Timber.w("HomePagerPreview: Unknown page $page")
        }
    }
}

