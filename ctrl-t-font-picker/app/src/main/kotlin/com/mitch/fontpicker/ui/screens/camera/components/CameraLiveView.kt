package com.mitch.fontpicker.ui.screens.camera.components

import android.net.Uri
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme
import timber.log.Timber

private val LIVE_VIEW_CORNER_RADIUS = 16.dp
private val LIVE_VIEW_BORDER_WIDTH = 1.dp

@Composable
fun CameraLiveView(
    cameraPreviewView: androidx.camera.core.Preview?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LIVE_VIEW_CORNER_RADIUS),
    photoUri: Uri? = null,
    isLoading: Boolean? = false,
    aspectRatio: Float = 3f / 4f
) {
    // Maintain a remembered state that only updates when isLoading is not null
    val isLoadingNonNull = remember { mutableStateOf(false) }

    if (isLoading != null) {
        isLoadingNonNull.value = isLoading
    }

    Timber.d("CameraLiveView Composable called " +
            "with isLoadingNonNull: $isLoadingNonNull and photoUri: $photoUri")

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .background(
                color = FontPickerDesignSystem.colorScheme.background,
                shape = shape
            )
            .border(
                width = LIVE_VIEW_BORDER_WIDTH,
                color = if (isLoadingNonNull.value) FontPickerDesignSystem.colorScheme.primary
                else FontPickerDesignSystem.extendedColorScheme.borders,
                shape = shape
            )
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
                    if (cameraPreviewView != null) {
                        Timber.d("Setting SurfaceProvider for PreviewView in factory.")
                        cameraPreviewView.surfaceProvider = this.surfaceProvider
                    } else {
                        Timber.e("Preview is null in factory. Cannot set SurfaceProvider.")
                    }
                }
            },
            update = { view ->
                if (cameraPreviewView != null) {
                    Timber.d("Updating SurfaceProvider in update lambda.")
                    cameraPreviewView.surfaceProvider = view.surfaceProvider
                } else {
                    Timber.e("Preview is null in update. Cannot set SurfaceProvider.")
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Display captured photo overlay if a photo is being processed
        if (photoUri != null && isLoadingNonNull.value) {
            Timber.d("Displaying CapturedImageWithOverlay for URI: $photoUri")
            CapturedImageWithOverlay(photoUri = photoUri)
        }
    }
}