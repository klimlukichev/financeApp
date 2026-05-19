package ru.rsreu.klimlukichev.financeapp.domain.categorization

import ru.rsreu.klimlukichev.financeapp.domain.model.Category
import java.util.Locale

class TransactionCategorizer {

    fun categorize(
        text: String,
        bankCategory: String?,
        categories: List<Category>,
        keywordCategoryMap: Map<String, String>,
    ): Category? {
        val normalizedText = text.normalizedForCategorization()
        val normalizedBankCategory = bankCategory.orEmpty().normalizedForCategorization()
        val merchant = merchantKey(text)
        if (normalizedText.isBlank() && normalizedBankCategory.isBlank()) {
            return null
        }

        val scores = mutableMapOf<String, Int>()
        BANK_CATEGORY_HINTS.forEach { (bankCategoryHint, categoryName) ->
            if (normalizedBankCategory.contains(bankCategoryHint)) {
                scores.addScore(categoryName, BANK_CATEGORY_SCORE + bankCategoryHint.length)
            }
        }

        keywordCategoryMap.forEach { (keyword, categoryName) ->
            val normalizedKeyword = keyword.normalizedForCategorization()
            if (normalizedKeyword.isBlank()) return@forEach

            val score = when {
                merchant == normalizedKeyword -> EXACT_MERCHANT_SCORE
                merchant.contains(normalizedKeyword) -> MERCHANT_CONTAINS_SCORE + normalizedKeyword.length
                normalizedBankCategory.contains(normalizedKeyword) -> BANK_CATEGORY_SCORE + normalizedKeyword.length
                normalizedText.contains(normalizedKeyword) -> TEXT_CONTAINS_SCORE + normalizedKeyword.length
                else -> 0
            }

            if (score > 0) {
                scores.addScore(categoryName, score)
            }
        }

        val categoryByName = categories.associateBy { it.name.normalizedForCategorization() }
        return scores.entries
            .maxWithOrNull(compareBy<Map.Entry<String, Int>> { it.value }.thenBy { it.key.length })
            ?.key
            ?.normalizedForCategorization()
            ?.let(categoryByName::get)
    }

    fun merchantKey(text: String): String {
        val normalized = text.normalizedForCategorization()
            .replace(CARD_MASK_REGEX, " ")
            .replace(NUMBER_REGEX, " ")
            .replace(SERVICE_PHRASES_REGEX, " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val tokens = normalized
            .split(" ")
            .filter { token -> token.length > 1 && token !in STOP_WORDS }

        return tokens.take(MERCHANT_KEY_TOKEN_LIMIT).joinToString(" ")
            .ifBlank { normalized }
    }

    private fun MutableMap<String, Int>.addScore(categoryName: String, score: Int) {
        this[categoryName] = (this[categoryName] ?: 0) + score
    }

    companion object {
        fun String.normalizedForCategorization(): String =
            trim()
                .lowercase(Locale.forLanguageTag("ru-RU"))
                .replace('ё', 'е')
                .replace(Regex("[^a-zа-я0-9]+"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        private const val EXACT_MERCHANT_SCORE = 140
        private const val MERCHANT_CONTAINS_SCORE = 95
        private const val BANK_CATEGORY_SCORE = 75
        private const val TEXT_CONTAINS_SCORE = 45
        private const val MERCHANT_KEY_TOKEN_LIMIT = 4

        private val CARD_MASK_REGEX = Regex("""\*{2,}\d{2,4}""")
        private val NUMBER_REGEX = Regex("""\b\d{3,}\b""")
        private val SERVICE_PHRASES_REGEX = Regex(
            """\b(операция|карте|карта|оплата|покупка|перевод|rus|qr|p qr|sbp|сбп)\b""",
        )
        private val STOP_WORDS = setOf(
            "по",
            "на",
            "из",
            "за",
            "от",
            "в",
            "и",
            "ооо",
            "ип",
            "ao",
            "llc",
            "rus",
        )

        private val BANK_CATEGORY_HINTS = mapOf(
            "супермаркет" to "Еда",
            "фастфуд" to "Еда",
            "кафе" to "Еда",
            "ресторан" to "Еда",
            "транспорт" to "Транспорт",
            "такси" to "Транспорт",
            "топливо" to "Транспорт",
            "азс" to "Транспорт",
            "жкх" to "Жилье",
            "дом" to "Жилье",
            "аптек" to "Здоровье",
            "медицин" to "Здоровье",
            "одежд" to "Одежда",
            "маркетплейс" to "Маркетплейсы",
            "связь" to "Связь",
            "телеком" to "Связь",
            "подписк" to "Подписки",
            "развлеч" to "Развлечения",
            "кино" to "Развлечения",
            "путешеств" to "Путешествия",
            "отел" to "Путешествия",
            "образован" to "Образование",
            "красот" to "Красота",
        )
    }
}
