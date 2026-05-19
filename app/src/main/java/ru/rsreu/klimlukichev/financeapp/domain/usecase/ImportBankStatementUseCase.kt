package ru.rsreu.klimlukichev.financeapp.domain.usecase

import kotlinx.coroutines.flow.first
import ru.rsreu.klimlukichev.financeapp.domain.importing.BankStatementImportRepository
import ru.rsreu.klimlukichev.financeapp.domain.importing.BankStatementImportResult
import ru.rsreu.klimlukichev.financeapp.domain.importing.BankStatementTransaction
import ru.rsreu.klimlukichev.financeapp.domain.model.Category
import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction
import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.TransactionRepository
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale

class ImportBankStatementUseCase(
    private val importRepository: BankStatementImportRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val categorizeByKeywordsUseCase: CategorizeByKeywordsUseCase,
) {

    suspend operator fun invoke(inputStream: InputStream): BankStatementImportResult {
        val statementTransactions = importRepository.parse(inputStream)
            .filter { it.type == TransactionType.EXPENSE }
        val categories = categoryRepository.observeAll().first()
        val fallbackCategory = categories.firstOrNull { it.name.equals(OTHER_CATEGORY_NAME, ignoreCase = true) }
            ?: categories.firstOrNull()
            ?: error("Нет категорий для импорта")

        val transactions = statementTransactions.mapIndexed { index, statementTransaction ->
            val category = chooseCategory(statementTransaction, categories) ?: fallbackCategory
            statementTransaction.toTransaction(category.id, index)
        }

        val insertResult = transactionRepository.insertAllIgnore(transactions)
        val importedCount = insertResult.count { it != IGNORED_INSERT_ID }
        return BankStatementImportResult(
            parsedCount = statementTransactions.size,
            importedCount = importedCount,
            duplicateCount = statementTransactions.size - importedCount,
        )
    }

    private suspend fun chooseCategory(
        statementTransaction: BankStatementTransaction,
        categories: List<Category>,
    ): Category? {
        val searchableText = listOfNotNull(
            statementTransaction.bankCategory,
            statementTransaction.description,
        ).joinToString(" ")

        return categorizeByKeywordsUseCase(
            text = searchableText,
            categories = categories,
            bankCategory = statementTransaction.bankCategory,
        ).first()
            ?: categories.findByName(inferCategoryName(searchableText))
    }

    private fun BankStatementTransaction.toTransaction(categoryId: Long, indexInStatement: Int): Transaction =
        Transaction(
            amount = amount,
            date = date,
            categoryId = categoryId,
            note = description,
            type = type,
            sourceBank = bank,
            sourceDescription = description,
            importHash = importHash(indexInStatement),
        )

    private fun BankStatementTransaction.importHash(indexInStatement: Int): String {
        val raw = listOf(
            bank.normalized(),
            date.toString(),
            amount.toString(),
            type.name,
            description.normalized(),
            indexInStatement.toString(),
        ).joinToString("|")
        return MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun List<Category>.findByName(name: String?): Category? =
        name?.let { targetName -> firstOrNull { it.name.equals(targetName, ignoreCase = true) } }

    private fun inferCategoryName(text: String): String? {
        val normalized = text.normalized()
        return when {
            FOOD_KEYWORDS.any { normalized.contains(it) } -> "Еда"
            TRANSPORT_KEYWORDS.any { normalized.contains(it) } -> "Транспорт"
            HOME_KEYWORDS.any { normalized.contains(it) } -> "Жильё"
            ENTERTAINMENT_KEYWORDS.any { normalized.contains(it) } -> "Развлечения"
            else -> null
        }
    }

    private fun String.normalized(): String =
        trim()
            .lowercase(Locale.forLanguageTag("ru-RU"))
            .replace(Regex("\\s+"), " ")

    private companion object {
        const val OTHER_CATEGORY_NAME = "Прочее"
        const val IGNORED_INSERT_ID = -1L

        val FOOD_KEYWORDS = listOf(
            "супермаркет",
            "пятероч",
            "перекрест",
            "магнит",
            "глобус",
            "продукт",
            "пекарн",
            "ресторан",
            "tomato",
            "yaponomama",
        )

        val TRANSPORT_KEYWORDS = listOf(
            "транспорт",
            "такси",
            "bilet",
            "tpp",
            "whoosh",
            "blablacar",
            "блабла",
        )

        val HOME_KEYWORDS = listOf("жиль", "квартир", "коммун")
        val ENTERTAINMENT_KEYWORDS = listOf("развлеч", "кино", "игры")
    }
}
