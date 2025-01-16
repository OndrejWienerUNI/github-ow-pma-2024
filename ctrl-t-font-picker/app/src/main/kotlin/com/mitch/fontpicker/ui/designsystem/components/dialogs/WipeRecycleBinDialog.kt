package com.mitch.fontpicker.ui.designsystem.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.mitch.fontpicker.R
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme

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
                tint = FontPickerDesignSystem.extendedColorScheme.redAccent
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
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    color = FontPickerDesignSystem.extendedColorScheme.redAccent
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(id = R.string.confirm),
                    color = FontPickerDesignSystem.extendedColorScheme.redAccent
                )
            }
        },
        containerColor = FontPickerDesignSystem.colorScheme.surface
    )
}

@Preview
@Composable
private fun WipeRecycleBinDialogPreview() {
    FontPickerTheme {
        WipeRecycleBinDialog(
            onDismiss = { },
            onConfirm = { }
        )
    }
}
