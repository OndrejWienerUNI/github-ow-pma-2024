package com.mitch.fontpicker.ui.screens.camera.components

import android.net.Uri
import android.view.ViewGroup
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme
import timber.log.Timber

private val LIVE_VIEW_CORNER_RADIUS = 16.dp
private val LIVE_VIEW_BORDER_WIDTH = 1.dp
private const val ASPECT_RATIO = 3f / 4f // Corrected aspect ratio

@Composable
fun CameraLiveView(
    preview: Preview?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LIVE_VIEW_CORNER_RADIUS),
    photoUri: Uri? = null,
    isLoading: Boolean = false
) {
    Timber.d("CameraLiveView Composable called with isLoading: $isLoading and photoUri: $photoUri")

    Box(
        modifier = modifier
            .aspectRatio(ASPECT_RATIO)
            .background(
                color = FontPickerDesignSystem.colorScheme.background,
                shape = shape
            )
            .border(
                width = LIVE_VIEW_BORDER_WIDTH,
                color = if (isLoading) FontPickerDesignSystem.colorScheme.primary
                else FontPickerDesignSystem.extendedColorScheme.borders,
                shape = shape
            )
            .animateContentSize()
    ) {
        // Camera Preview
        AndroidView(
            factory = { context ->
                Timber.d("Creating PreviewView.")
                PreviewView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    if (preview != null) {
                        Timber.d("Setting SurfaceProvider for PreviewView in factory.")
                        preview.surfaceProvider = this.surfaceProvider
                    } else {
                        Timber.e("Preview is null in factory. Cannot set SurfaceProvider.")
                    }
                }
            },
            update = { view ->
                if (preview != null) {
                    Timber.d("Updating SurfaceProvider in update lambda.")
                    preview.surfaceProvider = view.surfaceProvider
                } else {
                    Timber.e("Preview is null in update. Cannot set SurfaceProvider.")
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Display captured photo overlay if a photo is being processed
        if (photoUri != null && isLoading) {
            Timber.d("Displaying CapturedImageWithOverlay for URI: $photoUri")
            CapturedImageWithOverlay(photoUri = photoUri)
        }
    }
}