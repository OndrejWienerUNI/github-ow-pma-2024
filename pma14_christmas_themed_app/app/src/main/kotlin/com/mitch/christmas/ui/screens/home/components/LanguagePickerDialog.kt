package com.mitch.christmas.ui.screens.home.components

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.mitch.christmas.R
import com.mitch.christmas.domain.models.ChristmasLanguagePreference
import com.mitch.christmas.ui.designsystem.ChristmasDesignSystem
import com.mitch.christmas.ui.designsystem.ChristmasIcons
import com.mitch.christmas.ui.designsystem.ChristmasTheme
import com.mitch.christmas.ui.designsystem.theme.custom.padding

@Composable
fun LanguagePickerDialog(
    selectedLanguage: ChristmasLanguagePreference,
    onDismiss: () -> Unit,
    onConfirm: (ChristmasLanguagePreference) -> Unit
) {
    var tempLanguage by remember { mutableStateOf(selectedLanguage) }

    val items = listOf(
        LanguagePickerItem.English,
        LanguagePickerItem.Czech
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = ChristmasIcons.Outlined.Translate,
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(id = R.string.change_language))
        },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                for (item in items) {
                    val isSelected = item.language == tempLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .selectable(
                                selected = isSelected,
                                onClick = { tempLanguage = item.language },
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
                                painter = painterResource(id = item.flagId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .testTag(item.flagId.toString())
                            )
                            Text(
                                text = item.language.locale.displayLanguage,
                                style = ChristmasDesignSystem.typography.bodyLarge
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

sealed class LanguagePickerItem(
    val language: ChristmasLanguagePreference,
    @DrawableRes val flagId: Int
) {
    data object English : LanguagePickerItem(
        language = ChristmasLanguagePreference.English,
        flagId = R.drawable.english_flag
    )

    data object Czech : LanguagePickerItem(
        language = ChristmasLanguagePreference.Czech,
        flagId = R.drawable.czech_flag
    )
}

@PreviewLightDark
@Composable
private fun LanguagePickerDialogPreview() {
    ChristmasTheme {
        LanguagePickerDialog(
            selectedLanguage = ChristmasLanguagePreference.English,
            onDismiss = { },
            onConfirm = { }
        )
    }
}
