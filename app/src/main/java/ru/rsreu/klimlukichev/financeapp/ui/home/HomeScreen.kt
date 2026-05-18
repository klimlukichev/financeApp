package ru.rsreu.klimlukichev.financeapp.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.rsreu.klimlukichev.financeapp.R
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)
            }.onSuccess { outputStream ->
                if (outputStream != null) {
                    viewModel.onExportDocumentCreated(outputStream)
                } else {
                    viewModel.onExportDocumentOpenFailed()
                }
            }.onFailure {
                viewModel.onExportDocumentOpenFailed()
            }
        } else {
            viewModel.onExportDocumentDismissed()
        }
    }
    val openPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)
            }.onSuccess { inputStream ->
                if (inputStream != null) {
                    viewModel.onImportDocumentSelected(inputStream)
                } else {
                    viewModel.onImportDocumentOpenFailed()
                }
            }.onFailure {
                viewModel.onImportDocumentOpenFailed()
            }
        }
    }
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("ru-RU"))
    }
    val monthFormatter = remember {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru-RU"))
    }
    val selectedMonthLabel = remember(uiState.selectedMonth) {
        uiState.selectedMonth.format(monthFormatter)
    }
    val monthlyTotal = remember(uiState.categoryStats) {
        uiState.categoryStats.sumOf { it.totalAmount }
    }

    LaunchedEffect(viewModel) {
        viewModel.exportRequests.collect { request ->
            createDocumentLauncher.launch(request.fileName)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.importRequests.collect {
            openPdfLauncher.launch(arrayOf("application/pdf"))
        }
    }

    LaunchedEffect(viewModel, context) {
        viewModel.exportMessages.collect { message ->
            val text = when (message) {
                is ExportMessage.Success -> context.getString(
                    R.string.export_success,
                    message.transactionCount,
                )
                ExportMessage.Error -> context.getString(R.string.export_error)
            }
            snackbarHostState.showSnackbar(text)
        }
    }

    LaunchedEffect(viewModel, context) {
        viewModel.importMessages.collect { message ->
            val text = when (message) {
                is ImportMessage.Success -> context.getString(
                    R.string.import_success,
                    message.importedCount,
                    message.duplicateCount,
                )
                is ImportMessage.Error -> message.reason
                    ?.let { context.getString(R.string.import_error_with_reason, it) }
                    ?: context.getString(R.string.import_error)
            }
            snackbarHostState.showSnackbar(text)
        }
    }

    LaunchedEffect(viewModel, context) {
        viewModel.budgetMessages.collect { message ->
            val text = when (message) {
                BudgetMessage.Saved -> context.getString(R.string.budget_saved)
            }
            snackbarHostState.showSnackbar(text)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(stringResource(R.string.action_add))
            }
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            HomeContent(
                uiState = uiState,
                monthlyTotal = monthlyTotal,
                selectedMonthLabel = selectedMonthLabel,
                currencyFormat = currencyFormat,
                onTransactionClick = viewModel::onTransactionClick,
                onPreviousMonthClick = viewModel::onPreviousMonthClick,
                onNextMonthClick = viewModel::onNextMonthClick,
                onExportClick = viewModel::onExportClick,
                onImportClick = viewModel::onImportClick,
                onWeeklyBudgetInputChange = viewModel::onWeeklyBudgetInputChange,
                onSaveWeeklyBudgetClick = viewModel::onSaveWeeklyBudgetClick,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    if (uiState.isAddDialogVisible) {
        AddTransactionDialog(
            transaction = uiState.transactionInDialog,
            categories = uiState.categories,
            suggestedCategory = uiState.suggestedCategory,
            onDismiss = viewModel::onDismissAddDialog,
            onNoteChange = viewModel::onDialogNoteChange,
            onSave = viewModel::onSaveTransaction,
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    monthlyTotal: Double,
    selectedMonthLabel: String,
    currencyFormat: NumberFormat,
    onTransactionClick: (TransactionItemUi) -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onWeeklyBudgetInputChange: (String) -> Unit,
    onSaveWeeklyBudgetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 104.dp),
    ) {
        item {
            HomeHeader()
            Spacer(modifier = Modifier.height(18.dp))
            MonthlySummaryCard(
                total = currencyFormat.format(monthlyTotal),
                selectedMonthLabel = selectedMonthLabel,
                transactionCount = uiState.transactions.size,
                categoryCount = uiState.categoryStats.size,
                onPreviousMonthClick = onPreviousMonthClick,
                onNextMonthClick = onNextMonthClick,
                onExportClick = onExportClick,
                onImportClick = onImportClick,
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        item {
            WeeklyBudgetCard(
                budgetInput = uiState.weeklyBudgetInput,
                isSaving = uiState.isWeeklyBudgetSaving,
                onBudgetInputChange = onWeeklyBudgetInputChange,
                onSaveClick = onSaveWeeklyBudgetClick,
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        item {
            CategorySpendingChart(
                stats = uiState.categoryStats,
                currencyFormat = currencyFormat,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        item {
            SectionHeader(
                title = stringResource(R.string.home_recent_title),
                subtitle = stringResource(R.string.home_recent_subtitle),
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (uiState.transactions.isEmpty()) {
            item {
                EmptyTransactionsCard()
            }
        } else {
            items(uiState.transactions, key = { it.id }) { transaction ->
                TransactionListItem(
                    item = transaction,
                    onClick = { onTransactionClick(transaction) },
                    currencyFormat = currencyFormat,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Column {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeeklyBudgetCard(
    budgetInput: String,
    isSaving: Boolean,
    onBudgetInputChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
        ) {
            Text(
                text = stringResource(R.string.budget_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.budget_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = budgetInput,
                onValueChange = onBudgetInputChange,
                label = { Text(stringResource(R.string.budget_limit_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onSaveClick,
                enabled = !isSaving,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_save_budget))
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    total: String,
    selectedMonthLabel: String,
    transactionCount: Int,
    categoryCount: Int,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                        ),
                    ),
                )
                .padding(22.dp),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_month_total_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(
                        onClick = onPreviousMonthClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text("<")
                    }
                    Text(
                        text = selectedMonthLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = onNextMonthClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(">")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = total,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.height(22.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryMetric(
                        label = stringResource(R.string.home_transactions_count),
                        value = transactionCount.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    SummaryMetric(
                        label = stringResource(R.string.home_categories_count),
                        value = categoryCount.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = onImportClick,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color.White.copy(alpha = 0.16f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.action_import_pdf))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    TextButton(
                        onClick = onExportClick,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color.White.copy(alpha = 0.16f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.action_export_csv))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.85f)),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyTransactionsCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_empty_transactions_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.home_empty_transactions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
