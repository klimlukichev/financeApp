package ru.rsreu.klimlukichev.financeapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.rsreu.klimlukichev.financeapp.domain.model.Category
import ru.rsreu.klimlukichev.financeapp.domain.model.CategorySpending
import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository
import ru.rsreu.klimlukichev.financeapp.domain.usecase.AddTransactionUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.GetCategorySpendingUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.GetLastTransactionsUseCase

class HomeViewModel(
    private val getLastTransactionsUseCase: GetLastTransactionsUseCase,
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val isAddDialogVisible = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        getLastTransactionsUseCase(limit = TRANSACTION_LIMIT),
        getCategorySpendingUseCase(),
        categoryRepository.observeAll(),
        isAddDialogVisible,
    ) { transactions, spending, categories, dialogVisible ->
        HomeUiState(
            transactions = transactions.toTransactionItems(categories),
            categoryStats = spending.toCategoryStats(categories),
            categories = categories,
            isAddDialogVisible = dialogVisible,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun onAddClick() {
        isAddDialogVisible.update { true }
    }

    fun onDismissAddDialog() {
        isAddDialogVisible.update { false }
    }

    fun onSaveTransaction(amount: Double, categoryId: Long, note: String?) {
        viewModelScope.launch {
            runCatching {
                addTransactionUseCase(
                    Transaction(
                        amount = amount,
                        date = System.currentTimeMillis(),
                        categoryId = categoryId,
                        note = note?.takeIf { it.isNotBlank() },
                    ),
                )
            }.onSuccess {
                isAddDialogVisible.update { false }
            }
        }
    }

    private fun List<Transaction>.toTransactionItems(categories: List<Category>): List<TransactionItemUi> =
        map { transaction ->
            val category = categories.find { it.id == transaction.categoryId }
            TransactionItemUi(
                id = transaction.id,
                amount = transaction.amount,
                date = transaction.date,
                categoryName = category?.name ?: "—",
                note = transaction.note,
                colorInt = category?.colorInt ?: DEFAULT_COLOR,
            )
        }

    private fun List<CategorySpending>.toCategoryStats(
        categories: List<Category>,
    ): List<CategoryStat> =
        map { spending ->
            val category = categories.find { it.id == spending.categoryId }
            CategoryStat(
                categoryId = spending.categoryId,
                categoryName = spending.categoryName,
                totalAmount = spending.totalAmount,
                colorInt = category?.colorInt ?: DEFAULT_COLOR,
            )
        }.filter { it.totalAmount > 0.0 }

    companion object {
        const val TRANSACTION_LIMIT = 10
        private const val DEFAULT_COLOR = 0xFF9E9E9E.toInt()
    }
}
