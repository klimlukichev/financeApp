package ru.rsreu.klimlukichev.financeapp.domain.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val date: Long,
    val categoryId: Long,
    val note: String? = null,
)
