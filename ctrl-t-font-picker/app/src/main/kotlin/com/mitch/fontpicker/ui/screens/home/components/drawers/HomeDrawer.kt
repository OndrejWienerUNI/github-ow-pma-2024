package com.mitch.fontpicker.ui.screens.home.components.drawers

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.screens.home.HomeUiState
import kotlinx.coroutines.launch

const val FIRST_PAGE_INDEX = 0

@Composable
fun HomeDrawer(
    uiState: HomeUiState,
    onChangeTheme: (FontPickerThemePreference) -> Unit,
    onChangeLanguage: (FontPickerLanguagePreference) -> Unit,
    currentPage: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()

    // Animate the top bar's alpha based on the current page
    val rowAlpha = animateFloatAsState(
        targetValue = if (currentPage == FIRST_PAGE_INDEX) 1f else 0f,
        label = "DisappearingAnimation"
    ).value

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                uiState = uiState,
                onChangeTheme = onChangeTheme,
                onChangeLanguage = onChangeLanguage
            )
        },
        content = {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
            ) {

                content() // Main screen content

                HomeDrawerTopBar(
                    rowAlpha = rowAlpha,
                    onToggleDrawer = {
                        if (drawerState.isClosed) {
                            scope.launch { drawerState.open() }
                        } else {
                            scope.launch { drawerState.close() }
                        }
                    },
                    modifier = modifier
                )
            }
        }
    )
}


@Preview(name = "Home Drawer - Closed")
@Composable
private fun HomeDrawerClosedPreview() {
    FontPickerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FontPickerDesignSystem.colorScheme.background)
        ) {

            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { 2 }
            )

            HomeDrawer(
                uiState = HomeUiState.Success(
                    language = FontPickerLanguagePreference.English,
                    theme = FontPickerThemePreference.Light
                ),
                onChangeTheme = { /* Stub: handle theme change */ },
                onChangeLanguage = { /* Stub: handle language change */ },
                modifier = Modifier,
                currentPage = pagerState.currentPage
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Main Content",
                        style = FontPickerDesignSystem.typography.bodyLarge,
                        color = FontPickerDesignSystem.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Preview(name = "Home Drawer - Opened")
@Composable
private fun HomeDrawerOpenedPreview() {
    FontPickerTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                HomeDrawerContent(
                    uiState = HomeUiState.Success(
                        language = FontPickerLanguagePreference.English,
                        theme = FontPickerThemePreference.Light
                    ),
                    onChangeTheme = { /* Stub: handle theme change */ },
                    onChangeLanguage = { /* Stub: handle language change */ }
                )
            },
            content = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Main Content", style = FontPickerDesignSystem.typography.bodyLarge)
                }
            }
        )
    }
}
