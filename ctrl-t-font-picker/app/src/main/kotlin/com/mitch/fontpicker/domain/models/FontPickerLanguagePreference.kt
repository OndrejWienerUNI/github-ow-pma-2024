package com.mitch.fontpicker.domain.models

import java.util.Locale

enum class FontPickerLanguagePreference(val locale: Locale?) {
    FollowSystem(locale = null),
    English(locale = Locale.ENGLISH),
    Czech(locale = Locale("cs", "CZ"));

    companion object {
        val Default: FontPickerLanguagePreference = FollowSystem

        fun fromLocale(locale: Locale): FontPickerLanguagePreference {
            return entries.find { it.locale == locale } ?: Default
        }
    }
}
