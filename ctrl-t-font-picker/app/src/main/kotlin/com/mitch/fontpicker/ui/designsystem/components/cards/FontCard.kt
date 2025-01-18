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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.launch
import timber.log.Timber


private const val CLICK_DEBOUNCE_TIMEOUT = 500L

private val BORDER_THICKNESS = 1.dp
private val HEART_ICON_BUTTON_SIZE = 38.dp
private val HEART_ICON_SIZE = 30.dp
private val IMAGES_COLUMN_HEIGHT = 120.dp // Fixed height for images column
private val OVERLAY_CIRCLE_SIZE = 36.dp
private val OVERLAY_ICON_SIZE = 32.dp


@Composable
fun FontCard(
    font: FontDownloaded,
    inSelectionDialog: Boolean,
    onLikeClick: (FontDownloaded) -> Unit,
    onWebpageClick: () -> Unit,
    isThemeDark: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    // Safeguard for rapid-fire clicks
    val lastClickTime = remember { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()
    val isProcessing = remember { mutableStateOf(false) }
    val isLiked = font.isLiked.value

    fun handleLikeClick() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime.longValue > CLICK_DEBOUNCE_TIMEOUT && !isProcessing.value) {
            lastClickTime.longValue = currentTime
            isProcessing.value = true

            // Update UI state
            font.isLiked.value = !isLiked
            Timber.d("FontCard: Font '${font.title}' like state changed to ${font.isLiked.value}")
            onLikeClick(font)

            // Reset processing flag after debounce timeout
            coroutineScope.launch {
                kotlinx.coroutines.delay(CLICK_DEBOUNCE_TIMEOUT)
                isProcessing.value = false
            }
        } else {
            Timber.d("FontCard: Rapid click ignored for like button")
        }
    }


    fun handleWebpageClick() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime.longValue > CLICK_DEBOUNCE_TIMEOUT) {
            lastClickTime.longValue = currentTime
            Timber.d("FontCard: Navigating to webpage for font '${font.title}'")
            onWebpageClick()
        } else {
            Timber.d("FontCard: Rapid click ignored for webpage button")
        }
    }

    // Process images based on theme
    val processedImages = remember(isThemeDark) {
        font.bitmaps.map { bitmap ->
            if (isThemeDark) {
                Timber.d("Inverting image due to dark theme")
                BitmapToolkit.invertImage(bitmap)
            } else {
                bitmap
            }
        }
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
        modifier = modifier
            .padding(padding.zero)
            .fillMaxWidth()
            .border(BorderStroke(BORDER_THICKNESS, borderColor), shape = CardDefaults.shape)
            .background(
                color = FontPickerDesignSystem.extendedColorScheme.cardSurface,
                shape = CardDefaults.shape
            )
            .clip(CardDefaults.shape)
            // Must come after the clip
            .clickable(enabled = inSelectionDialog) { handleLikeClick() },
        colors = CardDefaults.cardColors(
            containerColor = FontPickerDesignSystem.extendedColorScheme.cardSurface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding.small, vertical = padding.small),
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
                            .padding(horizontal = padding.small)
                            .weight(1f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IMAGES_COLUMN_HEIGHT) // Fixed height for the images column
                        .padding(
                            start = padding.small,
                            end = padding.small,
                            bottom = padding.small,
                            top = padding.zero
                        )
                        .background(
                            color = if (isThemeDark) Color.Black else Color.White,
                            shape = CardDefaults.shape
                        )
                        .clip(CardDefaults.shape),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Distribute available height equally among images
                    processedImages.forEach { bitmap ->
                        Row(
                            modifier = Modifier
                                .wrapContentWidth()
                                .weight(1f)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Font Preview Image",
                                modifier = Modifier
                                    .fillMaxHeight(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = { handleLikeClick() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = padding.medium  - 2.dp, vertical = padding.extraSmall)
                    .size(HEART_ICON_BUTTON_SIZE)
                    .zIndex(1f)
            ) {
                Icon(
                    modifier = Modifier.size(HEART_ICON_SIZE),
                    imageVector = heartIcon,
                    contentDescription = null,
                    tint = heartColor
                )
            }

            IconButton(
                onClick = { if (!inSelectionDialog) handleWebpageClick() else handleLikeClick() },
                modifier = Modifier
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
                    .clip(CircleShape)
                    .zIndex(1f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.what_font_is_symbol),
                    tint = Color.Unspecified,
                    contentDescription = "WhatFontIs Font Recognition Provider Icon",
                    modifier = Modifier
                        .size(OVERLAY_ICON_SIZE)
                        .background(
                            color = FontPickerDesignSystem.colorScheme.tertiary,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Preview
@Composable
fun FontCardPreviewLight() {
    FontPickerTheme(isThemeDark = false) {
        val sampleBitmap1 = Bitmap.createBitmap(800, 120, Bitmap.Config.ARGB_8888)
            .apply { eraseColor(android.graphics.Color.RED) }
        val sampleBitmap2 = Bitmap.createBitmap(500, 120, Bitmap.Config.ARGB_8888)
            .apply { eraseColor(android.graphics.Color.GREEN) }
        val sampleBitmap3 = Bitmap.createBitmap(1200, 120, Bitmap.Config.ARGB_8888)
            .apply { eraseColor(android.graphics.Color.BLUE) }

        val fontDownloaded = FontDownloaded(
            title = "Example Font (Light Theme, disliked)",
            url = "https://example.com/font",
            imageUrls = listOf("https://example.com/font-image-1"),
            bitmaps = listOf(sampleBitmap1, sampleBitmap2, sampleBitmap3)
        )

        FontCard(
            font = fontDownloaded,
            inSelectionDialog = true,
            onLikeClick = { /* Like click action */ },
            onWebpageClick = { /* Webpage click action */ },
            isThemeDark = false
        )
    }
}

@Preview
@Composable
fun FontCardPreviewDark() {
    FontPickerTheme(isThemeDark = true) {
        val sampleBitmap1 = Bitmap.createBitmap(800, 120, Bitmap.Config.ARGB_8888)
            .apply { eraseColor(android.graphics.Color.RED) }
//        val sampleBitmap2 = Bitmap.createBitmap(500, 120, Bitmap.Config.ARGB_8888)
//            .apply { eraseColor(android.graphics.Color.GREEN) }
        val sampleBitmap3 = Bitmap.createBitmap(1200, 120, Bitmap.Config.ARGB_8888)
            .apply { eraseColor(android.graphics.Color.BLUE) }

        val fontDownloaded = FontDownloaded(
            title = "Example Font (Dark Theme, liked)",
            url = "https://example.com/font",
            imageUrls = listOf("https://example.com/font-image-1"),
            bitmaps = listOf(sampleBitmap1, sampleBitmap3)
        )

        FontCard(
            font = fontDownloaded,
            inSelectionDialog = true,
            onLikeClick = { /* Like click action */ },
            onWebpageClick = { /* Webpage click action */ },
            isThemeDark = true
        )
    }
}
