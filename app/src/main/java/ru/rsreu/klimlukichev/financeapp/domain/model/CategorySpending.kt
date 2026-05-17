package ru.rsreu.klimlukichev.financeapp.domain.model

data class CategorySpending(
    val categoryId: Long,
    val categoryName: String,
    val totalAmount: Double,
)
