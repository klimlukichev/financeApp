package ru.rsreu.klimlukichev.financeapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.rsreu.klimlukichev.financeapp.domain.categorization.TransactionCategorizer
import ru.rsreu.klimlukichev.financeapp.domain.model.Category

class TransactionCategorizerTest {

    private val categorizer = TransactionCategorizer()
    private val categories = listOf(
        Category(name = "Еда", iconResId = 0, colorInt = 0, isDefault = true),
        Category(name = "Транспорт", iconResId = 0, colorInt = 0, isDefault = true),
        Category(name = "Здоровье", iconResId = 0, colorInt = 0, isDefault = true),
        Category(name = "Маркетплейсы", iconResId = 0, colorInt = 0, isDefault = true),
        Category(name = "Связь", iconResId = 0, colorInt = 0, isDefault = true),
        Category(name = "Переводы", iconResId = 0, colorInt = 0, isDefault = true),
        Category(name = "Прочее", iconResId = 0, colorInt = 0, isDefault = true),
    )
    private val rules = mapOf(
        "globus" to "Еда",
        "ozon" to "Маркетплейсы",
        "tele2" to "Связь",
        "аптека" to "Здоровье",
        "whoosh" to "Транспорт",
        "сбп" to "Переводы",
        "перевод" to "Переводы",
    )

    @Test
    fun `uses bank category and merchant text for supermarket expense`() {
        val category = categorizer.categorize(
            text = "GLOBUS TULA_P_QR OSINOVAYA GOR RUS. Операция по карте",
            bankCategory = "Супермаркеты",
            categories = categories,
            keywordCategoryMap = rules,
        )

        assertEquals("Еда", category?.name)
    }

    @Test
    fun `detects marketplace and mobile operators from merchant`() {
        val marketplaceCategory = categorizer.categorize(
            text = "OZON BANK интернет-магазин",
            bankCategory = null,
            categories = categories,
            keywordCategoryMap = rules,
        )
        val mobileCategory = categorizer.categorize(
            text = "TELE2 RUSSIA пополнение связи",
            bankCategory = null,
            categories = categories,
            keywordCategoryMap = rules,
        )

        assertEquals("Маркетплейсы", marketplaceCategory?.name)
        assertEquals("Связь", mobileCategory?.name)
    }

    @Test
    fun `uses learned merchant correction with highest priority`() {
        val learnedRules = rules + (categorizer.merchantKey("WHOOSH MOSCOW RUS") to "Транспорт")

        val category = categorizer.categorize(
            text = "WHOOSH MOSCOW RUS",
            bankCategory = "Развлечения",
            categories = categories,
            keywordCategoryMap = learnedRules,
        )

        assertEquals("Транспорт", category?.name)
    }

    @Test
    fun `does not categorize as transfers by sbp alone`() {
        val category = categorizer.categorize(
            text = "Оплата СБП QR в кафе",
            bankCategory = null,
            categories = categories,
            keywordCategoryMap = rules,
        )

        assertNull(category)
    }

    @Test
    fun `categorizes as transfers when operation text contains transfer word`() {
        val category = categorizer.categorize(
            text = "Перевод СБП Иванову И И",
            bankCategory = null,
            categories = categories,
            keywordCategoryMap = rules,
        )

        assertEquals("Переводы", category?.name)
    }
}
