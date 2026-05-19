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
            "globus" to "Еда",
            "глобус" to "Еда",
            "перекресток" to "Еда",
            "перекрест" to "Еда",
            "пятерочка" to "Еда",
            "pyaterochka" to "Еда",
            "магнит" to "Еда",
            "dixy" to "Еда",
            "дикси" to "Еда",
            "лента" to "Еда",
            "ашан" to "Еда",
            "auchan" to "Еда",
            "вкусвилл" to "Еда",
            "vkusvill" to "Еда",
            "самокат" to "Еда",
            "lavka" to "Еда",
            "yandex lavka" to "Еда",
            "кафе" to "Еда",
            "кофе" to "Еда",
            "coffee" to "Еда",
            "ресторан" to "Еда",
            "burger" to "Еда",
            "kfc" to "Еда",
            "vkusno" to "Еда",
            "продукты" to "Еда",
            "магазин" to "Еда",
            "супермаркет" to "Еда",
            "супермаркеты" to "Еда",
            "такси" to "Транспорт",
            "taxi" to "Транспорт",
            "яндекс такси" to "Транспорт",
            "yandex go" to "Транспорт",
            "метро" to "Транспорт",
            "автобус" to "Транспорт",
            "тройка" to "Транспорт",
            "transport" to "Транспорт",
            "whoosh" to "Транспорт",
            "azs" to "Транспорт",
            "азс" to "Транспорт",
            "лукойл" to "Транспорт",
            "lukoil" to "Транспорт",
            "бензин" to "Транспорт",
            "аренда" to "Жильё",
            "квартира" to "Жильё",
            "жкх" to "Жильё",
            "коммун" to "Жильё",
            "домофон" to "Жильё",
            "кино" to "Развлечения",
            "игры" to "Развлечения",
            "steam" to "Развлечения",
            "playstation" to "Развлечения",
            "xbox" to "Развлечения",
            "kinopoisk" to "Развлечения",
            "кинотеатр" to "Развлечения",
            "театр" to "Развлечения",
            "аптека" to "Здоровье",
            "аптек" to "Здоровье",
            "pharmacy" to "Здоровье",
            "gorzdrav" to "Здоровье",
            "здрав" to "Здоровье",
            "клиника" to "Здоровье",
            "медицин" to "Здоровье",
            "ozon" to "Маркетплейсы",
            "озон" to "Маркетплейсы",
            "wildberries" to "Маркетплейсы",
            "wb" to "Маркетплейсы",
            "яндекс маркет" to "Маркетплейсы",
            "market" to "Маркетплейсы",
            "aliexpress" to "Маркетплейсы",
            "одежда" to "Одежда",
            "обувь" to "Одежда",
            "lamoda" to "Одежда",
            "zara" to "Одежда",
            "befree" to "Одежда",
            "спортмастер" to "Одежда",
            "tele2" to "Связь",
            "теле2" to "Связь",
            "mts" to "Связь",
            "мтс" to "Связь",
            "megafon" to "Связь",
            "мегафон" to "Связь",
            "beeline" to "Связь",
            "билайн" to "Связь",
            "интернет" to "Связь",
            "netflix" to "Подписки",
            "spotify" to "Подписки",
            "yandex plus" to "Подписки",
            "яндекс плюс" to "Подписки",
            "okko" to "Подписки",
            "ivi" to "Подписки",
            "wink" to "Подписки",
            "booking" to "Путешествия",
            "hotel" to "Путешествия",
            "отель" to "Путешествия",
            "аэрофлот" to "Путешествия",
            "rzd" to "Путешествия",
            "ржд" to "Путешествия",
            "авиабилеты" to "Путешествия",
            "университет" to "Образование",
            "курс" to "Образование",
            "школа" to "Образование",
            "книги" to "Образование",
            "салон" to "Красота",
            "барбер" to "Красота",
            "парикмах" to "Красота",
            "космет" to "Красота",
            "перевод" to "Переводы",
            "сбп" to "Переводы",
        )
    }
}
