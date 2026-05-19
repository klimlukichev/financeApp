package ru.rsreu.klimlukichev.financeapp.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

internal val Context.themeSettingsDataStore by preferencesDataStore(name = "theme_settings")
