package ru.rsreu.klimlukichev.financeapp.domain.model

data class BudgetExceededInfo(
    val spentAmount: Double,
    val budgetLimit: Double,
)
