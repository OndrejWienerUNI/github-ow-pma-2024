package com.mitch.template.domain.models

import java.util.Locale

enum class TemplateLanguagePreference(val locale: Locale) {
    English(locale = Locale.ENGLISH),
    Czech(locale = Locale("cs", "CZ"));

    companion object {
        val Default: TemplateLanguagePreference = English

        fun fromLocale(locale: Locale): TemplateLanguagePreference {
            return entries.find { it.locale.language == locale.language } ?: Default
        }
    }
}
