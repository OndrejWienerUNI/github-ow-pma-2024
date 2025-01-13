package com.mitch.fontpicker.ui.screens.camera.components

import android.net.Uri
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme
import kotlinx.coroutines.launch
import timber.log.Timber

private val LIVE_VIEW_CORNER_RADIUS = 16.dp
private val LIVE_VIEW_BORDER_WIDTH = 1.dp
private const val ASPECT_RATIO = 3 / 4f

@Composable
fun CameraLiveView(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LIVE_VIEW_CORNER_RADIUS),
    lensFacing: Int,
    photoUri: Uri? = null,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    var hasError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Define the preview instance once, outside bindCamera
    val preview = remember {
        androidx.camera.core.Preview.Builder().build().apply {
            surfaceProvider = previewView.surfaceProvider
        }
    }

    fun bindCamera() {
        scope.launch {
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Unbind all and rebind with the new lensFacing
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
                hasError = false // Clear error state if binding succeeds
                Timber.d("Camera bound successfully with lensFacing: $lensFacing.")
            } catch (e: Exception) {
                Timber.e(e, "Error binding camera.")
                hasError = true
            }
        }
    }

    // Rebind the camera whenever lensFacing changes
    LaunchedEffect(lensFacing) {
        Timber.d("Lens facing changed: $lensFacing")
        bindCamera()
    }

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
        // Always keep the camera preview in the composition
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

        if (photoUri != null && isLoading) {
            CapturedImageWithOverlay(photoUri = photoUri)
        }

        // Show an error overlay if the camera fails
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FontPickerDesignSystem.colorScheme.background.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Button(onClick = { bindCamera() }) {
                    androidx.compose.material3.Text("Retry")
                }
            }
        }
    }
}
