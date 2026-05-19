package ru.rsreu.klimlukichev.financeapp.domain.usecase

import kotlinx.coroutines.flow.first
import ru.rsreu.klimlukichev.financeapp.domain.categorization.TransactionCategorizer
import ru.rsreu.klimlukichev.financeapp.domain.repository.KeywordCategoryRepository

class RememberCategoryCorrectionUseCase(
    private val keywordCategoryRepository: KeywordCategoryRepository,
    private val transactionCategorizer: TransactionCategorizer,
) {

    suspend operator fun invoke(
        sourceDescription: String?,
        note: String?,
        categoryName: String,
    ) {
        val sourceText = sourceDescription?.takeIf { it.isNotBlank() }
            ?: note?.takeIf { it.isNotBlank() }
            ?: return
        val merchantKey = transactionCategorizer.merchantKey(sourceText)
        if (merchantKey.isBlank()) return

        val currentMap = keywordCategoryRepository.observeKeywordCategoryMap().first()
        keywordCategoryRepository.saveKeywordCategoryMap(
            currentMap + (merchantKey to categoryName),
        )
    }
}
