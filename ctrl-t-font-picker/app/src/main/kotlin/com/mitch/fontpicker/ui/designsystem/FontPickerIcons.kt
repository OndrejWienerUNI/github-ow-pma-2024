package com.mitch.fontpicker.ui.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.EvaIcons
import compose.icons.evaicons.Fill
import compose.icons.evaicons.Outline
import compose.icons.evaicons.fill.AlertTriangle
import compose.icons.evaicons.fill.CheckmarkCircle2
import compose.icons.evaicons.fill.CloseCircle
import compose.icons.evaicons.fill.Heart
import compose.icons.evaicons.outline.Heart

object FontPickerIcons {
    object Filled {
        val Success: ImageVector = EvaIcons.Fill.CheckmarkCircle2
        val Warning: ImageVector = EvaIcons.Fill.AlertTriangle
        val Error: ImageVector = EvaIcons.Fill.CloseCircle
        val Heart: ImageVector = EvaIcons.Fill.Heart
    }

    object Outlined {
        val LightMode: ImageVector = Icons.Outlined.LightMode
        val DarkMode: ImageVector = Icons.Outlined.DarkMode
        val FollowSystem: ImageVector = Icons.Outlined.Contrast
        val Translate: ImageVector = Icons.Outlined.Translate
        val Palette: ImageVector = Icons.Outlined.Palette
        val Close: ImageVector = Icons.Outlined.Close
        val Heart: ImageVector = EvaIcons.Outline.Heart
    }
}
