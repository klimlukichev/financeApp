package ru.rsreu.klimlukichev.financeapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.rsreu.klimlukichev.financeapp.domain.repository.KeywordCategoryRepository

class DataStoreKeywordCategoryRepository(
    private val dataStore: DataStore<Preferences>,
) : KeywordCategoryRepository {

    override fun observeKeywordCategoryMap(): Flow<Map<String, String>> =
        dataStore.data.map { preferences ->
            preferences[KEYWORD_CATEGORY_MAP_KEY]
                ?.toKeywordCategoryMap()
                ?.takeIf { it.isNotEmpty() }
                ?: DEFAULT_KEYWORD_CATEGORY_MAP
        }

    override suspend fun saveKeywordCategoryMap(keywordCategoryMap: Map<String, String>) {
        dataStore.edit { preferences ->
            preferences[KEYWORD_CATEGORY_MAP_KEY] = keywordCategoryMap.toPreferenceEntries()
        }
    }

    private fun Set<String>.toKeywordCategoryMap(): Map<String, String> =
        mapNotNull { entry ->
            val parts = entry.split(ENTRY_SEPARATOR, limit = 2)
            val keyword = parts.getOrNull(0)?.trim().orEmpty()
            val categoryName = parts.getOrNull(1)?.trim().orEmpty()
            if (keyword.isBlank() || categoryName.isBlank()) {
                null
            } else {
                keyword to categoryName
            }
        }.toMap()

    private fun Map<String, String>.toPreferenceEntries(): Set<String> =
        entries
            .filter { (keyword, categoryName) -> keyword.isNotBlank() && categoryName.isNotBlank() }
            .map { (keyword, categoryName) -> "${keyword.trim()}$ENTRY_SEPARATOR${categoryName.trim()}" }
            .toSet()

    private companion object {
        const val ENTRY_SEPARATOR = "\t"

        val KEYWORD_CATEGORY_MAP_KEY = stringSetPreferencesKey("keyword_category_map")

        val DEFAULT_KEYWORD_CATEGORY_MAP = mapOf(
            "кафе" to "Еда",
            "кофе" to "Еда",
            "ресторан" to "Еда",
            "продукты" to "Еда",
            "магазин" to "Еда",
            "такси" to "Транспорт",
            "метро" to "Транспорт",
            "автобус" to "Транспорт",
            "аренда" to "Жильё",
            "квартира" to "Жильё",
            "кино" to "Развлечения",
            "игры" to "Развлечения",
        )
    }
}
