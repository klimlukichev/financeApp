package ru.rsreu.klimlukichev.financeapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import ru.rsreu.klimlukichev.financeapp.R
import ru.rsreu.klimlukichev.financeapp.domain.model.Category
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    transaction: TransactionItemUi?,
    categories: List<Category>,
    suggestedCategory: Category?,
    onDismiss: () -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: (transactionId: Long?, amount: Double, date: Long, categoryId: Long, note: String?) -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val initialDateMillis = remember(transaction?.id, transaction?.date) {
        transaction?.date?.toLocalDateFromSystemMillis()?.toUtcStartOfDayMillis()
            ?: LocalDate.now().toUtcStartOfDayMillis()
    }
    var amountText by remember(transaction?.id) { mutableStateOf(transaction?.amount?.toString().orEmpty()) }
    var noteText by remember(transaction?.id) { mutableStateOf(transaction?.note.orEmpty()) }
    var selectedCategory by remember(categories, transaction?.categoryId) {
        mutableStateOf(categories.firstOrNull { it.id == transaction?.categoryId } ?: categories.firstOrNull())
    }
    var selectedDateMillis by remember(transaction?.id) { mutableStateOf(initialDateMillis) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var isCategoryManuallySelected by remember(transaction?.id) { mutableStateOf(transaction != null) }
    var amountError by remember { mutableStateOf(false) }
    val formattedDate = remember(selectedDateMillis) {
        selectedDateMillis.toLocalDateFromUtcMillis().format(dateFormatter)
    }

    LaunchedEffect(suggestedCategory?.id) {
        if (transaction == null && !isCategoryManuallySelected && suggestedCategory != null) {
            selectedCategory = suggestedCategory
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column {
                Text(
                    text = stringResource(
                    if (transaction == null) R.string.dialog_add_title else R.string.dialog_edit_title,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.dialog_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = false
                    },
                    label = { Text(stringResource(R.string.dialog_amount_hint)) },
                    isError = amountError,
                    supportingText = {
                        if (amountError) {
                            Text(stringResource(R.string.error_invalid_amount))
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                TextButton(
                    onClick = { isDatePickerVisible = true },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                ) {
                    Text(stringResource(R.string.dialog_date_value, formattedDate))
                }

                if (categories.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.dialog_category_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category
                                        isCategoryManuallySelected = true
                                        categoryExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = {
                        noteText = it
                        onNoteChange(it)
                    },
                    label = { Text(stringResource(R.string.dialog_note_hint)) },
                    shape = RoundedCornerShape(18.dp),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
                Button(
                    onClick = {
                        val amount = amountText.replace(',', '.').toDoubleOrNull()
                        val categoryId = selectedCategory?.id
                        if (amount == null || amount == 0.0 || categoryId == null) {
                            amountError = true
                            return@Button
                        }
                        onSave(
                            transaction?.id,
                            amount,
                            selectedDateMillis.toLocalDateFromUtcMillis().toSystemStartOfDayMillis(),
                            categoryId,
                            noteText,
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.dialog_save))
                }
            }
        },
    )

    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
        )
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis ?: selectedDateMillis
                        isDatePickerVisible = false
                    },
                ) {
                    Text(stringResource(R.string.dialog_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerVisible = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun LocalDate.toUtcStartOfDayMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun LocalDate.toSystemStartOfDayMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDateFromUtcMillis(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun Long.toLocalDateFromSystemMillis(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
