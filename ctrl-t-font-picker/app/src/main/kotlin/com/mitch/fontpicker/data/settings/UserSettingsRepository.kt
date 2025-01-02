package com.mitch.fontpicker.data.settings

import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import com.mitch.fontpicker.domain.models.FontPickerUserPreferences
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val preferences: Flow<FontPickerUserPreferences>
    suspend fun initLocaleIfNeeded()
    suspend fun setTheme(theme: FontPickerThemePreference)
    suspend fun setLanguage(language: FontPickerLanguagePreference)
}
