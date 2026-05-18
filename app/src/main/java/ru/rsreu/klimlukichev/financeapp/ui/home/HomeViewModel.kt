package ru.rsreu.klimlukichev.financeapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rsreu.klimlukichev.financeapp.domain.model.Category
import ru.rsreu.klimlukichev.financeapp.domain.model.CategorySpending
import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction
import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType
import ru.rsreu.klimlukichev.financeapp.domain.repository.BudgetRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository
import ru.rsreu.klimlukichev.financeapp.domain.usecase.AddTransactionUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.CheckWeeklyBudgetUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.CategorizeByKeywordsUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.ExportTransactionsUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.GetCategorySpendingUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.GetLastTransactionsUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.ImportBankStatementUseCase
import ru.rsreu.klimlukichev.financeapp.notifications.FinanceNotificationManager
import java.io.InputStream
import java.io.OutputStream
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getLastTransactionsUseCase: GetLastTransactionsUseCase,
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val categoryRepository: CategoryRepository,
    private val categorizeByKeywordsUseCase: CategorizeByKeywordsUseCase,
    private val exportTransactionsUseCase: ExportTransactionsUseCase,
    private val budgetRepository: BudgetRepository,
    private val checkWeeklyBudgetUseCase: CheckWeeklyBudgetUseCase,
    private val notificationManager: FinanceNotificationManager,
    private val importBankStatementUseCase: ImportBankStatementUseCase,
) : ViewModel() {

    private val isAddDialogVisible = MutableStateFlow(false)
    private val transactionInDialog = MutableStateFlow<TransactionItemUi?>(null)
    private val noteForCategorySuggestion = MutableStateFlow("")
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val weeklyBudgetInput = MutableStateFlow<String?>(null)
    private val isWeeklyBudgetSaving = MutableStateFlow(false)
    private val _exportRequests = MutableSharedFlow<ExportDocumentRequest>()
    private val _exportMessages = MutableSharedFlow<ExportMessage>()
    private val _budgetMessages = MutableSharedFlow<BudgetMessage>()
    private val _importRequests = MutableSharedFlow<ImportDocumentRequest>()
    private val _importMessages = MutableSharedFlow<ImportMessage>()
    private var pendingExportMonth: YearMonth? = null

    val exportRequests: SharedFlow<ExportDocumentRequest> = _exportRequests.asSharedFlow()
    val exportMessages: SharedFlow<ExportMessage> = _exportMessages.asSharedFlow()
    val budgetMessages: SharedFlow<BudgetMessage> = _budgetMessages.asSharedFlow()
    val importRequests: SharedFlow<ImportDocumentRequest> = _importRequests.asSharedFlow()
    val importMessages: SharedFlow<ImportMessage> = _importMessages.asSharedFlow()

    private val selectedMonthSpending = selectedMonth.flatMapLatest { month ->
        getCategorySpendingUseCase(year = month.year, month = month.monthValue)
            .map { spending -> month to spending }
    }

    private val budgetUiState = combine(
        budgetRepository.observeSettings(),
        weeklyBudgetInput,
        isWeeklyBudgetSaving,
    ) { settings, input, isSaving ->
        BudgetUiState(
            weeklyBudgetLimit = settings.weeklyBudgetLimit,
            weeklyBudgetInput = input ?: settings.weeklyBudgetLimit.toInputText(),
            isWeeklyBudgetSaving = isSaving,
        )
    }

    private val dialogState = combine(
        isAddDialogVisible,
        transactionInDialog,
    ) { dialogVisible, dialogTransaction ->
        dialogVisible to dialogTransaction
    }

    private val suggestedCategory: StateFlow<Category?> = combine(
        noteForCategorySuggestion,
        categoryRepository.observeAll(),
    ) { note, categories ->
        note to categories
    }.flatMapLatest { (note, categories) ->
        categorizeByKeywordsUseCase(note, categories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val baseUiState = combine(
        getLastTransactionsUseCase(limit = TRANSACTION_LIMIT),
        selectedMonthSpending,
        categoryRepository.observeAll(),
        dialogState,
        budgetUiState,
    ) { transactions, monthSpending, categories, dialog, budget ->
        val (month, spending) = monthSpending
        val (dialogVisible, dialogTransaction) = dialog
        HomeUiState(
            transactions = transactions.toTransactionItems(categories),
            categoryStats = spending.toCategoryStats(categories),
            categories = categories,
            selectedMonth = month,
            weeklyBudgetLimit = budget.weeklyBudgetLimit,
            weeklyBudgetInput = budget.weeklyBudgetInput,
            isWeeklyBudgetSaving = budget.isWeeklyBudgetSaving,
            isAddDialogVisible = dialogVisible,
            transactionInDialog = dialogTransaction,
            isLoading = false,
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        baseUiState,
        suggestedCategory,
    ) { state, categorySuggestion ->
        state.copy(suggestedCategory = categorySuggestion)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun onAddClick() {
        transactionInDialog.update { null }
        noteForCategorySuggestion.update { "" }
        isAddDialogVisible.update { true }
    }

    fun onTransactionClick(transaction: TransactionItemUi) {
        transactionInDialog.update { transaction }
        noteForCategorySuggestion.update { transaction.note.orEmpty() }
        isAddDialogVisible.update { true }
    }

    fun onDismissAddDialog() {
        isAddDialogVisible.update { false }
        transactionInDialog.update { null }
        noteForCategorySuggestion.update { "" }
    }

    fun onDialogNoteChange(note: String) {
        noteForCategorySuggestion.update { note }
    }

    fun onWeeklyBudgetInputChange(input: String) {
        weeklyBudgetInput.update { input }
    }

    fun onSaveWeeklyBudgetClick() {
        val input = weeklyBudgetInput.value.orEmpty().trim()
        val limit = if (input.isBlank()) {
            0.0
        } else {
            input.replace(',', '.')
                .toDoubleOrNull()
                ?.takeIf { it >= 0.0 }
                ?: return
        }

        viewModelScope.launch {
            isWeeklyBudgetSaving.update { true }
            runCatching {
                budgetRepository.updateWeeklyBudgetLimit(limit)
            }.onSuccess {
                weeklyBudgetInput.update { limit.toInputText() }
                _budgetMessages.emit(BudgetMessage.Saved)
                checkAndNotifyWeeklyBudget()
            }
            isWeeklyBudgetSaving.update { false }
        }
    }

    fun onPreviousMonthClick() {
        selectedMonth.update { it.minusMonths(1) }
    }

    fun onNextMonthClick() {
        selectedMonth.update { it.plusMonths(1) }
    }

    fun onExportClick() {
        viewModelScope.launch {
            val month = selectedMonth.value
            pendingExportMonth = month
            _exportRequests.emit(
                ExportDocumentRequest(fileName = month.toExportFileName()),
            )
        }
    }

    fun onImportClick() {
        viewModelScope.launch {
            _importRequests.emit(ImportDocumentRequest)
        }
    }

    fun onImportDocumentSelected(inputStream: InputStream) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    importBankStatementUseCase(inputStream)
                }
            }.onSuccess { result ->
                _importMessages.emit(
                    ImportMessage.Success(
                        importedCount = result.importedCount,
                        duplicateCount = result.duplicateCount,
                    ),
                )
                checkAndNotifyWeeklyBudget()
            }.onFailure { error ->
                _importMessages.emit(ImportMessage.Error(error.message))
            }
        }
    }

    fun onImportDocumentOpenFailed() {
        viewModelScope.launch {
            _importMessages.emit(ImportMessage.Error())
        }
    }

    fun onExportDocumentCreated(outputStream: OutputStream) {
        viewModelScope.launch {
            runCatching {
                val month = pendingExportMonth ?: selectedMonth.value
                exportTransactionsUseCase(
                    year = month.year,
                    month = month.monthValue,
                    outputStream = outputStream,
                )
            }.onSuccess { transactionCount ->
                pendingExportMonth = null
                _exportMessages.emit(ExportMessage.Success(transactionCount))
            }.onFailure {
                pendingExportMonth = null
                _exportMessages.emit(ExportMessage.Error)
            }
        }
    }

    fun onExportDocumentOpenFailed() {
        pendingExportMonth = null
        viewModelScope.launch {
            _exportMessages.emit(ExportMessage.Error)
        }
    }

    fun onExportDocumentDismissed() {
        pendingExportMonth = null
    }

    fun onSaveTransaction(transactionId: Long?, amount: Double, date: Long, categoryId: Long, note: String?) {
        viewModelScope.launch {
            val editedTransaction = transactionInDialog.value
            runCatching {
                addTransactionUseCase(
                    Transaction(
                        id = transactionId ?: 0,
                        amount = amount,
                        date = date,
                        categoryId = categoryId,
                        note = note?.takeIf { it.isNotBlank() },
                        type = editedTransaction?.type ?: TransactionType.EXPENSE,
                        sourceBank = editedTransaction?.sourceBank,
                        sourceDescription = editedTransaction?.sourceDescription,
                        importHash = editedTransaction?.importHash,
                    ),
                )
            }.onSuccess {
                isAddDialogVisible.update { false }
                transactionInDialog.update { null }
                noteForCategorySuggestion.update { "" }
                checkAndNotifyWeeklyBudget()
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
                categoryId = transaction.categoryId,
                categoryName = category?.name ?: "—",
                note = transaction.note,
                colorInt = category?.colorInt ?: DEFAULT_COLOR,
                type = transaction.type,
                sourceBank = transaction.sourceBank,
                sourceDescription = transaction.sourceDescription,
                importHash = transaction.importHash,
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

    private fun YearMonth.toExportFileName(): String =
        "transactions_${year}_${monthValue.toString().padStart(2, '0')}.csv"

    private suspend fun checkAndNotifyWeeklyBudget() {
        checkWeeklyBudgetUseCase()?.let { budgetInfo ->
            notificationManager.showWeeklyBudgetExceeded(budgetInfo)
        }
    }

    private fun Double.toInputText(): String =
        if (this <= 0.0) "" else toString()

    companion object {
        const val TRANSACTION_LIMIT = 10
        private const val DEFAULT_COLOR = 0xFF9E9E9E.toInt()
    }
}

data class ExportDocumentRequest(
    val fileName: String,
)

data object ImportDocumentRequest

sealed interface ExportMessage {
    data class Success(val transactionCount: Int) : ExportMessage
    data object Error : ExportMessage
}

sealed interface ImportMessage {
    data class Success(
        val importedCount: Int,
        val duplicateCount: Int,
    ) : ImportMessage

    data class Error(
        val reason: String? = null,
    ) : ImportMessage
}

sealed interface BudgetMessage {
    data object Saved : BudgetMessage
}

private data class BudgetUiState(
    val weeklyBudgetLimit: Double,
    val weeklyBudgetInput: String,
    val isWeeklyBudgetSaving: Boolean,
)
