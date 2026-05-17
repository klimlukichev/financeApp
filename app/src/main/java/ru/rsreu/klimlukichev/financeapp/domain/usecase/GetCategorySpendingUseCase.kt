package ru.rsreu.klimlukichev.financeapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.rsreu.klimlukichev.financeapp.domain.model.CategorySpending
import ru.rsreu.klimlukichev.financeapp.domain.repository.TransactionRepository
import ru.rsreu.klimlukichev.financeapp.domain.util.DatePeriodFactory
import java.time.ZoneId

class GetCategorySpendingUseCase(
    private val transactionRepository: TransactionRepository,
) {

    /** Расходы по категориям за календарный месяц [year]-[month]. */
    operator fun invoke(
        year: Int,
        month: Int,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Flow<List<CategorySpending>> {
        val period = DatePeriodFactory.monthOf(year, month, zoneId)
        return transactionRepository.observeCategorySpending(
            startDate = period.startInclusive,
            endDate = period.endInclusive,
        )
    }

    /** Расходы по категориям за месяц, в который попадает [referenceTimeMillis]. */
    operator fun invoke(
        referenceTimeMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Flow<List<CategorySpending>> {
        val period = DatePeriodFactory.monthOf(referenceTimeMillis, zoneId)
        return transactionRepository.observeCategorySpending(
            startDate = period.startInclusive,
            endDate = period.endInclusive,
        )
    }
}
