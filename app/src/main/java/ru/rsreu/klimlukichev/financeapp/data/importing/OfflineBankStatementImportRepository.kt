package ru.rsreu.klimlukichev.financeapp.data.importing

import ru.rsreu.klimlukichev.financeapp.domain.importing.BankStatementImportRepository
import ru.rsreu.klimlukichev.financeapp.domain.importing.BankStatementTransaction
import java.io.InputStream

class OfflineBankStatementImportRepository(
    private val pdfTextExtractor: PdfTextExtractor,
    private val parserFactory: BankStatementParserFactory,
) : BankStatementImportRepository {

    override suspend fun parse(inputStream: InputStream): List<BankStatementTransaction> =
        inputStream.use { stream ->
            val text = pdfTextExtractor.extract(stream)
            parserFactory.parserFor(text)
                .parse(text)
                .ifEmpty { throw EmptyBankStatementException() }
        }
}
