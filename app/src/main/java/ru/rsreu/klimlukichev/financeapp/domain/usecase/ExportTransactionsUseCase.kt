package ru.rsreu.klimlukichev.financeapp.domain.usecase

import kotlinx.coroutines.flow.first
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.TransactionRepository
import ru.rsreu.klimlukichev.financeapp.domain.util.DatePeriodFactory
import java.io.OutputStream
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ExportTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) {

    suspend operator fun invoke(
        year: Int,
        month: Int,
        outputStream: OutputStream,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Int {
        return outputStream.use { stream ->
            val period = DatePeriodFactory.monthOf(year, month, zoneId)
            val categoriesById = categoryRepository.observeAll()
                .first()
                .associateBy { it.id }
            val transactions = transactionRepository.observeByPeriod(
                startDate = period.startInclusive,
                endDate = period.endInclusive,
            ).first()

            stream.write(UTF8_BOM)
            stream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.appendLine(
                    listOf("id", "date", "amount", "category_id", "category", "note")
                        .joinToString(CSV_SEPARATOR),
                )
                transactions
                    .sortedBy { it.date }
                    .forEach { transaction ->
                        val date = Instant.ofEpochMilli(transaction.date)
                            .atZone(zoneId)
                            .toLocalDate()
                            .format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val categoryName = categoriesById[transaction.categoryId]?.name.orEmpty()
                        writer.appendLine(
                            listOf(
                                transaction.id.toString(),
                                date,
                                transaction.amount.toCsvAmount(),
                                transaction.categoryId.toString(),
                                categoryName,
                                transaction.note.orEmpty(),
                            ).joinToString(CSV_SEPARATOR) { it.toCsvField() },
                        )
                    }
            }

            transactions.size
        }
    }

    private fun Double.toCsvAmount(): String =
        BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()

    private fun String.toCsvField(): String {
        val escaped = replace("\"", "\"\"")
        val needsQuotes = any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuotes) "\"$escaped\"" else escaped
    }

    private companion object {
        const val CSV_SEPARATOR = ","
        val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    }
}
