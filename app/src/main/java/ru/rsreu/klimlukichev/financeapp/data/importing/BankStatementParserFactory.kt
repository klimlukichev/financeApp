package ru.rsreu.klimlukichev.financeapp.data.importing

class BankStatementParserFactory(
    private val parsers: List<BankStatementParser>,
) {

    fun parserFor(text: String): BankStatementParser =
        parsers.firstOrNull { it.canParse(text) }
            ?: throw UnsupportedBankStatementFormatException()
}

class UnsupportedBankStatementFormatException : IllegalArgumentException(
    "Формат банковской выписки не поддерживается",
)

class EmptyBankStatementException : IllegalArgumentException(
    "В выписке не найдены операции для импорта",
)
