package com.mitch.fontpicker.ui.designsystem.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.AlertDialogDefaults.textContentColor
import androidx.compose.material3.AlertDialogDefaults.titleContentColor
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosableDialog(
    onDismiss: () -> Unit,
    title: @Composable () -> Unit,
    body: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties()
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        properties = properties
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = FontPickerDesignSystem.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(padding.medium)
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = padding.small)
                        .align(Alignment.End)
                ) {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip {
                                Text("Close dialog")
                            }
                        },
                        state = rememberTooltipState()
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = EvaIcons.Outline.Close,
                                contentDescription = "Close dialog"
                            )
                        }
                    }
                }

                icon?.let {
                    Box(
                        modifier = Modifier
                            .padding(bottom = padding.medium)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        icon()
                    }
                }

                CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                    val textStyle = FontPickerDesignSystem.typography.headlineSmall
                    ProvideTextStyle(textStyle) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = padding.medium)
                                .align(
                                    if (icon == null) {
                                        Alignment.Start
                                    } else {
                                        Alignment.CenterHorizontally
                                    }
                                )
                        ) {
                            title()
                        }
                    }
                }

                CompositionLocalProvider(LocalContentColor provides textContentColor) {
                    val textStyle = FontPickerDesignSystem.typography.bodyMedium
                    ProvideTextStyle(textStyle) {
                        Row(
                            modifier = Modifier
                                .weight(weight = 1f, fill = false)
                                .padding(bottom = 24.dp)
                                .align(Alignment.Start)
                        ) {
                            body()
                        }
                    }
                }

                if (dismissButton != null && confirmButton != null) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = padding.medium,
                                bottom = padding.zero,
                                start = padding.zero,
                                end = padding.zero
                            )
                    ) {
                        var dismissButtonWidth by remember { mutableIntStateOf(0) }
                        var confirmButtonWidth by remember { mutableIntStateOf(0) }

                        val maxWidthDp = maxWidth

                        val totalButtonWidth = with(LocalDensity.current) {
                            dismissButtonWidth.toDp() + confirmButtonWidth.toDp()
                        }

                        val isStacked = totalButtonWidth > maxWidthDp - 20.dp

                        if (!isStacked) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .onSizeChanged { size ->
                                            dismissButtonWidth = size.width
                                        }

                                ) {
                                    dismissButton()
                                }
                                Box(
                                    modifier = Modifier
                                        .onSizeChanged { size ->
                                            confirmButtonWidth = size.width
                                        }
                                ) {
                                    confirmButton()
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .onSizeChanged { size ->
                                            confirmButtonWidth = size.width
                                        }
                                ) {
                                    confirmButton()
                                }
                                Box(
                                    modifier = Modifier
                                        .onSizeChanged { size ->
                                            dismissButtonWidth = size.width
                                        }
                                ) {
                                    dismissButton()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun BasicClosableDialogPreview() {
    ClosableDialog(
        onDismiss = { },
        title = {
            Text(text = "Basic dialog title")
        },
        body = {
            Text(text = "A dialog is a type of modal window that appears")
        },
        confirmButton = {
            TextButton(onClick = { }) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { }) {
                Text(text = "Dismiss")
            }
        }
    )
}

@Preview
@Composable
private fun ClosableDialogWithHeroIconPreview() {
    ClosableDialog(
        onDismiss = { },
        icon = {
            Icon(
                imageVector = FontPickerIcons.Outlined.Translate,
                contentDescription = null
            )
        },
        title = {
            Text(
                text = "Basic dialog title",
                textAlign = TextAlign.Center
            )
        },
        body = {
            Text(
                text = "A dialog is a type of modal window that appears",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = { }) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { }) {
                Text(text = "Dismiss")
            }
        }
    )
}

@Preview
@Composable
private fun ClosableDialogWithHeroIconAndStyledButtonsPreview() {
    ClosableDialog(
        onDismiss = { },
        icon = {
            Icon(
                imageVector = FontPickerIcons.Outlined.Translate,
                contentDescription = null
            )
        },
        title = {
            Text(
                text = "Basic dialog title",
                textAlign = TextAlign.Center
            )
        },
        body = {
            Text(
                text = "A dialog is a type of modal window that appears",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            StyledDialogButton(
                text = "Confirm",
                onClick = { /* Handle confirm click */ },
                textColor = FontPickerDesignSystem.colorScheme.primary,
                borderColorPressed = FontPickerDesignSystem.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            StyledDialogButton(
                text = "Dismiss",
                onClick = { /* Handle dismiss click */ },
                textColor = FontPickerDesignSystem.colorScheme.primary,
                borderColorPressed = FontPickerDesignSystem.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

