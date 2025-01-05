package com.mitch.fontpicker.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.R
import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.screens.home.HomeUiState
import com.mitch.fontpicker.ui.screens.home.components.LanguagePickerDialog
import com.mitch.fontpicker.ui.screens.home.components.ThemePickerDialog
import kotlinx.coroutines.launch

private enum class ActiveDialog {
    None, Language, Theme
}

@Composable
fun HomeDrawer(
    uiState: HomeUiState,
    onChangeTheme: (FontPickerThemePreference) -> Unit,
    onChangeLanguage: (FontPickerLanguagePreference) -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Manage the state of the active dialog within the drawer
    var activeDialog by remember { mutableStateOf(ActiveDialog.None) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BoxWithConstraints {
                val drawerWidth = minOf(400.dp, maxWidth * 0.8f)
                Column(
                    modifier = Modifier
                        .width(drawerWidth)
                        .fillMaxHeight()
                        .background(
                            color = FontPickerDesignSystem.colorScheme.surface,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 16.dp,
                                bottomEnd = 16.dp,
                                bottomStart = 0.dp
                            )
                        )
                        .padding(top = 56.dp, start = 18.dp, end = 18.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.in_app_title),
                        color = FontPickerDesignSystem.colorScheme.primary,
                        style = FontPickerDesignSystem.typography.displayMedium,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.in_app_description),
                        style = FontPickerDesignSystem.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp, bottom = 20.dp)
                    )
                    Button(
                        onClick = { activeDialog = ActiveDialog.Language },
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(text = stringResource(id = R.string.change_language))
                    }
                    Button(
                        onClick = { activeDialog = ActiveDialog.Theme },
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(text = stringResource(id = R.string.change_theme))
                    }
                }
            }
        }
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            // Drawer icon at the top-left
            IconButton(
                onClick = {
                    scope.launch {
                        if (drawerState.isClosed) {
                            drawerState.open()
                        } else {
                            drawerState.close()
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 18.dp)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.menu),
                    modifier = Modifier.size(32.dp)
                )
            }

            // Main content passed to the drawer
            content()

            // Handle Active Dialogs
            when (activeDialog) {
                ActiveDialog.Language -> LanguagePickerDialog(
                    selectedLanguage = if (uiState is HomeUiState.Success) uiState.language
                    else FontPickerLanguagePreference.English,
                    onDismiss = { activeDialog = ActiveDialog.None },
                    onConfirm = {
                        activeDialog = ActiveDialog.None
                        onChangeLanguage(it) // Forward the callback
                    }
                )

                ActiveDialog.Theme -> ThemePickerDialog(
                    selectedTheme = if (uiState is HomeUiState.Success) uiState.theme
                    else FontPickerThemePreference.Light,
                    onDismiss = { activeDialog = ActiveDialog.None },
                    onConfirm = {
                        activeDialog = ActiveDialog.None
                        onChangeTheme(it) // Forward the callback
                    }
                )

                ActiveDialog.None -> Unit // Do nothing
            }
        }
    }
}

