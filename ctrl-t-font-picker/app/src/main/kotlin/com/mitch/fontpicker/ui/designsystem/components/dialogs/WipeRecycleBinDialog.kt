package com.mitch.fontpicker.ui.designsystem.components.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.mitch.fontpicker.R
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding

@Composable
fun WipeRecycleBinDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = FontPickerIcons.Outlined.Trash,
                contentDescription = null,
                tint = FontPickerDesignSystem.extendedColorScheme.redAccent,
                modifier = Modifier.padding(
                    top = padding.small,
                    bottom = padding.zero,
                    start = padding.zero,
                    end = padding.zero,
                )
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.wipe_recycle_bin_title),
                color = FontPickerDesignSystem.extendedColorScheme.redAccent,
                style = FontPickerDesignSystem.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.wipe_recycle_bin_confirmation),
                color = FontPickerDesignSystem.colorScheme.onSurface,
                style = FontPickerDesignSystem.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            StyledDialogButton(
                text = stringResource(id = R.string.confirm),
                onClick = onConfirm,
                textColor = FontPickerDesignSystem.extendedColorScheme.redAccent,
                borderColorPressed = FontPickerDesignSystem.extendedColorScheme.redAccent
            )
        },
        dismissButton = {
            StyledDialogButton(
                text = stringResource(id = R.string.cancel),
                onClick = onDismiss,
                textColor = FontPickerDesignSystem.extendedColorScheme.redAccent,
                borderColorPressed = FontPickerDesignSystem.extendedColorScheme.redAccent
            )
        },
        containerColor = FontPickerDesignSystem.colorScheme.surface
    )
}

@PreviewLightDark
@Composable
private fun WipeRecycleBinDialogPreview() {
    FontPickerTheme {
        WipeRecycleBinDialog(
            onDismiss = { },
            onConfirm = { }
        )
    }
}
