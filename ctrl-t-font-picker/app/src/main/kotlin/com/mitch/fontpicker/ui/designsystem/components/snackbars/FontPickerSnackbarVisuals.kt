package com.mitch.fontpicker.ui.designsystem.components.snackbars

import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.theme.custom.extendedColorScheme

data class SnackbarEvent(
    val message: String,
    val action: SnackbarAction? = null,
    val onDismiss: (() -> Unit)? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val type: FontPickerSnackbarType = FontPickerSnackbarType.Default,
    val imageVector: ImageVector? = when (type) {
        FontPickerSnackbarType.Default -> null
        FontPickerSnackbarType.Success -> FontPickerIcons.Filled.Success
        FontPickerSnackbarType.Warning -> FontPickerIcons.Filled.Warning
        FontPickerSnackbarType.Error -> FontPickerIcons.Filled.Error
    }
)

data class SnackbarAction(val label: String, val onPerformAction: () -> Unit)

data class FontPickerSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    val onPerformAction: (() -> Unit)? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = duration == SnackbarDuration.Indefinite,
    val type: FontPickerSnackbarType = FontPickerSnackbarType.Default,
    val imageVector: ImageVector? = when (type) {
        FontPickerSnackbarType.Default -> null
        FontPickerSnackbarType.Success -> FontPickerIcons.Filled.Success
        FontPickerSnackbarType.Warning -> FontPickerIcons.Filled.Warning
        FontPickerSnackbarType.Error -> FontPickerIcons.Filled.Error
    }
) : SnackbarVisuals

fun SnackbarEvent.toVisuals(): FontPickerSnackbarVisuals {
    return FontPickerSnackbarVisuals(
        message = this.message,
        actionLabel = this.action?.label,
        duration = this.duration,
        type = this.type,
        imageVector = this.imageVector
    )
}

enum class FontPickerSnackbarType {
    Default,
    Success,
    Warning,
    Error
}

data class FontPickerSnackbarColors(
    val containerColor: Color,
    val iconColor: Color,
    val messageColor: Color,
    val actionColor: Color
)

object FontPickerSnackbarDefaults {

    @Composable
    fun defaultSnackbarColors(
        containerColor: Color = SnackbarDefaults.color,
        messageColor: Color = SnackbarDefaults.contentColor,
        actionColor: Color = SnackbarDefaults.actionContentColor,
        iconColor: Color = SnackbarDefaults.contentColor
    ): FontPickerSnackbarColors {
        return FontPickerSnackbarColors(
            containerColor = containerColor,
            messageColor = messageColor,
            actionColor = actionColor,
            iconColor = iconColor
        )
    }

    @Composable
    fun successSnackbarColors(
        containerColor: Color = FontPickerDesignSystem.extendedColorScheme.success,
        messageColor: Color = FontPickerDesignSystem.extendedColorScheme.onSuccess,
        actionColor: Color = FontPickerDesignSystem.extendedColorScheme.onSuccess,
        iconColor: Color = actionColor
    ): FontPickerSnackbarColors {
        return FontPickerSnackbarColors(
            containerColor = containerColor,
            messageColor = messageColor,
            actionColor = actionColor,
            iconColor = iconColor
        )
    }

    @Composable
    fun warningSnackbarColors(
        containerColor: Color = FontPickerDesignSystem.extendedColorScheme.warning,
        messageColor: Color = FontPickerDesignSystem.extendedColorScheme.onWarning,
        actionColor: Color = FontPickerDesignSystem.extendedColorScheme.onWarning,
        iconColor: Color = actionColor
    ): FontPickerSnackbarColors {
        return FontPickerSnackbarColors(
            containerColor = containerColor,
            messageColor = messageColor,
            actionColor = actionColor,
            iconColor = iconColor
        )
    }

    @Composable
    fun errorSnackbarColors(
        containerColor: Color = FontPickerDesignSystem.colorScheme.errorContainer,
        messageColor: Color = FontPickerDesignSystem.colorScheme.onErrorContainer,
        actionColor: Color = FontPickerDesignSystem.colorScheme.onErrorContainer,
        iconColor: Color = actionColor
    ): FontPickerSnackbarColors {
        return FontPickerSnackbarColors(
            containerColor = containerColor,
            messageColor = messageColor,
            actionColor = actionColor,
            iconColor = iconColor
        )
    }
}
