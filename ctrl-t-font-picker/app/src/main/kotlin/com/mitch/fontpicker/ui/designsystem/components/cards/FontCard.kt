package com.mitch.fontpicker.ui.designsystem.components.cards

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mitch.fontpicker.R
import com.mitch.fontpicker.data.api.FontDownloaded
import com.mitch.fontpicker.data.images.BitmapToolkit
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding
import timber.log.Timber

private val BORDER_THICKNESS = 1.dp
private val HEART_ICON_BUTTON_SIZE = 40.dp
private val HEART_ICON_SIZE = 28.dp
private val IMAGE_HEIGHT = 40.dp
private val OVERLAY_CIRCLE_SIZE = 36.dp
private val OVERLAY_ICON_SIZE = 22.dp
private val TEXT_Y_OFFSET = (-0.5).dp

@Composable
fun FontCard(
    font: FontDownloaded,
    inSelectionDialog: Boolean,
    onWebpageClick: () -> Unit,
    isThemeDark: Boolean = isSystemInDarkTheme()
) {
    // Use font.isLiked.value directly
    val isLiked = font.isLiked.value

    val processedImages = font.bitmaps.map { bitmap ->
        if (isThemeDark) BitmapToolkit.invertImage(bitmap) else bitmap
    }

    val heartIcon: ImageVector = if (isLiked) FontPickerIcons.Filled.Heart else FontPickerIcons.Outlined.Heart
    val heartColor: Color = if (isLiked)
        FontPickerDesignSystem.colorScheme.primary
    else
        FontPickerDesignSystem.extendedColorScheme.icOnBackground

    val borderColor: Color = if (isLiked)
        FontPickerDesignSystem.colorScheme.primary
    else
        FontPickerDesignSystem.extendedColorScheme.borders

    Card(
        modifier = Modifier
            .padding(padding.zero)
            .fillMaxWidth()
            .border(BorderStroke(BORDER_THICKNESS, borderColor), shape = CardDefaults.shape)
            .background(
                color = FontPickerDesignSystem.colorScheme.surface,
                shape = CardDefaults.shape
            )
            .clickable(enabled = inSelectionDialog) {
                font.isLiked.value = !isLiked // Update the state directly
                Timber.d("FontCard: Font '${font.title}' like state changed to ${font.isLiked.value}")
            },
        colors = CardDefaults.cardColors(
            containerColor = FontPickerDesignSystem.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = padding.extraSmall, end = padding.extraSmall),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = font.title,
                        style = FontPickerDesignSystem.typography.titleMedium,
                        color = FontPickerDesignSystem.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(padding.medium)
                            .offset(y = TEXT_Y_OFFSET)
                            .weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (!inSelectionDialog) {
                                font.isLiked.value = !font.isLiked.value
                                Timber.d("FontCard: Font '${font.title}' isLiked " +
                                        "state changed to $isLiked")
                            }
                        },
                        modifier = Modifier
                            .padding(padding.small)
                            .size(HEART_ICON_BUTTON_SIZE)
                    ) {
                        Icon(
                            modifier = Modifier.size(HEART_ICON_SIZE),
                            imageVector = heartIcon,
                            contentDescription = null,
                            tint = heartColor
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = padding.small,
                            end = padding.small,
                            bottom = padding.small,
                            top = padding.zero
                        )
                        .clip(CardDefaults.shape)
                        .background(
                            color = if (isThemeDark) Color.Black else Color.White,
                            shape = CardDefaults.shape
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    processedImages.forEach { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Font Preview Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IMAGE_HEIGHT)
                                .clip(CardDefaults.shape),
                            alignment = Alignment.Center
                        )
                    }
                }
            }

            // Symbol overlay
            Box(
                modifier = Modifier
                    .clickable { onWebpageClick() }
                    .align(Alignment.BottomEnd)
                    .padding(padding.medium)
                    .size(OVERLAY_CIRCLE_SIZE)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        clip = true
                    )
                    .background(
                        color = FontPickerDesignSystem.colorScheme.tertiary,
                        shape = CircleShape
                    )
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.what_font_is_symbol),
                    contentDescription = "WhatFontIs Font Recognition Provider Icon",
                    modifier = Modifier.size(OVERLAY_ICON_SIZE)
                )
            }
        }
    }
}


@Preview
@Composable
fun FontCardPreviewLight() {
    FontPickerTheme(isThemeDark = false) {
        val sampleBitmap = Bitmap.createBitmap(500, 120, Bitmap.Config.ARGB_8888)
        val fontDownloaded = FontDownloaded(
            title = "Example Font (Light Theme, disliked)",
            url = "https://example.com/font",
            imageUrls = listOf("https://example.com/font-image-1"),
            bitmaps = listOf(sampleBitmap, sampleBitmap, sampleBitmap)
        )

        FontCard(
            font = fontDownloaded,
            inSelectionDialog = true,
            onWebpageClick = { /* Webpage click action */ },
            isThemeDark = false
        )
    }
}

@Preview
@Composable
fun FontCardPreviewDark() {
    FontPickerTheme(isThemeDark = true) {
        val sampleBitmap = Bitmap.createBitmap(500, 120, Bitmap.Config.ARGB_8888)
        val fontDownloaded = FontDownloaded(
            title = "Example Font (Dark Theme, liked)",
            url = "https://example.com/font",
            imageUrls = listOf("https://example.com/font-image-1"),
            bitmaps = listOf(sampleBitmap, sampleBitmap, sampleBitmap)
        )

        FontCard(
            font = fontDownloaded,
            inSelectionDialog = false,
            onWebpageClick = { /* Webpage click action */ },
            isThemeDark = true
        )
    }
}
