package com.mitch.fontpicker.ui.screens.recycle.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme

private val BUTTON_PADDING = 40.dp
private val BUTTON_WIDTH = 100.dp
private val BUTTON_ELEVATION = 4.dp
private val ICON_PADDING = 0.dp
private val ICON_SIZE = 40.dp

@Composable
fun WipeRecycleBinButton(
    onClick: () -> Unit,
    recycleBinEmpty: Boolean,
    modifier: Modifier = Modifier
) {

    val buttonColors = if (recycleBinEmpty) {
        ButtonDefaults.buttonColors(
            containerColor = FontPickerDesignSystem.colorScheme.tertiary,
            contentColor = FontPickerDesignSystem.colorScheme.onTertiary
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = FontPickerDesignSystem.extendedColorScheme.redAccent,
            contentColor = FontPickerDesignSystem.extendedColorScheme.onRedAccent
        )
    }

    Button(
        onClick = onClick,
        enabled = !recycleBinEmpty,
        colors = buttonColors,
        shape = RoundedCornerShape(50),
        elevation = ButtonDefaults.elevatedButtonElevation(BUTTON_ELEVATION),
        modifier = modifier
            .padding(BUTTON_PADDING)
            .size(BUTTON_WIDTH, BUTTON_WIDTH / 2f)
    ) {
        Icon(
            imageVector = FontPickerIcons.Outlined.Trash,
            contentDescription = "Wipe Recycle Bin",
            modifier = Modifier
                .size(ICON_SIZE)
                .padding(horizontal = ICON_PADDING, vertical = ICON_PADDING)
                .align(Alignment.CenterVertically)
        )
    }
}

@Preview
@Composable
fun WipeRecycleBinButtonPreview() {
    FontPickerTheme {
        Box {
            // Preview for a non-empty recycle bin
            WipeRecycleBinButton(
                onClick = {},
                recycleBinEmpty = false
            )
        }
    }

    FontPickerTheme {
        Box {
            // Preview for an empty recycle bin
            WipeRecycleBinButton(
                onClick = {},
                recycleBinEmpty = true
            )
        }
    }
}
