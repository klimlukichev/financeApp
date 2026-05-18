package ru.rsreu.klimlukichev.financeapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.rsreu.klimlukichev.financeapp.domain.model.Category
import ru.rsreu.klimlukichev.financeapp.domain.repository.KeywordCategoryRepository
import java.util.Locale

class CategorizeByKeywordsUseCase(
    private val keywordCategoryRepository: KeywordCategoryRepository,
) {

    operator fun invoke(
        text: String,
        categories: List<Category>,
    ): Flow<Category?> =
        keywordCategoryRepository.observeKeywordCategoryMap().map { keywordCategoryMap ->
            val normalizedText = text.normalizedForSearch()
            if (normalizedText.isBlank()) {
                return@map null
            }

            val categoryName = keywordCategoryMap.entries
                .firstOrNull { (keyword) -> normalizedText.contains(keyword.normalizedForSearch()) }
                ?.value

            categories.firstOrNull { category ->
                category.name.equals(categoryName, ignoreCase = true)
            }
        }

    private fun String.normalizedForSearch(): String =
        trim().lowercase(Locale.forLanguageTag("ru-RU"))
}
