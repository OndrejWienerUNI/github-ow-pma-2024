package com.mitch.fontpicker.ui.designsystem.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.R
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme

private val ICON_SIZE = 64.dp
private val ICON_TEXT_SPACING = 6.dp
private val ICON_TEXT_OFFSET_Y = (-2).dp
private val ROW_END_MARGIN = 8.dp

@Composable
fun AppBrandingRow(
    iconResId: Int,
    titleResId: Int,
    modifier: Modifier = Modifier,
    topMargin: Dp = 0.dp,
    bottomMargin: Dp = 10.dp
) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = modifier
            .padding(
                top = topMargin,
                bottom = bottomMargin,
                end = ROW_END_MARGIN
            )
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = stringResource(id = titleResId),
            tint = Color.Unspecified,
            modifier = Modifier.size(ICON_SIZE)
        )
        Text(
            text = stringResource(id = titleResId),
            color = FontPickerDesignSystem.colorScheme.primary,
            style = FontPickerDesignSystem.typography.displayMedium.copy(
                shadow = Shadow(
                    color = FontPickerDesignSystem.colorScheme.onBackground.copy(alpha = 0.15f),
                    offset = Offset(2f, 2f),
                    blurRadius = 0f
                )
            ),
            modifier = Modifier
                .padding(start = ICON_TEXT_SPACING)
                .offset(y = ICON_TEXT_OFFSET_Y)
        )
    }
}


@PreviewLightDark
@Composable
private fun AppBrandingRowPreview() {
    FontPickerTheme {
        Box(
            Modifier.background(FontPickerDesignSystem.colorScheme.background)
        ) {
            AppBrandingRow(
                iconResId = R.drawable.app_icon_cropped,
                titleResId = R.string.in_app_title,
                topMargin = 0.dp,
                bottomMargin = 10.dp
            )
        }
    }
}
