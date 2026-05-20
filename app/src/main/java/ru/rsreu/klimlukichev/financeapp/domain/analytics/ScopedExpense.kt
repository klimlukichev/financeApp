package ru.rsreu.klimlukichev.financeapp.domain.analytics

import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction

data class ScopedExpense(
    val transaction: Transaction,
    val categoryName: String,
)
