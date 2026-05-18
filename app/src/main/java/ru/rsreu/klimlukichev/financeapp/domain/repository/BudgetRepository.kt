package ru.rsreu.klimlukichev.financeapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.rsreu.klimlukichev.financeapp.domain.model.BudgetSettings

interface BudgetRepository {

    fun observeSettings(): Flow<BudgetSettings>

    suspend fun updateWeeklyBudgetLimit(limit: Double)

    suspend fun markBudgetExceededNotified(weekKey: String)
}
