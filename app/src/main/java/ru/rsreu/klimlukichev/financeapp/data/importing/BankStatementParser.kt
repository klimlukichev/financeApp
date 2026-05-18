package ru.rsreu.klimlukichev.financeapp.data.importing

import ru.rsreu.klimlukichev.financeapp.domain.importing.BankStatementTransaction

interface BankStatementParser {

    fun canParse(text: String): Boolean

    fun parse(text: String): List<BankStatementTransaction>
}
