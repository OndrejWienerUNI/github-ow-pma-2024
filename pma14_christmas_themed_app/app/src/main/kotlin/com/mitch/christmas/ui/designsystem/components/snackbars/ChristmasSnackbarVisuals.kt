package com.mitch.christmas.ui.designsystem.components.snackbars

import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mitch.christmas.ui.designsystem.ChristmasDesignSystem
import com.mitch.christmas.ui.designsystem.ChristmasIcons
import com.mitch.christmas.ui.designsystem.theme.custom.extendedColorScheme

data class SnackbarEvent(
    val message: String,
    val action: SnackbarAction? = null,
    val onDismiss: (() -> Unit)? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val type: ChristmasSnackbarType = ChristmasSnackbarType.Default,
    val imageVector: ImageVector? = when (type) {
        ChristmasSnackbarType.Default -> null
        ChristmasSnackbarType.Success -> ChristmasIcons.Filled.Success
        ChristmasSnackbarType.Warning -> ChristmasIcons.Filled.Warning
        ChristmasSnackbarType.Error -> ChristmasIcons.Filled.Error
    }
)

data class SnackbarAction(val label: String, val onPerformAction: () -> Unit)

data class ChristmasSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    val onPerformAction: (() -> Unit)? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = duration == SnackbarDuration.Indefinite,
    val type: ChristmasSnackbarType = ChristmasSnackbarType.Default,
    val imageVector: ImageVector? = when (type) {
        ChristmasSnackbarType.Default -> null
        ChristmasSnackbarType.Success -> ChristmasIcons.Filled.Success
        ChristmasSnackbarType.Warning -> ChristmasIcons.Filled.Warning
        ChristmasSnackbarType.Error -> ChristmasIcons.Filled.Error
    }
) : SnackbarVisuals

fun SnackbarEvent.toVisuals(): ChristmasSnackbarVisuals {
    return ChristmasSnackbarVisuals(
        message = this.message,
        actionLabel = this.action?.label,
        duration = this.duration,
        type = this.type,
        imageVector = this.imageVector
    )
}

enum class ChristmasSnackbarType {
    Default,
    Success,
    Warning,
    Error
}

data class ChristmasSnackbarColors(
    val containerColor: Color,
    val iconColor: Color,
    val messageColor: Color,
    val actionColor: Color
)

object ChristmasSnackbarDefaults {

    @Composable
    fun defaultSnackbarColors(
        containerColor: Color = SnackbarDefaults.color,
        messageColor: Color = SnackbarDefaults.contentColor,
        actionColor: Color = SnackbarDefaults.actionContentColor,
        iconColor: Color = SnackbarDefaults.contentColor
    ): ChristmasSnackbarColors {
        return ChristmasSnackbarColors(
            containerColor = containerColor,
            messageColor = messageColor,
            actionColor = actionColor,
            iconColor = iconColor
        )
    }

    @Composable
    fun successSnackbarColors(
        containerColor: Color = ChristmasDesignSystem.extendedColorScheme.success,
        messageColor: Color = ChristmasDesignSystem.extendedColorScheme.onSuccess,
        actionColor: Color = ChristmasDesignSystem.extendedColorScheme.onSuccess,
        iconColor: Color = actionColor
    ): ChristmasSnackbarColors {
        return ChristmasSnackbarColors(
            containerColor = containerColor,
            messageColor = messageColor,
            actionColor = actionColor,
            iconColor = iconColor
        )
    }

    @Composable
    fun warningSnackbarColors(
        containerColor: Color = ChristmasDesignSystem.extendedColorScheme.warning,
        messageColor: Color = ChristmasDesignSystem.extendedColorScheme.onWarning,
        actionColor: Color = ChristmasDesignSystem.extendedColorScheme.onWarning,
        iconColor: Color = actionColor
    ): ChristmasSnackbarColors {
        return ChristmasSnackbarColors(
            containerColor = containerColor,
            messageColor = messageColor,
            actionColor = actionColor,
            iconColor = iconColor
        )
    }

    @Composable
    fun errorSnackbarColors(
        containerColor: Color = ChristmasDesignSystem.colorScheme.errorContainer,
        messageColor: Color = ChristmasDesignSystem.colorScheme.onErrorContainer,
        actionColor: Color = ChristmasDesignSystem.colorScheme.onErrorContainer,
        iconColor: Color = actionColor
    ): ChristmasSnackbarColors {
        return ChristmasSnackbarColors(
            containerColor = containerColor,
            messageColor = messageColor,
            actionColor = actionColor,
            iconColor = iconColor
        )
    }
}
