package ru.rsreu.klimlukichev.financeapp.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.androidx.compose.koinViewModel
import ru.rsreu.klimlukichev.financeapp.R
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contentResolver = LocalContext.current.contentResolver
    val snackbarHostState = remember { SnackbarHostState() }
    var isMonthPickerVisible by remember { mutableStateOf(false) }
    var isSettingsVisible by remember { mutableStateOf(false) }
    var pendingSnackbar by remember { mutableStateOf<PendingSnackbar?>(null) }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.openOutputStream(uri)
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
    val createPdfReportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.openOutputStream(uri)
            }.onSuccess { outputStream ->
                if (outputStream != null) {
                    viewModel.onPdfReportDocumentCreated(outputStream)
                } else {
                    viewModel.onPdfReportDocumentOpenFailed()
                }
            }.onFailure {
                viewModel.onPdfReportDocumentOpenFailed()
            }
        } else {
            viewModel.onPdfReportDocumentDismissed()
        }
    }
    val openPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.openInputStream(uri)
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
    val appLocale = remember(uiState.languageTag) {
        Locale.forLanguageTag(uiState.languageTag)
    }
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(CURRENCY_LOCALE).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }
    val monthFormatter = remember(appLocale) {
        DateTimeFormatter.ofPattern(MONTH_YEAR_PATTERN, appLocale)
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
        viewModel.pdfReportRequests.collect { request ->
            createPdfReportLauncher.launch(request.fileName)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.importRequests.collect {
            openPdfLauncher.launch(arrayOf("application/pdf"))
        }
    }

    LaunchedEffect(viewModel) {
        supervisorScope {
            launch {
                viewModel.exportMessages.collect { message ->
                    pendingSnackbar = PendingSnackbar.Export(message)
                }
            }
            launch {
                viewModel.pdfReportMessages.collect { message ->
                    pendingSnackbar = PendingSnackbar.PdfReport(message)
                }
            }
            launch {
                viewModel.importMessages.collect { message ->
                    pendingSnackbar = PendingSnackbar.Import(message)
                }
            }
            launch {
                viewModel.budgetMessages.collect { message ->
                    pendingSnackbar = PendingSnackbar.Budget(message)
                }
            }
        }
    }

    val snackbarText = when (val snackbar = pendingSnackbar) {
        is PendingSnackbar.Export -> when (val message = snackbar.message) {
            is ExportMessage.Success -> stringResource(
                R.string.export_success,
                message.transactionCount,
            )
            ExportMessage.Error -> stringResource(R.string.export_error)
        }
        is PendingSnackbar.PdfReport -> when (val message = snackbar.message) {
            is ExportMessage.Success -> stringResource(
                R.string.pdf_report_success,
                message.transactionCount,
            )
            ExportMessage.Error -> stringResource(R.string.pdf_report_error)
        }
        is PendingSnackbar.Import -> when (val message = snackbar.message) {
            is ImportMessage.Success -> stringResource(
                R.string.import_success,
                message.importedCount,
                message.duplicateCount,
            )
            is ImportMessage.Error -> message.reason?.let { reason ->
                stringResource(R.string.import_error_with_reason, reason)
            } ?: stringResource(R.string.import_error)
        }
        is PendingSnackbar.Budget -> when (snackbar.message) {
            BudgetMessage.Saved -> stringResource(R.string.budget_saved)
        }
        null -> null
    }

    LaunchedEffect(snackbarText) {
        snackbarText?.let { message ->
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
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
                onMonthLabelClick = { isMonthPickerVisible = true },
                onSettingsClick = { isSettingsVisible = true },
                onExportClick = viewModel::onExportClick,
                onPdfReportClick = viewModel::onPdfReportClick,
                onImportClick = viewModel::onImportClick,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    if (isMonthPickerVisible) {
        MonthSelectionDialog(
            selectedMonth = uiState.selectedMonth,
            monthFormatter = monthFormatter,
            displayLocale = appLocale,
            onMonthSelected = { month ->
                viewModel.onMonthSelected(month)
                isMonthPickerVisible = false
            },
            onDismiss = { isMonthPickerVisible = false },
        )
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

    if (isSettingsVisible) {
        SettingsDialog(
            uiState = uiState,
            onBudgetInputChange = viewModel::onWeeklyBudgetInputChange,
            onSaveBudgetClick = viewModel::onSaveWeeklyBudgetClick,
            onDarkThemeChange = viewModel::onDarkThemeChange,
            onLanguageSelected = viewModel::onLanguageSelected,
            onDismiss = { isSettingsVisible = false },
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
    onMonthLabelClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExportClick: () -> Unit,
    onPdfReportClick: () -> Unit,
    onImportClick: () -> Unit,
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
            HomeHeader(onSettingsClick = onSettingsClick)
            Spacer(modifier = Modifier.height(18.dp))
            MonthlySummaryCard(
                total = currencyFormat.format(monthlyTotal),
                selectedMonthLabel = selectedMonthLabel,
                transactionCount = uiState.transactions.size,
                categoryCount = uiState.categoryStats.size,
                canNavigateNextMonth = uiState.canNavigateNextMonth,
                onPreviousMonthClick = onPreviousMonthClick,
                onNextMonthClick = onNextMonthClick,
                onMonthLabelClick = onMonthLabelClick,
                onExportClick = onExportClick,
                onPdfReportClick = onPdfReportClick,
                onImportClick = onImportClick,
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
private fun SettingsDialog(
    uiState: HomeUiState,
    onBudgetInputChange: (String) -> Unit,
    onSaveBudgetClick: () -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                WeeklyBudgetSettingsSection(
                    budgetInput = uiState.weeklyBudgetInput,
                    isSaving = uiState.isWeeklyBudgetSaving,
                    onBudgetInputChange = onBudgetInputChange,
                    onSaveClick = onSaveBudgetClick,
                )
                ThemeSettingsRow(
                    isDarkThemeEnabled = uiState.isDarkThemeEnabled,
                    onDarkThemeChange = onDarkThemeChange,
                )
                LanguageSettingsSection(
                    selectedLanguageTag = uiState.languageTag,
                    onLanguageSelected = onLanguageSelected,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}

@Composable
private fun ThemeSettingsRow(
    isDarkThemeEnabled: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.theme_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.theme_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = isDarkThemeEnabled,
            onCheckedChange = onDarkThemeChange,
        )
    }
}

@Composable
private fun LanguageSettingsSection(
    selectedLanguageTag: String,
    onLanguageSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.language_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            LanguageOptionButton(
                text = stringResource(R.string.language_russian),
                selected = selectedLanguageTag == LANGUAGE_RU,
                onClick = { onLanguageSelected(LANGUAGE_RU) },
                modifier = Modifier.weight(1f),
            )
            LanguageOptionButton(
                text = stringResource(R.string.language_english),
                selected = selectedLanguageTag == LANGUAGE_EN,
                onClick = { onLanguageSelected(LANGUAGE_EN) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LanguageOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
    ) {
        Text(text)
    }
}

@Composable
private fun HomeHeader(
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onSettingsClick) {
            Text(stringResource(R.string.action_settings))
        }
    }
}

@Composable
private fun WeeklyBudgetSettingsSection(
    budgetInput: String,
    isSaving: Boolean,
    onBudgetInputChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.budget_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.budget_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = budgetInput,
            onValueChange = onBudgetInputChange,
            label = { Text(stringResource(R.string.budget_limit_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(),
        )
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

@Composable
private fun MonthlySummaryCard(
    total: String,
    selectedMonthLabel: String,
    transactionCount: Int,
    categoryCount: Int,
    canNavigateNextMonth: Boolean,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onMonthLabelClick: () -> Unit,
    onExportClick: () -> Unit,
    onPdfReportClick: () -> Unit,
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
                    TextButton(
                        onClick = onMonthLabelClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = selectedMonthLabel,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    TextButton(
                        onClick = onNextMonthClick,
                        enabled = canNavigateNextMonth,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f),
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
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = onPdfReportClick,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.White.copy(alpha = 0.22f),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_export_pdf_report))
                }
            }
        }
    }
}

@Composable
private fun MonthSelectionDialog(
    selectedMonth: YearMonth,
    monthFormatter: DateTimeFormatter,
    displayLocale: Locale,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
) {
    val months = remember {
        val currentMonth = YearMonth.now()
        List(MONTH_PICKER_MONTHS_COUNT) { index -> currentMonth.minusMonths(index.toLong()) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.month_picker_title)) },
        text = {
            LazyColumn {
                items(months.chunked(3)) { rowMonths ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowMonths.forEach { month ->
                            TextButton(
                                onClick = { onMonthSelected(month) },
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (month == selectedMonth) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color.Transparent
                                    },
                                ),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = month.format(monthFormatter)
                                        .replaceFirstChar { it.titlecase(displayLocale) },
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                        repeat(3 - rowMonths.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )
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

private val CURRENCY_LOCALE = Locale.forLanguageTag("ru-RU")

private const val MONTH_PICKER_MONTHS_COUNT = 36
private const val MONTH_YEAR_PATTERN = "LLLL yyyy"
private const val LANGUAGE_RU = "ru"
private const val LANGUAGE_EN = "en"

private sealed interface PendingSnackbar {
    data class Export(val message: ExportMessage) : PendingSnackbar
    data class PdfReport(val message: ExportMessage) : PendingSnackbar
    data class Import(val message: ImportMessage) : PendingSnackbar
    data class Budget(val message: BudgetMessage) : PendingSnackbar
}
