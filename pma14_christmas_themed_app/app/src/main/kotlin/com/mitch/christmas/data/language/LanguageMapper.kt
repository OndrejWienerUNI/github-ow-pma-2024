package com.mitch.christmas.data.language

import com.mitch.christmas.domain.models.ChristmasLanguagePreference
import java.util.Locale

fun Locale.toDomainModel(): ChristmasLanguagePreference {
    // removes country code and variants if present
    val localeLanguageOnly = Locale.forLanguageTag(this.language)

    return ChristmasLanguagePreference.fromLocale(localeLanguageOnly)
}
