package com.mitch.fontpicker.domain.models

enum class FontPickerThemePreference {
    FollowSystem,
    Light,
    Dark;

    companion object {
        val Default: FontPickerThemePreference = FollowSystem
    }
}
