package com.mitch.fontpicker.ui.designsystem.components.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.mitch.fontpicker.R
import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import com.mitch.fontpicker.ui.designsystem.FontPickerDesignSystem
import com.mitch.fontpicker.ui.designsystem.FontPickerIcons
import com.mitch.fontpicker.ui.designsystem.FontPickerTheme
import com.mitch.fontpicker.ui.designsystem.theme.custom.padding

@Composable
fun LanguagePickerDialog(
    selectedLanguage: FontPickerLanguagePreference,
    onDismiss: () -> Unit,
    onConfirm: (FontPickerLanguagePreference) -> Unit
) {
    var tempLanguage by remember { mutableStateOf(selectedLanguage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = FontPickerIcons.Outlined.Translate,
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(id = R.string.change_language))
        },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                for (languagePreference in FontPickerLanguagePreference.entries) {
                    val isSelected = languagePreference == tempLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .selectable(
                                selected = isSelected,
                                onClick = { tempLanguage = languagePreference },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = padding.medium),
                        horizontalArrangement = Arrangement.spacedBy(padding.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(padding.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = languagePreference.flag(),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (languagePreference.locale == null) {
                                    stringResource(id = R.string.system_default)
                                } else {
                                    languagePreference.locale.displayLanguage
                                },
                                style = FontPickerDesignSystem.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(tempLanguage)
                    onDismiss()
                },
                enabled = tempLanguage != selectedLanguage
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    )
}

@Composable
private fun FontPickerLanguagePreference.flag(): Painter {
    val flagId = when (this) {
        FontPickerLanguagePreference.FollowSystem -> R.drawable.earth_flag
        FontPickerLanguagePreference.English -> R.drawable.english_flag
        FontPickerLanguagePreference.Czech -> R.drawable.czech_flag
    }
    return painterResource(id = flagId)
}

@PreviewLightDark
@Composable
private fun LanguagePickerDialogPreview() {
    FontPickerTheme {
        LanguagePickerDialog(
            selectedLanguage = FontPickerLanguagePreference.English,
            onDismiss = { },
            onConfirm = { }
        )
    }
}