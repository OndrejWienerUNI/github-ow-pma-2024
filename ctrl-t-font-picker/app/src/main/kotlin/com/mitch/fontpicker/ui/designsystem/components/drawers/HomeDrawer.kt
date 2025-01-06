package com.mitch.fontpicker.ui.designsystem.components.drawers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.ui.unit.dp

private val ICON_PADDING_HORIZONTAL = 6.dp

@Composable
fun HomeDrawer(
    uiState: HomeUiState,
    onChangeTheme: (FontPickerThemePreference) -> Unit,
    onChangeLanguage: (FontPickerLanguagePreference) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(
        initialValue = androidx.compose.material3.DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()

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

                // Drawer toggle icon respects system bars' padding
                DrawerToggleIcon(
                    onToggleDrawer = {
                        if (drawerState.isClosed) {
                            scope.launch { drawerState.open() }
                        } else {
                            scope.launch { drawerState.close() }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(horizontal = ICON_PADDING_HORIZONTAL)
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
            HomeDrawer(
                uiState = HomeUiState.Success(
                    language = FontPickerLanguagePreference.English,
                    theme = FontPickerThemePreference.Light
                ),
                onChangeTheme = { /* Stub: handle theme change */ },
                onChangeLanguage = { /* Stub: handle language change */ },
                modifier = Modifier
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
        val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Open)

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
