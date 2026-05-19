package ru.rsreu.klimlukichev.financeapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.rsreu.klimlukichev.financeapp.domain.categorization.TransactionCategorizer
import ru.rsreu.klimlukichev.financeapp.domain.model.Category
import ru.rsreu.klimlukichev.financeapp.domain.repository.KeywordCategoryRepository

class CategorizeByKeywordsUseCase(
    private val keywordCategoryRepository: KeywordCategoryRepository,
    private val transactionCategorizer: TransactionCategorizer,
) {

    operator fun invoke(
        text: String,
        categories: List<Category>,
        bankCategory: String? = null,
    ): Flow<Category?> =
        keywordCategoryRepository.observeKeywordCategoryMap().map { keywordCategoryMap ->
            transactionCategorizer.categorize(
                text = text,
                bankCategory = bankCategory,
                categories = categories,
                keywordCategoryMap = keywordCategoryMap,
            )
        }
}
