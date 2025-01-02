package com.mitch.fontpicker.ui.designsystem.components.snackbars

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding

@Composable
fun FontPickerSnackbar(
    colors: FontPickerSnackbarColors,
    message: String,
    icon: ImageVector?,
    action: SnackbarAction?,
    modifier: Modifier = Modifier,
    dismissAction: @Composable (() -> Unit)? = null,
    actionOnNewLine: Boolean = false,
    shape: Shape = SnackbarDefaults.shape
) {
    Snackbar(
        modifier = modifier,
        action = action?.let {
            {
                TextButton(
                    onClick = action.onPerformAction,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colors.actionColor
                    )
                ) {
                    Text(text = action.label)
                }
            }
        },
        dismissAction = dismissAction,
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = colors.containerColor,
        contentColor = colors.messageColor,
        actionContentColor = colors.actionColor
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(padding.small))
            }
            Text(text = message)
        }
    }
}

@PreviewLightDark
@Composable
private fun FontPickerSnackbarDefaultPreview() {
    FontPickerTheme {
        FontPickerSnackbar(
            message = "Default",
            icon = null,
            action = SnackbarAction(label = "This is my action", onPerformAction = { }),
            colors = FontPickerSnackbarDefaults.defaultSnackbarColors()
        )
    }
}

@Preview
@Composable
private fun FontPickerSnackbarDefaultIndefinitePreview() {
    FontPickerTheme {
        FontPickerSnackbar(
            message = "Default",
            icon = null,
            action = SnackbarAction(label = "This is my action", onPerformAction = { }),
            colors = FontPickerSnackbarDefaults.defaultSnackbarColors(),
            dismissAction = {
                IconButton(
                    onClick = { },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = FontPickerDesignSystem.colorScheme.inverseOnSurface
                    )
                ) {
                    Icon(
                        imageVector = FontPickerIcons.Outlined.Close,
                        contentDescription = "Dismiss snackbar"
                    )
                }
            }
        )
    }
}

@Preview
@Composable
private fun FontPickerSnackbarSuccessPreview() {
    FontPickerTheme {
        FontPickerSnackbar(
            message = "Success",
            icon = FontPickerIcons.Filled.Success,
            action = null,
            colors = FontPickerSnackbarDefaults.successSnackbarColors()
        )
    }
}

@Preview
@Composable
private fun FontPickerSnackbarWarningPreview() {
    FontPickerTheme {
        FontPickerSnackbar(
            message = "Warning",
            icon = FontPickerIcons.Filled.Warning,
            action = null,
            colors = FontPickerSnackbarDefaults.warningSnackbarColors()
        )
    }
}

@Preview
@Composable
private fun FontPickerSnackbarErrorPreview() {
    FontPickerTheme {
        FontPickerSnackbar(
            message = "Error",
            icon = FontPickerIcons.Filled.Error,
            action = null,
            colors = FontPickerSnackbarDefaults.errorSnackbarColors()
        )
    }
}
