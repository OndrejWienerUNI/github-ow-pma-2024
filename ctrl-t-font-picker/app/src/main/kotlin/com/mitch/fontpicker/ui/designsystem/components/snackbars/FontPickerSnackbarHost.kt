package com.mitch.fontpicker.ui.designsystem.components.snackbars

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mitch.fontpicker.R
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding

@Composable
fun FontPickerSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = hostState,
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = padding.medium)
            .then(modifier)
    ) { snackbarData ->
        val customVisuals = snackbarData.visuals as FontPickerSnackbarVisuals

        val colors = when (customVisuals.type) {
            FontPickerSnackbarType.Default -> FontPickerSnackbarDefaults.defaultSnackbarColors()
            FontPickerSnackbarType.Success -> FontPickerSnackbarDefaults.successSnackbarColors()
            FontPickerSnackbarType.Warning -> FontPickerSnackbarDefaults.warningSnackbarColors()
            FontPickerSnackbarType.Error -> FontPickerSnackbarDefaults.errorSnackbarColors()
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            FontPickerSnackbar(
                colors = colors,
                icon = customVisuals.imageVector,
                message = customVisuals.message,
                action = if (
                    customVisuals.actionLabel != null &&
                    customVisuals.onPerformAction != null
                ) {
                    SnackbarAction(
                        label = customVisuals.actionLabel,
                        onPerformAction = customVisuals.onPerformAction
                    )
                } else {
                    null
                },
                dismissAction = if (customVisuals.duration == SnackbarDuration.Indefinite) {
                    {
                        IconButton(
                            onClick = snackbarData::dismiss,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = FontPickerDesignSystem.colorScheme.inverseOnSurface
                            )
                        ) {
                            Icon(
                                imageVector = FontPickerIcons.Outlined.Close,
                                contentDescription = stringResource(
                                    id = R.string.dismiss_snackbar_content_description
                                )
                            )
                        }
                    }
                } else {
                    null
                }
            )
        }
    }
}
