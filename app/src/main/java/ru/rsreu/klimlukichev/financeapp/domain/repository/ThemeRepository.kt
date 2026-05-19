package ru.rsreu.klimlukichev.financeapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {

    fun observeDarkThemeEnabled(): Flow<Boolean>

    suspend fun setDarkThemeEnabled(enabled: Boolean)

    fun observeLanguageTag(): Flow<String>

    suspend fun setLanguageTag(languageTag: String)
}
