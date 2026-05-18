package ru.rsreu.klimlukichev.financeapp.domain.importing

data class BankStatementImportResult(
    val parsedCount: Int,
    val importedCount: Int,
    val duplicateCount: Int,
)
