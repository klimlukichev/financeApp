package ru.rsreu.klimlukichev.financeapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.rsreu.klimlukichev.financeapp.data.local.ThemeSettingsPreferences
import ru.rsreu.klimlukichev.financeapp.domain.repository.ThemeRepository

class DataStoreThemeRepository(
    private val dataStore: DataStore<Preferences>,
) : ThemeRepository {

    override fun observeDarkThemeEnabled(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[ThemeSettingsPreferences.DARK_THEME_ENABLED] ?: false
        }

    override suspend fun setDarkThemeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ThemeSettingsPreferences.DARK_THEME_ENABLED] = enabled
        }
    }

    override fun observeLanguageTag(): Flow<String> =
        dataStore.data.map { preferences ->
            preferences[ThemeSettingsPreferences.LANGUAGE_TAG]
                ?: ThemeSettingsPreferences.DEFAULT_LANGUAGE_TAG
        }

    override suspend fun setLanguageTag(languageTag: String) {
        dataStore.edit { preferences ->
            preferences[ThemeSettingsPreferences.LANGUAGE_TAG] =
                languageTag.takeIf { it in ThemeSettingsPreferences.SUPPORTED_LANGUAGE_TAGS }
                    ?: ThemeSettingsPreferences.DEFAULT_LANGUAGE_TAG
        }
    }
}
