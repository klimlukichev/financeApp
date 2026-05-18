package ru.rsreu.klimlukichev.financeapp.domain.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val date: Long,
    val categoryId: Long,
    val note: String? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val sourceBank: String? = null,
    val sourceDescription: String? = null,
    val importHash: String? = null,
)
