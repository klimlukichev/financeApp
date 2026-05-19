package ru.rsreu.klimlukichev.financeapp.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object ThemeSettingsPreferences {
    const val DEFAULT_LANGUAGE_TAG = "ru"
    val SUPPORTED_LANGUAGE_TAGS = setOf("ru", "en")
    val DARK_THEME_ENABLED = booleanPreferencesKey("dark_theme_enabled")
    val LANGUAGE_TAG = stringPreferencesKey("language_tag")
}
