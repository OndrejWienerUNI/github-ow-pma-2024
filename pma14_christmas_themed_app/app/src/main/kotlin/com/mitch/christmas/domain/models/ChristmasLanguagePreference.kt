package com.mitch.christmas.domain.models

import java.util.Locale

enum class ChristmasLanguagePreference(val locale: Locale) {
    English(locale = Locale.ENGLISH),
    Czech(locale = Locale("cs", "CZ"));

    companion object {
        val Default: ChristmasLanguagePreference = English

        fun fromLocale(locale: Locale): ChristmasLanguagePreference {
            return entries.find { it.locale.language == locale.language } ?: Default
        }
    }
}
