package com.mitch.christmas.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitch.christmas.R
import com.mitch.christmas.domain.models.ChristmasLanguagePreference
import com.mitch.christmas.domain.models.ChristmasThemePreference
import com.mitch.christmas.ui.designsystem.ChristmasTheme
import com.mitch.christmas.ui.designsystem.components.loading.LoadingScreen
import com.mitch.christmas.ui.screens.home.components.LanguagePickerDialog
import com.mitch.christmas.ui.screens.home.components.ThemePickerDialog

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
    onChangeTheme: (ChristmasThemePreference) -> Unit,
    onChangeLanguage: (ChristmasLanguagePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        HomeUiState.Loading -> LoadingScreen()

        is HomeUiState.Success -> {
            var activeDialog by remember { mutableStateOf(ActiveDialog.None) }
            when (activeDialog) {
                ActiveDialog.None -> Unit

                ActiveDialog.Language -> LanguagePickerDialog(
                    selectedLanguage = uiState.language,
                    onDismiss = { activeDialog = ActiveDialog.None },
                    onConfirm = onChangeLanguage
                )

                // TODO: Check how the theme is changed - it now resets to language from app start
                ActiveDialog.Theme -> ThemePickerDialog(
                    selectedTheme = uiState.theme,
                    onDismiss = { activeDialog = ActiveDialog.None },
                    onConfirm = onChangeTheme
                )
            }

            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { activeDialog = ActiveDialog.Language }) {
                    Text(text = stringResource(id = R.string.change_language))
                }

                Button(onClick = { activeDialog = ActiveDialog.Theme }) {
                    Text(text = stringResource(R.string.change_theme))
                }
            }
        }

        is HomeUiState.Error -> Unit
    }
}

private enum class ActiveDialog {
    None, Language, Theme
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun HomeScreenContentPreview() {
    ChristmasTheme {
        HomeScreen(
            uiState = HomeUiState.Success(
                language = ChristmasLanguagePreference.English,
                theme = ChristmasThemePreference.Light
            ),
            onChangeTheme = { },
            onChangeLanguage = { }
        )
    }
}