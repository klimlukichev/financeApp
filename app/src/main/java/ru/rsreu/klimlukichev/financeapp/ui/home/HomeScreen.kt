package ru.rsreu.klimlukichev.financeapp.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.rsreu.klimlukichev.financeapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = viewModel::onAddClick) {
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
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    if (uiState.isAddDialogVisible) {
        AddTransactionDialog(
            categories = uiState.categories,
            onDismiss = viewModel::onDismissAddDialog,
            onSave = viewModel::onSaveTransaction,
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.home_chart_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            CategorySpendingChart(
                stats = uiState.categoryStats,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        item {
            Text(
                text = stringResource(R.string.home_recent_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )
        }

        if (uiState.transactions.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.home_empty_transactions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
        } else {
            items(uiState.transactions, key = { it.id }) { transaction ->
                TransactionListItem(item = transaction)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
