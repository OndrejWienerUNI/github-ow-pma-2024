package com.mitch.fontpicker.ui.screens.camera.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.components.loading.LoadingScreen
import kotlinx.coroutines.delay
import timber.log.Timber

@Composable
fun CapturedImageWithOverlay(
    photoUri: Uri,
    showContent: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (showContent) {
        Timber.d("Displaying captured/selected image and overlay.")
        val visibleState = remember {
            MutableTransitionState(false).apply { targetState = true }
        }

        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(animationSpec = tween(durationMillis = 300))
        ) {
            BoxWithConstraints(
                modifier = modifier
                    .fillMaxSize()
                    .background(FontPickerDesignSystem.colorScheme.tertiary) // Tertiary background by default
            ) {
                val containerAspectRatio = constraints.maxWidth.toFloat() / constraints.maxHeight.toFloat()

                // Use AsyncImagePainter to load the image
                val painter = rememberAsyncImagePainter(photoUri)
                val state = painter.state // This is a StateFlow

                // Collect the state flow as a Compose state
                val currentState = state.collectAsState().value

                // Default modifier for the image
                var imageModifier = Modifier.fillMaxSize()

                if (currentState is AsyncImagePainter.State.Success) {
                    val imageWidth = currentState.painter.intrinsicSize.width
                    val imageHeight = currentState.painter.intrinsicSize.height
                    if (imageWidth > 0 && imageHeight > 0) {
                        val imageAspectRatio = imageWidth / imageHeight
                        val tertiaryBackground = FontPickerDesignSystem.colorScheme.tertiary

                        if (imageAspectRatio < containerAspectRatio) {
                            LaunchedEffect(Unit) {
                                imageModifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(imageAspectRatio)
                                    .background(tertiaryBackground)
                                delay(100)
                            }
                        } else {
                            imageModifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(imageAspectRatio)
                        }
                    }
                }

                androidx.compose.foundation.Image(
                    painter = painter,
                    contentDescription = "Captured Photo",
                    modifier = imageModifier.align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )

                // Overlay loading screen
                LoadingScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FontPickerDesignSystem.colorScheme.background.copy(alpha = 0.5f))
                )
            }
        }
    }
}
