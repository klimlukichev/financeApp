package ru.rsreu.klimlukichev.financeapp.ui.home

import ru.rsreu.klimlukichev.financeapp.domain.model.Category
import java.time.YearMonth

data class HomeUiState(
    val transactions: List<TransactionItemUi> = emptyList(),
    val categoryStats: List<CategoryStat> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val canNavigateNextMonth: Boolean = false,
    val weeklyBudgetLimit: Double = 0.0,
    val weeklyBudgetInput: String = "",
    val isWeeklyBudgetSaving: Boolean = false,
    val isAddDialogVisible: Boolean = false,
    val transactionInDialog: TransactionItemUi? = null,
    val suggestedCategory: Category? = null,
    val isLoading: Boolean = true,
)
