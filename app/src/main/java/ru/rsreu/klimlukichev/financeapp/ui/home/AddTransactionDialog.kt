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
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

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
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val initialDateTime = remember(transaction?.id, transaction?.date) {
        transaction?.date?.toLocalDateTimeFromSystemMillis() ?: LocalDateTime.now()
    }
    val initialDateMillis = remember(initialDateTime) { initialDateTime.toLocalDate().toUtcStartOfDayMillis() }
    var amountText by remember(transaction?.id) { mutableStateOf(transaction?.amount?.roundToLong()?.toString().orEmpty()) }
    var noteText by remember(transaction?.id) { mutableStateOf(transaction?.note.orEmpty()) }
    var selectedCategory by remember(categories, transaction?.categoryId) {
        mutableStateOf(categories.firstOrNull { it.id == transaction?.categoryId } ?: categories.firstOrNull())
    }
    var selectedDateMillis by remember(transaction?.id) { mutableStateOf(initialDateMillis) }
    var selectedHour by remember(transaction?.id) { mutableStateOf(initialDateTime.hour) }
    var selectedMinute by remember(transaction?.id) { mutableStateOf(initialDateTime.minute) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var isTimePickerVisible by remember { mutableStateOf(false) }
    var isCategoryManuallySelected by remember(transaction?.id) { mutableStateOf(transaction != null) }
    var amountError by remember { mutableStateOf(false) }
    var dateTimeError by remember { mutableStateOf(false) }
    val formattedDate = remember(selectedDateMillis) {
        selectedDateMillis.toLocalDateFromUtcMillis().format(dateFormatter)
    }
    val formattedTime = remember(selectedHour, selectedMinute) {
        LocalTime.of(selectedHour, selectedMinute).format(timeFormatter)
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

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(
                        onClick = { isDatePickerVisible = true },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.dialog_date_value, formattedDate))
                    }
                    TextButton(
                        onClick = { isTimePickerVisible = true },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.dialog_time_value, formattedTime))
                    }
                }

                TextButton(
                    onClick = {
                        val now = LocalDateTime.now()
                        selectedDateMillis = now.toLocalDate().toUtcStartOfDayMillis()
                        selectedHour = now.hour
                        selectedMinute = now.minute
                        dateTimeError = false
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.dialog_use_current_datetime))
                }
                if (dateTimeError) {
                    Text(
                        text = stringResource(R.string.error_future_datetime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
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
                        val selectedDateTime = LocalDateTime.of(
                            selectedDateMillis.toLocalDateFromUtcMillis(),
                            LocalTime.of(selectedHour, selectedMinute),
                        )
                        if (selectedDateTime.isAfter(LocalDateTime.now())) {
                            dateTimeError = true
                            return@Button
                        }
                        onSave(
                            transaction?.id,
                            amount,
                            selectedDateTime.toSystemMillis(),
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
        val maxDateMillis = remember { LocalDate.now().toUtcStartOfDayMillis() }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= maxDateMillis

                override fun isSelectableYear(year: Int): Boolean =
                    year <= LocalDate.now().year
            },
        )
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis ?: selectedDateMillis
                        dateTimeError = false
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

    if (isTimePickerVisible) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { isTimePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        dateTimeError = false
                        isTimePickerVisible = false
                    },
                ) {
                    Text(stringResource(R.string.dialog_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { isTimePickerVisible = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }
}

private fun LocalDate.toUtcStartOfDayMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun LocalDateTime.toSystemMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDateFromUtcMillis(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun Long.toLocalDateFromSystemMillis(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun Long.toLocalDateTimeFromSystemMillis(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
