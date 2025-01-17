package com.mitch.fontpicker.ui.screens.home.components.drawers

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.R
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme

private val ICON_PADDING_HORIZONTAL = 6.dp
private val TEXT_HORIZONTAL_PADDING = 18.dp
private val TEXT_VERTICAL_OFFSET = 0.dp
private val PADDING_VERTICAL = 2.dp
private val GRADIENT_HEIGHT = 8.dp

@Composable
fun HomeDrawerTopBar(
    rowAlpha: Float,
    onToggleDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Smoothly animate the alpha value
    val animatedAlpha by animateFloatAsState(
        targetValue = rowAlpha,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 300, // Duration of the animation
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ), label = ""
    )

    if (animatedAlpha > 0f) {
        Column {
            Row(
                modifier = Modifier
                    .background(
                        FontPickerDesignSystem.colorScheme.background.copy(alpha = animatedAlpha)
                    )
                    .alpha(animatedAlpha),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drawer icon
                HomeDrawerIcon(
                    onToggleDrawer = onToggleDrawer,
                    modifier = modifier
                        .padding(horizontal = ICON_PADDING_HORIZONTAL, vertical = PADDING_VERTICAL)
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
                        .padding(horizontal = TEXT_HORIZONTAL_PADDING, vertical = PADDING_VERTICAL)
                        .offset(y = TEXT_VERTICAL_OFFSET)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GRADIENT_HEIGHT)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                FontPickerDesignSystem.colorScheme.background.copy(alpha = animatedAlpha),
                                FontPickerDesignSystem.colorScheme.background.copy(alpha = 0f)
                            )
                        )
                    )
            )
        }
    }
}

@Preview
@Composable
private fun HomeDrawerTopBarPreview() {
    FontPickerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
        ) {
            HomeDrawerTopBar(
                rowAlpha = 1f,
                onToggleDrawer = { /* Stub for preview */ }
            )
        }
    }
}
