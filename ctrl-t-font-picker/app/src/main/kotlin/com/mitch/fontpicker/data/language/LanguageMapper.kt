package com.mitch.fontpicker.data.language

import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import java.util.Locale

fun Locale.toDomainModel(): FontPickerLanguagePreference {
    // removes country code and variants if present
    val localeLanguageOnly = Locale.forLanguageTag(this.language)

    return FontPickerLanguagePreference.fromLocale(localeLanguageOnly)
}
