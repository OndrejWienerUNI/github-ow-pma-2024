package com.mitch.fontpicker.ui.screens.home.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.R
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import kotlinx.coroutines.launch

// Constants for magic values
private val ICON_BUTTON_SIZE = 36.dp
private val ICON_SIZE = 32.dp

@Composable
fun HomeDrawerIcon(
    onToggleDrawer: suspend () -> Unit, // Pass a suspending function to control the drawer
    modifier: Modifier = Modifier
        .size(ICON_BUTTON_SIZE)
) {
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            scope.launch {
                onToggleDrawer()
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = stringResource(R.string.menu),
            modifier = Modifier.size(ICON_SIZE),
            tint = FontPickerDesignSystem.colorScheme.tertiary
        )
    }
}
