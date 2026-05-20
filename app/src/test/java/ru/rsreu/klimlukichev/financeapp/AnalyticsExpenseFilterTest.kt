package ru.rsreu.klimlukichev.financeapp

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.rsreu.klimlukichev.financeapp.domain.analytics.AnalyticsExpenseFilter
import ru.rsreu.klimlukichev.financeapp.domain.analytics.ScopedExpense
import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction
import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType
import java.time.LocalDate
import java.time.ZoneId

class AnalyticsExpenseFilterTest {

    private val zoneId = ZoneId.of("Europe/Moscow")
    private val filter = AnalyticsExpenseFilter()

    @Test
    fun `counts regular expense`() {
        val expense = scoped(
            id = 1,
            amount = 500.0,
            day = LocalDate.of(2026, 4, 23),
            categoryName = "Еда",
            note = "Пятёрочка",
            bank = "Сбер",
        )

        assertTrue(filter.isCountedInAnalytics(expense, listOf(expense), zoneId))
    }

    @Test
    fun `excludes transfer between own accounts by text`() {
        val expense = scoped(
            id = 1,
            amount = 1_000.0,
            day = LocalDate.of(2026, 4, 23),
            categoryName = "Переводы",
            note = "Перевод между счетами",
            bank = "Сбер",
        )

        assertFalse(filter.isCountedInAnalytics(expense, listOf(expense), zoneId))
    }

    @Test
    fun `counts transfer to another person`() {
        val expense = scoped(
            id = 1,
            amount = 1_000.0,
            day = LocalDate.of(2026, 4, 23),
            categoryName = "Переводы",
            note = "Перевод СБП Иванову И.И.",
            bank = "Сбер",
        )

        assertTrue(filter.isCountedInAnalytics(expense, listOf(expense), zoneId))
    }

    @Test
    fun `excludes middle leg of cross bank transfer chain`() {
        val day = LocalDate.of(2026, 4, 23)
        val bank1 = scoped(
            id = 1,
            amount = 1_000.0,
            day = day,
            categoryName = "Переводы",
            note = "Перевод СБП",
            bank = "Сбер",
        )
        val bank2 = scoped(
            id = 2,
            amount = 1_000.0,
            day = day,
            categoryName = "Переводы",
            note = "Перевод СБП",
            bank = "Т-Банк",
        )
        val bank3 = scoped(
            id = 3,
            amount = 1_000.0,
            day = day,
            categoryName = "Переводы",
            note = "Перевод СБП",
            bank = "ВТБ",
        )
        val period = listOf(bank1, bank2, bank3)

        assertTrue(filter.isCountedInAnalytics(bank1, period, zoneId))
        assertFalse(filter.isCountedInAnalytics(bank2, period, zoneId))
        assertFalse(filter.isCountedInAnalytics(bank3, period, zoneId))
    }

    private fun scoped(
        id: Long,
        amount: Double,
        day: LocalDate,
        categoryName: String,
        note: String,
        bank: String,
        categoryId: Long = 1,
    ): ScopedExpense {
        val millis = day.atStartOfDay(zoneId).toInstant().toEpochMilli()
        return ScopedExpense(
            transaction = Transaction(
                id = id,
                amount = amount,
                date = millis,
                categoryId = categoryId,
                note = note,
                type = TransactionType.EXPENSE,
                sourceBank = bank,
                sourceDescription = note,
            ),
            categoryName = categoryName,
        )
    }
}
