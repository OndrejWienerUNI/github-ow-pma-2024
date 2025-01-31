package com.mitch.fontpicker.ui.designsystem.components.dialogs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mitch.fontpicker.R
import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding

@Composable
fun ThemePickerDialog(
    selectedTheme: FontPickerThemePreference,
    onDismiss: () -> Unit,
    onConfirm: (FontPickerThemePreference) -> Unit
) {
    var tempTheme by remember { mutableStateOf(selectedTheme) }

    val items = listOf(
        ThemePickerItem.FollowSystem,
        ThemePickerItem.Light,
        ThemePickerItem.Dark
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = FontPickerIcons.Outlined.Palette,
                contentDescription = null,
                modifier = Modifier.padding(
                    top = padding.small,
                    bottom = padding.zero,
                    start = padding.zero,
                    end = padding.zero,
                )
            )
        },
        title = {
            Text(text = stringResource(id = R.string.change_theme))
        },
        containerColor = FontPickerDesignSystem.colorScheme.surface,
        textContentColor = FontPickerDesignSystem.colorScheme.onSurface,
        titleContentColor = FontPickerDesignSystem.colorScheme.primary,
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                for (item in items) {
                    val isSelected = item.theme == tempTheme
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .selectable(
                                selected = isSelected,
                                onClick = { tempTheme = item.theme },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = padding.medium),
                        horizontalArrangement = Arrangement.spacedBy(padding.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null // null recommended for accessibility with screen readers
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(padding.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .testTag(item.icon.toString())
                            )
                            Text(
                                text = stringResource(id = item.titleId),
                                style = FontPickerDesignSystem.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            StyledDialogButton(
                text = stringResource(id = R.string.cancel),
                onClick = onDismiss,
                textColor = FontPickerDesignSystem.colorScheme.primary,
                borderColorPressed = FontPickerDesignSystem.colorScheme.primary
            )
        },
        confirmButton = {
            StyledDialogButton(
                text = stringResource(id = R.string.save),
                onClick = {
                    onConfirm(tempTheme)
                    onDismiss()
                },
                textColor = FontPickerDesignSystem.colorScheme.primary,
                borderColorPressed = FontPickerDesignSystem.colorScheme.primary,
                enabled = tempTheme != selectedTheme
            )
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .width((minOf(LocalConfiguration.current.screenWidthDp, 500) * 0.9).dp)
            .padding(padding.medium)
    )
}

sealed class ThemePickerItem(
    val theme: FontPickerThemePreference,
    val icon: ImageVector,
    @StringRes val titleId: Int
) {
    data object FollowSystem : ThemePickerItem(
        theme = FontPickerThemePreference.FollowSystem,
        icon = FontPickerIcons.Outlined.FollowSystem,
        titleId = R.string.system_default
    )

    data object Light : ThemePickerItem(
        theme = FontPickerThemePreference.Light,
        icon = FontPickerIcons.Outlined.LightMode,
        titleId = R.string.light_theme
    )

    data object Dark : ThemePickerItem(
        theme = FontPickerThemePreference.Dark,
        icon = FontPickerIcons.Outlined.DarkMode,
        titleId = R.string.dark_theme
    )
}

@PreviewLightDark
@Composable
private fun ThemePickerDialogPreview() {
    FontPickerTheme {
        ThemePickerDialog(
            selectedTheme = FontPickerThemePreference.Dark,
            onDismiss = { },
            onConfirm = { }
        )
    }
}
