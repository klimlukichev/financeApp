package ru.rsreu.klimlukichev.financeapp.domain.usecase

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import ru.rsreu.klimlukichev.financeapp.domain.analytics.AnalyticsExpenseFilter
import ru.rsreu.klimlukichev.financeapp.domain.analytics.ScopedExpense
import ru.rsreu.klimlukichev.financeapp.domain.model.BudgetExceededInfo
import ru.rsreu.klimlukichev.financeapp.domain.repository.BudgetRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.TransactionRepository
import ru.rsreu.klimlukichev.financeapp.domain.util.DatePeriodFactory
import java.time.ZoneId

class CheckWeeklyBudgetUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val analyticsExpenseFilter: AnalyticsExpenseFilter,
) {

    suspend operator fun invoke(
        referenceTimeMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): BudgetExceededInfo? {
        val settings = budgetRepository.observeSettings().first()
        if (settings.weeklyBudgetLimit <= 0.0) return null

        val weekKey = DatePeriodFactory.weekKey(referenceTimeMillis, zoneId)
        if (settings.lastBudgetExceededWeek == weekKey) return null

        val period = DatePeriodFactory.weekOf(referenceTimeMillis, zoneId)
        val spentAmount = combine(
            transactionRepository.observeByPeriod(
                startDate = period.startInclusive,
                endDate = period.endInclusive,
            ),
            categoryRepository.observeAll(),
        ) { transactions, categories ->
            val categoryNameById = categories.associate { it.id to it.name }
            val scoped = transactions.map { transaction ->
                ScopedExpense(
                    transaction = transaction,
                    categoryName = categoryNameById[transaction.categoryId].orEmpty(),
                )
            }
            analyticsExpenseFilter.filterCounted(scoped, zoneId).sumOf { it.transaction.amount }
        }.first()

        if (spentAmount <= settings.weeklyBudgetLimit) return null

        budgetRepository.markBudgetExceededNotified(weekKey)
        return BudgetExceededInfo(
            spentAmount = spentAmount,
            budgetLimit = settings.weeklyBudgetLimit,
        )
    }
}
