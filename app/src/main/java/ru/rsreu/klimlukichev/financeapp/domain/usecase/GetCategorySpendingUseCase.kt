package ru.rsreu.klimlukichev.financeapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.rsreu.klimlukichev.financeapp.domain.analytics.AnalyticsExpenseFilter
import ru.rsreu.klimlukichev.financeapp.domain.analytics.CategorySpendingAggregator
import ru.rsreu.klimlukichev.financeapp.domain.analytics.ScopedExpense
import ru.rsreu.klimlukichev.financeapp.domain.model.CategorySpending
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.TransactionRepository
import ru.rsreu.klimlukichev.financeapp.domain.util.DatePeriodFactory
import java.time.ZoneId

class GetCategorySpendingUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val analyticsExpenseFilter: AnalyticsExpenseFilter,
) {

    /** Расходы по категориям за календарный месяц [year]-[month] (без внутренних переводов). */
    operator fun invoke(
        year: Int,
        month: Int,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Flow<List<CategorySpending>> {
        val period = DatePeriodFactory.monthOf(year, month, zoneId)
        return observeAnalyticsSpending(
            startDate = period.startInclusive,
            endDate = period.endInclusive,
            zoneId = zoneId,
        )
    }

    /** Расходы по категориям за месяц, в который попадает [referenceTimeMillis]. */
    operator fun invoke(
        referenceTimeMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Flow<List<CategorySpending>> {
        val period = DatePeriodFactory.monthOf(referenceTimeMillis, zoneId)
        return observeAnalyticsSpending(
            startDate = period.startInclusive,
            endDate = period.endInclusive,
            zoneId = zoneId,
        )
    }

    private fun observeAnalyticsSpending(
        startDate: Long,
        endDate: Long,
        zoneId: ZoneId,
    ): Flow<List<CategorySpending>> =
        combine(
            transactionRepository.observeByPeriod(startDate, endDate),
            categoryRepository.observeAll(),
        ) { transactions, categories ->
            val categoryNameById = categories.associate { it.id to it.name }
            val scoped = transactions.map { transaction ->
                ScopedExpense(
                    transaction = transaction,
                    categoryName = categoryNameById[transaction.categoryId].orEmpty(),
                )
            }
            val counted = analyticsExpenseFilter.filterCounted(scoped, zoneId)
            CategorySpendingAggregator.aggregate(counted, categories)
        }
}
