package com.mitch.fontpicker.ui.screens.favorites.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.data.api.FontDownloaded
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.components.cards.FontCard
import com.mitch.fontpicker.ui.designsystem.components.loading.LoadingScreen
import com.mitch.fontpicker.ui.designsystem.components.overlays.ErrorOverlay
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding
import timber.log.Timber
import java.util.UUID

private val GRADIENT_HEIGHT_TOP = 10.dp
private val GRADIENT_HEIGHT_BOTTOM = 10.dp

@Composable
fun FontCardListScreenContent(
    uiState: FontCardListUiState,
    onToggleLike: (FontDownloaded) -> Unit,
    onRetry: () -> Unit,
    listEndText: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    when (uiState) {
        is FontCardListUiState.Loading -> {
            Timber.d("LoadingScreen displayed.")
            LoadingScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FontPickerDesignSystem.colorScheme.background.copy(alpha = 0.5f))
            )
        }
        is FontCardListUiState.Success -> {
            Timber.d("FavoritesScreenContent displaying ${uiState.fontPreviews.size} fonts.")

            Box(modifier = Modifier.fillMaxSize()) {

                val displayedFonts = uiState.fontPreviews

                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(padding.medium),
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    contentPadding = PaddingValues(
                        horizontal = padding.medium, vertical = padding.zero
                    )
                ) {
                    item { /** zero height item for spacing **/ }

                    itemsIndexed(
                        items = displayedFonts,
                        key = { _, fontPreview -> fontPreview.id ?: UUID.randomUUID().toString() }
                    ) { index, fontPreview ->

                        Timber.d("Rendering LazyColumn item: ${fontPreview.title}, " +
                                "ID=${fontPreview.id}")
                        FontCard(
                            font = fontPreview,
                            inSelectionDialog = false,
                            onLikeClick = {
                                Timber.d("FontCard onLikeClick triggered " +
                                        "for font: ${fontPreview.title}")
                                onToggleLike(fontPreview)
                            },
                            onWebpageClick = {
                                Timber.d("FontCard onWebpageClick triggered " +
                                        "for font: ${fontPreview.title} " +
                                        "with URL: ${fontPreview.url}")
                                fontPreview.url.let { url ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            },
                            isThemeDark = isSystemInDarkTheme(),
                            modifier = Modifier.animateItem(
                                fadeInSpec = spring(
                                    dampingRatio = Spring.DampingRatioHighBouncy,
                                    stiffness = Spring.StiffnessLow),
                                fadeOutSpec = spring(
                                    dampingRatio = Spring.DampingRatioHighBouncy,
                                    stiffness = Spring.StiffnessLow)
                            )
                        )

                        if (index == displayedFonts.lastIndex && listEndText.isNotBlank()) {
                            ListEndText(
                                text = listEndText.trim(),
                                modifier = Modifier.animateItem(
                                    fadeInSpec = spring(
                                        dampingRatio = Spring.DampingRatioHighBouncy,
                                        stiffness = Spring.StiffnessLow),
                                    fadeOutSpec = spring(
                                        dampingRatio = Spring.DampingRatioHighBouncy,
                                        stiffness = Spring.StiffnessLow)
                                )
                            )
                        }
                    }

                    if (displayedFonts.isEmpty()) {
                        item { ListEndText(text = listEndText.trim()) }
                    }

                    item { /** zero height item for spacing **/ }
                }


                // Gradient overlay at the top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GRADIENT_HEIGHT_TOP)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    FontPickerDesignSystem.colorScheme.background.copy(alpha = 1f),
                                    FontPickerDesignSystem.colorScheme.background.copy(alpha = 0f)
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                )

                // Gradient overlay at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GRADIENT_HEIGHT_BOTTOM)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    FontPickerDesignSystem.colorScheme.background.copy(alpha = 0f),
                                    FontPickerDesignSystem.colorScheme.background.copy(alpha = 0.7f),
                                    FontPickerDesignSystem.colorScheme.background.copy(alpha = 1f)
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
            }
        }
        is FontCardListUiState.Error -> {
            Timber.e("ErrorOverlay displayed with message: ${uiState.error}")
            ErrorOverlay(
                errorMessage = uiState.error ?: "Unknown Error",
                closable = true,
                onClose = {
                    Timber.d("Error overlay dismissed.")
                    onRetry()
                }
            )
        }
    }
}
