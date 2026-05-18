package ru.rsreu.klimlukichev.financeapp.domain.model

data class BudgetSettings(
    val weeklyBudgetLimit: Double = 0.0,
    val lastBudgetExceededWeek: String? = null,
)
