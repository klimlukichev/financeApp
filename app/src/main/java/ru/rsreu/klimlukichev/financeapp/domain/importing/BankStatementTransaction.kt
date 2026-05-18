package ru.rsreu.klimlukichev.financeapp.domain.importing

import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType

data class BankStatementTransaction(
    val bank: String,
    val date: Long,
    val amount: Double,
    val type: TransactionType,
    val description: String,
    val bankCategory: String? = null,
)
