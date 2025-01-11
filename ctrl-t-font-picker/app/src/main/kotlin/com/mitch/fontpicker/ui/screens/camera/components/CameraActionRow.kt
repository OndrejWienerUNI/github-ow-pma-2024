package com.mitch.fontpicker.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.R
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme
import kotlinx.coroutines.launch

private val BUTTON_SIZE = 52.dp
private val SHOOT_BUTTON_SIZE = 72.dp
private val ROW_PADDING_HORIZONTAL = 30.dp
private val ROW_PADDING_VERTICAL = 16.dp

@Composable
fun CameraActionRow(
    onShoot: () -> Unit,
    onGallery: () -> Unit,
    onFlip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ROW_PADDING_HORIZONTAL, vertical = ROW_PADDING_VERTICAL),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Gallery Button
        IconButton(
            onClick = onGallery,
            modifier = Modifier.size(BUTTON_SIZE) // Size of the entire button
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(FontPickerDesignSystem.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gallery),
                    contentDescription = "Gallery Placeholder",
                    tint = Color.Unspecified
                )
            }
        }

        // Shoot Button with Color Change
        ShootButton(onShoot = onShoot)

        // Flip Button
        IconButton(
            onClick = onFlip,
            modifier = Modifier.size(BUTTON_SIZE) // Size of the entire button
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera_flip),
                contentDescription = "Flip Camera",
                tint = Color.Unspecified
            )
        }
    }
}


@Composable
private fun ShootButton(onShoot: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Determine the icon tint based on the pressed state
    val iconTint = if (isPressed) {
        FontPickerDesignSystem.extendedColorScheme.icOnBackgroundPressed
    } else {
        FontPickerDesignSystem.extendedColorScheme.icOnBackground
    }

    IconButton(
        onClick = {
            if (!isPressed) { // Prevent overlapping presses
                isPressed = true
                onShoot()

                // Reset the tint after a delay
                coroutineScope.launch {
                    kotlinx.coroutines.delay(200) // Hold pressed tint for 200ms
                    isPressed = false
                }
            }
        },
        modifier = Modifier.size(SHOOT_BUTTON_SIZE)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_shoot_button),
            contentDescription = "Shoot Button",
            tint = iconTint, // Tint changes dynamically based on state
            modifier = Modifier.fillMaxSize()
        )
    }
}


@PreviewLightDark
@Composable
private fun CameraActionRowPreview() {
    FontPickerTheme {
        Box(
            Modifier.background(FontPickerDesignSystem.colorScheme.background)
        ) {
            CameraActionRow(
                onShoot = { /* No-op for preview */ },
                onGallery = { /* No-op for preview */ },
                onFlip = { /* No-op for preview */ }
            )
        }
    }
}
