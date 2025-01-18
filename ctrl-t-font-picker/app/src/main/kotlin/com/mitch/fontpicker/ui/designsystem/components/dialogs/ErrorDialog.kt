package com.mitch.fontpicker.ui.designsystem.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding

// Define all dp values as private constants in caps lock
private const val DIALOG_WIDTH_FACTOR = 0.9f
private val MAX_DIALOG_WIDTH_DP = 500.dp

private val TITLE_OFFSET_Y = (-2.5).dp
private val ICON_BUTTON_SIZE = 28.dp
private val ICON_OFFSET_Y = (-4).dp
private val COLUMN_EXTRA_PADDING_START = 4.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.width(
            (minOf(LocalConfiguration.current.screenWidthDp,
                MAX_DIALOG_WIDTH_DP.value.toInt()) * DIALOG_WIDTH_FACTOR).dp
        ).padding(padding.small)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(padding.small),
            shape = FontPickerDesignSystem.shapes.large,
            color = FontPickerDesignSystem.colorScheme.error,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(
                modifier = Modifier.padding(
                    top = padding.medium,
                    bottom = padding.medium,
                    start = padding.medium + COLUMN_EXTRA_PADDING_START,
                    end = padding.medium
                ),
            ) {
                // Title and Close Button in the Same Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title
                    CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.titleContentColor) {
                        Text(
                            text = "Error",
                            style = FontPickerDesignSystem.typography.headlineSmall,
                            color = FontPickerDesignSystem.colorScheme.onError,
                            modifier = Modifier
                                .weight(1f)
                                .offset(y = TITLE_OFFSET_Y)
                        )
                    }

                    // Close Button with Tooltip
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip {
                                Text("Close dialog")
                            }
                        },
                        state = rememberTooltipState()
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(ICON_BUTTON_SIZE)
                                .offset(y = ICON_OFFSET_Y)
                        ) {
                            Icon(
                                imageVector = FontPickerIcons.Outlined.Close,
                                contentDescription = "Close dialog",
                                tint = FontPickerDesignSystem.colorScheme.onError
                            )
                        }
                    }
                }

                // Body Content
                CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.textContentColor) {
                    Text(
                        text = errorMessage,
                        style = FontPickerDesignSystem.typography.bodyMedium,
                        color = FontPickerDesignSystem.colorScheme.onError,
                        modifier = Modifier
                            .padding(vertical = padding.small)
                            .fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorDialogPreviewLight() {
    FontPickerTheme(isThemeDark = false) {
        Column(
            modifier = Modifier.background(FontPickerDesignSystem.colorScheme.background)
        ) {
            ErrorDialog(
                onDismiss = { /* Handle dismiss */ },
                errorMessage = "An unexpected error occurred. Please try again later."
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorDialogPreviewDark() {
    FontPickerTheme(isThemeDark = true) {
        Column(
            modifier = Modifier.background(FontPickerDesignSystem.colorScheme.background)
        ) {
            ErrorDialog(
                onDismiss = { /* Handle dismiss */ },
                errorMessage = "An unexpected error occurred. Please try again later."
            )
        }
    }
}
