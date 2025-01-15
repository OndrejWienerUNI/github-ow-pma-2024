package com.mitch.fontpicker.ui.designsystem.components.dialogs

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mitch.fontpicker.R
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.components.cards.FontCard
import com.mitch.fontpicker.ui.designsystem.components.cards.FontCardData
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding

private val DIALOG_WIDTH_MAX = 400.dp
private val GRADIENT_HEIGHT = 20.dp

@Composable
fun FontCardSelectionDialog(
    cards: List<FontCardData>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isThemeDark: Boolean = isSystemInDarkTheme()
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxDialogWidth = minOf(DIALOG_WIDTH_MAX, screenWidth)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_received_fonts),
                style = FontPickerDesignSystem.typography.titleMedium,
                color = FontPickerDesignSystem.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = padding.extraSmall)
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = padding.zero)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding.zero, vertical = padding.extraSmall),
                    verticalArrangement = Arrangement.spacedBy(padding.small)
                ) {
                    items(cards) { cardData ->
                        FontCard(
                            name = cardData.name,
                            images = cardData.images,
                            likedInitial = cardData.liked,
                            inSelection = true,
                            onLikeClick = {  },
                            onWebpageClick = {  },
                            isThemeDark = isThemeDark
                        )
                    }
                }

                // Gradient overlay at the top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GRADIENT_HEIGHT)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    FontPickerDesignSystem.colorScheme.surface.copy(alpha = 1f),
                                    FontPickerDesignSystem.colorScheme.surface.copy(alpha = 0.7f),
                                    FontPickerDesignSystem.colorScheme.surface.copy(alpha = 0f)
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                )

                // Gradient overlay at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GRADIENT_HEIGHT)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    FontPickerDesignSystem.colorScheme.surface.copy(alpha = 0f),
                                    FontPickerDesignSystem.colorScheme.surface.copy(alpha = 0.7f),
                                    FontPickerDesignSystem.colorScheme.surface.copy(alpha = 1f)
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.confirm),
                    color = FontPickerDesignSystem.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = FontPickerDesignSystem.colorScheme.primary
                )
            }
        },
        containerColor = FontPickerDesignSystem.colorScheme.surface,
        textContentColor = FontPickerDesignSystem.colorScheme.onSurface,
        titleContentColor = FontPickerDesignSystem.colorScheme.primary,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = maxDialogWidth)
            .padding(horizontal = padding.medium, vertical = padding.extraLarge)
    )
}

@Composable
@PreviewLightDark
fun FontCardSelectionAlertDialogPreview() {
    val sampleBitmap = Bitmap.createBitmap(500, 120, Bitmap.Config.ARGB_8888)
    val sampleCards = List(5) {
        FontCardData(
            name = "Example Font $it",
            images = listOf(sampleBitmap, sampleBitmap, sampleBitmap),
            liked = false,
            onLikeClick = {},
            onWebpageClick = {}
        )
    }
    FontPickerTheme {
        FontCardSelectionDialog(
            cards = sampleCards,
            onDismiss = {},
            onConfirm = {}
        )
    }
}
