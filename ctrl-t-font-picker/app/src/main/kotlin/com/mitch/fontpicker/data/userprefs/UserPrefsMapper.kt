package com.mitch.fontpicker.data.userprefs

import com.mitch.fontpicker.data.userprefs.UserPreferencesProtoModel.FontPickerThemePreferenceProto
import com.mitch.fontpicker.domain.models.FontPickerThemePreference

fun FontPickerThemePreference.toProtoModel(): FontPickerThemePreferenceProto = when (this) {
    FontPickerThemePreference.FollowSystem -> FontPickerThemePreferenceProto.FOLLOW_SYSTEM
    FontPickerThemePreference.Light -> FontPickerThemePreferenceProto.LIGHT
    FontPickerThemePreference.Dark -> FontPickerThemePreferenceProto.DARK
}

fun FontPickerThemePreferenceProto.toDomainModel(): FontPickerThemePreference = when (this) {
    FontPickerThemePreferenceProto.LIGHT -> FontPickerThemePreference.Light
    FontPickerThemePreferenceProto.DARK -> FontPickerThemePreference.Dark
    FontPickerThemePreferenceProto.UNRECOGNIZED,
    FontPickerThemePreferenceProto.FOLLOW_SYSTEM -> FontPickerThemePreference.FollowSystem
}
