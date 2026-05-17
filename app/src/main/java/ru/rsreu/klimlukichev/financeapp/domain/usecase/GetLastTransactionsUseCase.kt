package ru.rsreu.klimlukichev.financeapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction
import ru.rsreu.klimlukichev.financeapp.domain.repository.TransactionRepository

class GetLastTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
) {

    /**
     * @param limit максимальное число последних транзакций (по дате, убывание)
     */
    operator fun invoke(limit: Int = DEFAULT_LIMIT): Flow<List<Transaction>> {
        require(limit > 0) { "Лимит должен быть больше нуля" }
        return transactionRepository.observeRecent(limit)
    }

    companion object {
        const val DEFAULT_LIMIT = 20
    }
}
