package com.mitch.christmas.data.settings

import com.mitch.christmas.domain.models.ChristmasLanguagePreference
import com.mitch.christmas.domain.models.ChristmasThemePreference
import com.mitch.christmas.domain.models.ChristmasUserPreferences
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val preferences: Flow<ChristmasUserPreferences>
    suspend fun setTheme(theme: ChristmasThemePreference)
    suspend fun setLanguage(language: ChristmasLanguagePreference)
}
