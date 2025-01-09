package com.mitch.fontpicker.ui.screens.camera.components

import android.net.Uri
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.rememberAsyncImagePainter
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme
import timber.log.Timber

private val LIVE_VIEW_CORNER_RADIUS = 16.dp
private val LIVE_VIEW_BORDER_WIDTH = 1.dp
private const val ASPECT_RATIO = 3 / 4f

@Composable
@Suppress("UNUSED_PARAMETER")
fun CameraLiveView(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LIVE_VIEW_CORNER_RADIUS),
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    photoUri: Uri? = null,
    isLoading: Boolean = false,
    onCameraReady: (Boolean) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

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
    ) {
        when {
            photoUri != null -> {
                // Show captured photo
                androidx.compose.foundation.Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = "Captured Photo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                // Show live camera feed
                AndroidView(
                    factory = {
                        previewView.apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Show loading overlay during photo capture
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FontPickerDesignSystem.colorScheme.background.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }

        // Initialize the camera on mount
        DisposableEffect(Unit) {
            val cameraProvider = cameraProviderFuture.get()

            try {
                val preview = androidx.camera.core.Preview.Builder().build().apply {
                    surfaceProvider = previewView.surfaceProvider
                }
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                Timber.e(e, "Error handling surface.")
            }

            onDispose {
                cameraProvider.unbindAll()
            }
        }

    }
}
