package com.mitch.fontpicker.ui.designsystem.components.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.wear.compose.material.ContentAlpha

@Composable
fun StyledDialogButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    textColor: Color,
    borderColorPressed: Color,
    backgroundColor: Color = Color.Transparent,
    modifier: Modifier = Modifier.fillMaxWidth().background(backgroundColor)
) {
    val isPressed = remember { mutableStateOf(false) }
    val textColorDisabled = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled)
    val borderColorIdle = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled)

    TextButton(
        modifier = modifier,
        onClick = {
            if (enabled) {
                isPressed.value = true
                onClick()
                isPressed.value = false
            }
        },
        enabled = enabled,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) {
                if (isPressed.value) borderColorPressed else borderColorIdle
            } else {
                borderColorIdle
            }
        ),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (enabled) textColor else textColorDisabled
        )
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else textColorDisabled
        )
    }
}
