package com.mitch.christmas.data.settings

import com.mitch.christmas.data.language.LanguageLocalDataSource
import com.mitch.christmas.data.language.toDomainModel as toDomainModelLang
import com.mitch.christmas.data.userprefs.UserPreferencesLocalDataSource
import com.mitch.christmas.data.userprefs.toDomainModel as toDomainModelPrefs
import com.mitch.christmas.data.userprefs.toProtoModel
import com.mitch.christmas.domain.models.ChristmasLanguagePreference
import com.mitch.christmas.domain.models.ChristmasThemePreference
import com.mitch.christmas.domain.models.ChristmasUserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DefaultUserSettingsRepository(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
    private val languageLocalDataSource: LanguageLocalDataSource
) : UserSettingsRepository {

    override val preferences: Flow<ChristmasUserPreferences> = combine(
        userPreferencesLocalDataSource.protoPreferences,
        languageLocalDataSource.getLocale()
    ) { protoPreferences, locale ->
        ChristmasUserPreferences(
            theme = protoPreferences.theme.toDomainModelPrefs(),
            language = locale.toDomainModelLang()
        )
    }

    override suspend fun setTheme(theme: ChristmasThemePreference) {
        userPreferencesLocalDataSource.setTheme(theme.toProtoModel())
    }

    override suspend fun setLanguage(language: ChristmasLanguagePreference) {
        languageLocalDataSource.setLocale(language.locale)
    }
}
