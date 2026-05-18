package ru.rsreu.klimlukichev.financeapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.rsreu.klimlukichev.financeapp.domain.model.BudgetSettings
import ru.rsreu.klimlukichev.financeapp.domain.repository.BudgetRepository

class DataStoreBudgetRepository(
    private val dataStore: DataStore<Preferences>,
) : BudgetRepository {

    override fun observeSettings(): Flow<BudgetSettings> =
        dataStore.data.map { preferences ->
            BudgetSettings(
                weeklyBudgetLimit = preferences[WEEKLY_BUDGET_LIMIT] ?: 0.0,
                lastBudgetExceededWeek = preferences[LAST_BUDGET_EXCEEDED_WEEK],
            )
        }

    override suspend fun updateWeeklyBudgetLimit(limit: Double) {
        dataStore.edit { preferences ->
            preferences[WEEKLY_BUDGET_LIMIT] = limit.coerceAtLeast(0.0)
        }
    }

    override suspend fun markBudgetExceededNotified(weekKey: String) {
        dataStore.edit { preferences ->
            preferences[LAST_BUDGET_EXCEEDED_WEEK] = weekKey
        }
    }

    private companion object {
        val WEEKLY_BUDGET_LIMIT = doublePreferencesKey("weekly_budget_limit")
        val LAST_BUDGET_EXCEEDED_WEEK = stringPreferencesKey("last_budget_exceeded_week")
    }
}
