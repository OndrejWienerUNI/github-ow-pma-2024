package com.mitch.fontpicker.ui.screens.home.components.drawers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.R
import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.screens.home.HomeUiState
import com.mitch.fontpicker.ui.screens.home.components.branding.AppBrandingRow
import com.mitch.fontpicker.ui.designsystem.components.dialogs.LanguagePickerDialog
import com.mitch.fontpicker.ui.designsystem.components.dialogs.ThemePickerDialog


private enum class ActiveDialog {
    None, Language, Theme
}

private const val DRAWER_WIDTH_RATIO = 0.8f
private val DRAWER_WIDTH_MAX = 400.dp
private val DRAWER_CORNER_RADIUS = 16.dp
private val DRAWER_PADDING_TOP = 56.dp
private val DRAWER_PADDING_HORIZONTAL = 18.dp
private val DESCRIPTION_PADDING_BOTTOM = 20.dp
private val BUTTON_PADDING_BOTTOM = 16.dp
private val BUTTON_ICON_SIZE = 30.dp
private val BUTTON_ICON_PADDING_END = 12.dp

@Composable
fun HomeDrawerContent(
    uiState: HomeUiState,
    onChangeTheme: (FontPickerThemePreference) -> Unit,
    onChangeLanguage: (FontPickerLanguagePreference) -> Unit
) {
    var activeDialog by remember { mutableStateOf(ActiveDialog.None) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val drawerWidth = minOf(DRAWER_WIDTH_MAX, maxWidth * DRAWER_WIDTH_RATIO)

        Column(
            modifier = Modifier
                .width(drawerWidth)
                .fillMaxHeight()
                .background(
                    color = FontPickerDesignSystem.colorScheme.background,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        topEnd = DRAWER_CORNER_RADIUS,
                        bottomEnd = DRAWER_CORNER_RADIUS
                    )
                )
                .padding(
                    top = DRAWER_PADDING_TOP,
                    start = DRAWER_PADDING_HORIZONTAL,
                    end = DRAWER_PADDING_HORIZONTAL
                )
        ) {
            AppBrandingRow(
                iconResId = R.drawable.app_icon_cropped,
                titleResId = R.string.in_app_title,
                topMargin = 0.dp,
                bottomMargin = 18.dp
            )
            Text(
                text = stringResource(id = R.string.in_app_description),
                style = FontPickerDesignSystem.typography.titleMedium,
                modifier = Modifier.padding(bottom = DESCRIPTION_PADDING_BOTTOM)
            )

            // Button to change language
            Button(
                onClick = { activeDialog = ActiveDialog.Language },
                modifier = Modifier.padding(bottom = BUTTON_PADDING_BOTTOM)
            ) {
                Icon(
                    imageVector = FontPickerIcons.Outlined.Translate,
                    contentDescription = stringResource(R.string.change_language),
                    modifier = Modifier
                        .size(BUTTON_ICON_SIZE)
                        .padding(end = BUTTON_ICON_PADDING_END),
                    tint = FontPickerDesignSystem.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(id = R.string.change_language),
                    style = FontPickerDesignSystem.typography.bodyLarge
                )
            }

            // Button to change theme
            Button(
                onClick = { activeDialog = ActiveDialog.Theme },
                modifier = Modifier.padding(bottom = BUTTON_PADDING_BOTTOM)
            ) {
                Icon(
                    imageVector = if ((uiState as? HomeUiState.Success)?.theme
                        == FontPickerThemePreference.Light) {
                        FontPickerIcons.Outlined.LightMode
                    } else {
                        FontPickerIcons.Outlined.DarkMode
                    },
                    contentDescription = stringResource(R.string.change_theme),
                    modifier = Modifier
                        .size(BUTTON_ICON_SIZE)
                        .padding(end = BUTTON_ICON_PADDING_END),
                    tint = FontPickerDesignSystem.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(id = R.string.change_theme),
                    style = FontPickerDesignSystem.typography.bodyLarge
                )
            }
        }
    }

    // Handle Active Dialogs
    when (activeDialog) {
        ActiveDialog.Language -> LanguagePickerDialog(
            selectedLanguage = (uiState as? HomeUiState.Success)?.language
                ?: FontPickerLanguagePreference.English,
            onDismiss = { activeDialog = ActiveDialog.None },
            onConfirm = {
                activeDialog = ActiveDialog.None
                onChangeLanguage(it)
            }
        )

        ActiveDialog.Theme -> ThemePickerDialog(
            selectedTheme = (uiState as? HomeUiState.Success)?.theme
                ?: FontPickerThemePreference.Light,
            onDismiss = { activeDialog = ActiveDialog.None },
            onConfirm = {
                activeDialog = ActiveDialog.None
                onChangeTheme(it)
            }
        )

        ActiveDialog.None -> Unit
    }
}


@Preview
@Composable
private fun HomeDrawerContentPreview() {
    FontPickerTheme {
        HomeDrawerContent(
            uiState = HomeUiState.Success(
                language = FontPickerLanguagePreference.English,
                theme = FontPickerThemePreference.Light
            ),
            onChangeTheme = { /* Preview logic for changing theme */ },
            onChangeLanguage = { /* Preview logic for changing language */ }
        )
    }
}
