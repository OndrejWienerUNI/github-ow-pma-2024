package com.mitch.fontpicker.ui.screens.camera.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.R
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme

private val BUTTON_SIZE = 52.dp
private val SHOOT_BUTTON_SIZE = 72.dp
private val ROW_PADDING_HORIZONTAL = 30.dp
private val ROW_PADDING_VERTICAL = 16.dp

@Composable
fun CameraActionRow(
    onShoot: () -> Unit,
    onGallery: () -> Unit,
    onFlip: () -> Unit,
    galleryThumbnail: Painter? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ROW_PADDING_HORIZONTAL, vertical = ROW_PADDING_VERTICAL),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
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
                if (galleryThumbnail != null) {
                    Icon(
                        painter = galleryThumbnail,
                        contentDescription = "Gallery Thumbnail",
                        modifier = Modifier.fillMaxSize(), // Icon fills the parent Box
                        tint = FontPickerDesignSystem.colorScheme.onSurface,
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_gallery_placeholder),
                        contentDescription = "Gallery Placeholder",
                        tint = Color.Unspecified
                    )
                }
            }
        }

        // Shoot Button
        IconButton(
            onClick = onShoot,
            modifier = Modifier.size(SHOOT_BUTTON_SIZE) // Size of the entire button
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(FontPickerDesignSystem.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shoot_button),
                    contentDescription = "Shoot Button",
                    modifier = Modifier.fillMaxSize(), // Icon fills the parent Box
                    tint = FontPickerDesignSystem.extendedColorScheme.icOnBackground
                )
            }
        }

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
                onFlip = { /* No-op for preview */ },
                galleryThumbnail = null
            )
        }
    }
}
