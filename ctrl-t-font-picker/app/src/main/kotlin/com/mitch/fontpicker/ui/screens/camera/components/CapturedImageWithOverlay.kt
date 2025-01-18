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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.components.loading.LoadingScreen
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme
import timber.log.Timber

@Composable
fun CapturedImageWithOverlay(
    imageUri: Uri?,
    showContent: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Conditionally create the painter only if showContent is true and imageUri is not null
    val painter = if (showContent && imageUri != null) {
        rememberAsyncImagePainter(imageUri)
    } else {
        null
    }

    if (showContent && painter != null) {
        Timber.d("Displaying captured/selected image and overlay.")
        val visibleState = remember {
            MutableTransitionState(false).apply { targetState = true }
        }

        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(animationSpec = tween(durationMillis = 200))
        ) {
            BoxWithConstraints(
                modifier = modifier
                    .fillMaxSize()
                    .background(FontPickerDesignSystem.extendedColorScheme.pictureBackground)
            ) {
                val containerAspectRatio =
                    constraints.maxWidth.toFloat() / constraints.maxHeight.toFloat()
                var imageModifier = Modifier.fillMaxSize()

                // Ensure that painter state checks are valid
                val state = painter.state.collectAsState().value
                if (state is AsyncImagePainter.State.Success) {
                    val imageWidth = state.painter.intrinsicSize.width
                    val imageHeight = state.painter.intrinsicSize.height
                    if (imageWidth > 0 && imageHeight > 0) {
                        val imageAspectRatio = imageWidth / imageHeight

                        imageModifier = if (imageAspectRatio < containerAspectRatio) {
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(imageAspectRatio)
                        } else {
                            Modifier
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
    } else {
        Timber.d("Hiding overlay as showContent is false or imageUri is null.")
    }
}
