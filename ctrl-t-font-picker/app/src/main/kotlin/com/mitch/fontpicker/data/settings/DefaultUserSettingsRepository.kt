package com.mitch.fontpicker.data.settings

import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mitch.fontpicker.data.userprefs.UserPreferencesLocalDataSource
import com.mitch.fontpicker.data.userprefs.toDomainModel
import com.mitch.fontpicker.data.userprefs.toProtoModel
import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import com.mitch.fontpicker.domain.models.FontPickerThemePreference
import com.mitch.fontpicker.domain.models.FontPickerUserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale

class DefaultUserSettingsRepository(
    private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource
) : UserSettingsRepository {

    override val preferences: Flow<FontPickerUserPreferences> = userPreferencesLocalDataSource
        .protoPreferences
        .map { protoPreferences ->
            FontPickerUserPreferences(
                theme = protoPreferences.theme.toDomainModel(),
                language = if (protoPreferences.locale.isEmpty()) {
                    FontPickerLanguagePreference.FollowSystem
                } else {
                    FontPickerLanguagePreference.fromLocale(Locale(protoPreferences.locale))
                }
            )
        }

    override suspend fun initLocaleIfNeeded() {
        val languagePreference = preferences.firstOrNull()?.language
        if (
            languagePreference?.locale != null &&
            languagePreference != FontPickerLanguagePreference.FollowSystem
        ) {
            setAppLocale(languagePreference.locale)
        }
    }

    override suspend fun setTheme(theme: FontPickerThemePreference) {
        userPreferencesLocalDataSource.setTheme(theme.toProtoModel())
    }

    override suspend fun setLanguage(language: FontPickerLanguagePreference) {
        if (language.locale == null) {
            userPreferencesLocalDataSource.resetLocale()
            setAppLocale(locale = null)
        } else {
            userPreferencesLocalDataSource.setLocale(language.locale)
            setAppLocale(language.locale)
        }
    }

    /**
     * Sets the application's locale to the specified [locale].
     *
     * @param locale The [Locale] to set as the application's locale.
     * If `null`, the locale is reset to the default configuration (cleared).
     *
     * Usage:
     * ```
     * setAppLocale(Locale("en"))
     * setAppLocale(null) // Reset to default
     * ```
     */
    private suspend fun setAppLocale(locale: Locale?) {
        withContext(Dispatchers.Main) {
            // temporarily allow disk reads and writes,
            // since setApplicationLocales is a blocking operation;
            // (returns old policy to restore afterwards)
            val oldPolicy = StrictMode.allowThreadDiskWrites()
            val localeList = if (locale == null) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(locale.toLanguageTag())
            }
            AppCompatDelegate.setApplicationLocales(localeList)
            // restore old policy
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }
}
