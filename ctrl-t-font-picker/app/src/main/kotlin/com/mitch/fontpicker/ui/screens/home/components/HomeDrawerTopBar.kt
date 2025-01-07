package com.mitch.fontpicker.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.R
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme

private val ICON_PADDING_HORIZONTAL = 6.dp
private val ICON_END_PADDING = 8.dp
private val TEXT_HORIZONTAL_PADDING = 18.dp
private val TEXT_VERTICAL_OFFSET = 1.dp


@Composable
fun HomeDrawerTopBar(
    rowAlpha: Float,
    onToggleDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (rowAlpha > 0f) {
        Row(
            modifier = Modifier
                .padding(horizontal = ICON_PADDING_HORIZONTAL)
                .background(
                    FontPickerDesignSystem.colorScheme.surface.copy(alpha = rowAlpha)
                )
                .alpha(rowAlpha),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drawer icon
            HomeDrawerIcon(
                onToggleDrawer = onToggleDrawer,
                modifier = modifier.padding(end = ICON_END_PADDING)
            )

            Spacer(modifier = modifier.weight(1f))

            // Drawer top bar text
            Text(
                text = stringResource(R.string.capture_a_font),
                style = FontPickerDesignSystem.typography.titleLarge.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = FontPickerDesignSystem.colorScheme.tertiary,
                modifier = modifier
                    .padding(horizontal = TEXT_HORIZONTAL_PADDING)
                    .offset(y = TEXT_VERTICAL_OFFSET)
            )
        }
    }
}


@PreviewLightDark
@Composable
private fun HomeDrawerTopBarPreview() {
    FontPickerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FontPickerDesignSystem.colorScheme.background)
        ) {
            HomeDrawerTopBar(
                rowAlpha = 1f,
                onToggleDrawer = { /* Stub for preview */ }
            )
        }
    }
}