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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.data.api.FontDownloaded
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.components.cards.FontCard
import com.mitch.fontpicker.ui.designsystem.components.dialogs.ErrorDialog
import com.mitch.fontpicker.ui.designsystem.components.loading.LoadingScreen
import timber.log.Timber

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
    val isThemeDark = isSystemInDarkTheme()

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

            val displayedFonts = uiState.fontPreviews

            // Filter fonts with non-null IDs and log excluded ones
            val filteredFonts = remember(displayedFonts) {
                displayedFonts.filter { font ->
                    if (font.id != null) {
                        true
                    } else {
                        Timber.w(
                            "Font excluded due to missing ID: " +
                                    "Title='${font.title}', URL='${font.url}', " +
                                    "ImageUrls=${font.imageUrls}, Bitmaps=${font.bitmaps}"
                        )
                        false
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {

                FontListLazyColumn(
                    filteredFonts = filteredFonts,
                    listEndText = listEndText,
                    onToggleLike = onToggleLike,
                    onWebpageClick = { fontPreview ->
                        Timber.d("FontCard onWebpageClick triggered for font: ${fontPreview.title} " +
                                "with ID: ${fontPreview.id} and URL: ${fontPreview.url}")
                        fontPreview.url.let { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    },
                    isThemeDark = isThemeDark,
                    modifier = modifier
                )

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
            Timber.e("ErrorOverlay displayed with message: ${uiState.errorMessage}")
            ErrorDialog(
                errorMessage = uiState.errorMessage ?: "Unknown Error, please try again later.",
                onDismiss = {
                    onRetry()
                }
            )
        }
    }
}


@Composable
fun FontListLazyColumn(
    filteredFonts: List<FontDownloaded>,
    listEndText: String,
    onToggleLike: (FontDownloaded) -> Unit,
    onWebpageClick: (FontDownloaded) -> Unit,
    isThemeDark: Boolean,
    modifier: Modifier = Modifier
) {
    // Hardcoded padding values
    val horizontalPadding = 16.dp
    val verticalSpacing = 8.dp

    // Hardcoded spring animation parameters
    val springDampingRatio = Spring.DampingRatioHighBouncy
    val springStiffness = Spring.StiffnessLow

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        flingBehavior = ScrollableDefaults.flingBehavior(),
        contentPadding = PaddingValues(
            horizontal = horizontalPadding, vertical = 0.dp
        )
    ) {
        // Top spacing
        item {
            Spacer(
                modifier = Modifier
                    .height(0.dp)
                    .animateItem(
                        fadeInSpec = spring(
                            dampingRatio = springDampingRatio,
                            stiffness = springStiffness
                        ),
                        fadeOutSpec = spring(
                            dampingRatio = springDampingRatio,
                            stiffness = springStiffness
                        )
                    )
            )
        }

        // Render filtered fonts
        itemsIndexed(
            items = filteredFonts,
            key = { _, fontPreview -> fontPreview.id!! }
        ) { index, fontPreview ->

            Timber.d("Rendering LazyColumn item: ${fontPreview.title}, ID=${fontPreview.id}")
            FontCard(
                font = fontPreview,
                inSelectionDialog = false,
                onLikeClick = {
                    Timber.d("FontCard onLikeClick triggered for font: ${fontPreview.title}")
                    onToggleLike(fontPreview)
                },
                onWebpageClick = {
                    Timber.d(
                        "FontCard onWebpageClick triggered for font: ${fontPreview.title} " +
                                "with ID: ${fontPreview.id} and URL: ${fontPreview.url}"
                    )
                    onWebpageClick(fontPreview)
                },
                isThemeDark = isThemeDark,
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(
                        dampingRatio = springDampingRatio,
                        stiffness = springStiffness
                    ),
                    fadeOutSpec = spring(
                        dampingRatio = springDampingRatio,
                        stiffness = springStiffness
                    )
                )
            )

            // List end text
            if (index == filteredFonts.lastIndex && listEndText.isNotBlank()) {
                ListEndText(
                    text = listEndText.trim(),
                    modifier = Modifier.animateItem(
                        fadeInSpec = spring(
                            dampingRatio = springDampingRatio,
                            stiffness = springStiffness
                        ),
                        fadeOutSpec = spring(
                            dampingRatio = springDampingRatio,
                            stiffness = springStiffness
                        )
                    )
                )
            }
        }

        // Handle empty list
        if (filteredFonts.isEmpty()) {
            item {
                ListEndText(
                    text = listEndText.trim(),
                    modifier = Modifier.animateItem(
                        fadeInSpec = spring(
                            dampingRatio = springDampingRatio,
                            stiffness = springStiffness
                        ),
                        fadeOutSpec = spring(
                            dampingRatio = springDampingRatio,
                            stiffness = springStiffness
                        )
                    )
                )
            }
        }

        // Bottom spacing
        item {
            Spacer(
                modifier = Modifier
                    .height(0.dp)
                    .animateItem(
                        fadeInSpec = spring(
                            dampingRatio = springDampingRatio,
                            stiffness = springStiffness
                        ),
                        fadeOutSpec = spring(
                            dampingRatio = springDampingRatio,
                            stiffness = springStiffness
                        )
                    )
            )
        }
    }
}
