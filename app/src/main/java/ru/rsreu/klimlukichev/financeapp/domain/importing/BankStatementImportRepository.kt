package ru.rsreu.klimlukichev.financeapp.domain.importing

import java.io.InputStream

interface BankStatementImportRepository {

    suspend fun parse(inputStream: InputStream): List<BankStatementTransaction>
}
