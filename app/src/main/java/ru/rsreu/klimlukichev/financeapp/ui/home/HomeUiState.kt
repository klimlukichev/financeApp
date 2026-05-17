package ru.rsreu.klimlukichev.financeapp.ui.home

import ru.rsreu.klimlukichev.financeapp.domain.model.Category

data class HomeUiState(
    val transactions: List<TransactionItemUi> = emptyList(),
    val categoryStats: List<CategoryStat> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isAddDialogVisible: Boolean = false,
    val isLoading: Boolean = true,
)
