package com.mitch.christmas.data.userprefs

import com.mitch.christmas.data.userprefs.UserPreferencesProtoModel.ChristmasThemePreferenceProto
import com.mitch.christmas.domain.models.ChristmasThemePreference

fun ChristmasThemePreference.toProtoModel(): ChristmasThemePreferenceProto = when (this) {
    ChristmasThemePreference.FollowSystem -> ChristmasThemePreferenceProto.FOLLOW_SYSTEM
    ChristmasThemePreference.Light -> ChristmasThemePreferenceProto.LIGHT
    ChristmasThemePreference.Dark -> ChristmasThemePreferenceProto.DARK
}

fun ChristmasThemePreferenceProto.toDomainModel(): ChristmasThemePreference = when (this) {
    ChristmasThemePreferenceProto.LIGHT -> ChristmasThemePreference.Light
    ChristmasThemePreferenceProto.DARK -> ChristmasThemePreference.Dark
    ChristmasThemePreferenceProto.UNRECOGNIZED,
    ChristmasThemePreferenceProto.FOLLOW_SYSTEM -> ChristmasThemePreference.FollowSystem
}
