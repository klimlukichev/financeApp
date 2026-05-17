package ru.rsreu.klimlukichev.financeapp.domain.usecase

import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction
import ru.rsreu.klimlukichev.financeapp.domain.repository.TransactionRepository

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository,
) {

    suspend operator fun invoke(transaction: Transaction): Long {
        require(transaction.amount != 0.0) { "Сумма транзакции не может быть нулевой" }
        require(transaction.categoryId > 0) { "Категория должна быть выбрана" }
        require(transaction.date > 0) { "Дата транзакции обязательна" }
        return transactionRepository.insert(transaction)
    }
}
