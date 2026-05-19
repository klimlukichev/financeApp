package ru.rsreu.klimlukichev.financeapp.data.importing

import ru.rsreu.klimlukichev.financeapp.domain.importing.BankStatementTransaction
import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TBankStatementParser(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : BankStatementParser {

    override fun canParse(text: String): Boolean =
        text.contains("Выписка по договору", ignoreCase = true) ||
            text.contains("Т-Банк", ignoreCase = true) ||
            text.contains("Операции по карте", ignoreCase = true)

    override fun parse(text: String): List<BankStatementTransaction> {
        val normalizedText = text.normalizeStatementText()
        return operationRegex.findAll(normalizedText)
            .map { it.toPendingOperation() }
            .filterNot { it.description.isNoise() }
            .map { operation ->
            BankStatementTransaction(
                bank = BANK_NAME,
                date = operation.date,
                amount = operation.amount,
                type = operation.type,
                    description = operation.description
                        .removePageMarkerTail()
                        .ifBlank { "Операция из выписки Т-Банка" }
                        .normalizeStatementSpaces(),
            )
            }
            .toList()
    }

    private fun MatchResult.toPendingOperation(): PendingTBankOperation {
        val operationDate = groupValues[1]
        val operationTime = groupValues[2].takeIf { it.isNotBlank() }
        val isIncome = groupValues[3].isNotBlank() || groupValues[5].isNotBlank()
        val amount = groupValues[4].parseStatementAmount()
        val description = groupValues.getOrNull(7).orEmpty().normalizeStatementSpaces()

        val date = LocalDate.parse(operationDate, dateFormatter)
        val time = operationTime?.let { LocalTime.parse(it, timeFormatter) } ?: LocalTime.MIDNIGHT
        val dateTime = LocalDateTime.of(date, time)

        return PendingTBankOperation(
            date = dateTime.atZone(zoneId).toInstant().toEpochMilli(),
            amount = amount,
            type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
            description = description,
        )
    }

    private fun String.isNoise(): Boolean =
        startsWith("--") ||
            startsWith("Дата ") ||
            startsWith("Сумма") ||
            startsWith("операции", ignoreCase = true) ||
            startsWith("обработки", ignoreCase = true) ||
            startsWith("Операции по карте", ignoreCase = true) ||
            startsWith("Расходы:", ignoreCase = true) ||
            startsWith("Баланс ", ignoreCase = true) ||
            startsWith("•") ||
            contains("Выписка по договору", ignoreCase = true)

    private fun String.removePageMarkerTail(): String =
        replace(Regex("""\s+--\s+\d+\s+of\s+\d+\s+--.*$"""), "")

    private data class PendingTBankOperation(
        val date: Long,
        val amount: Double,
        val type: TransactionType,
        val description: String,
    )

    private companion object {
        const val BANK_NAME = "Т-Банк"

        val dateFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yy", Locale.forLanguageTag("ru-RU"))
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val operationRegex = Regex(
            pattern = """(\d{2}\.\d{2}\.\d{2})(?:\s+(\d{2}:\d{2}))?\s+\d{2}\.\d{2}\.\d{2}\s+(\+)?\s*([\d\s\u00A0\u202F]+[.,]\d{2})\s*(?:₽|руб\.?)?\s+(\+)?\s*([\d\s\u00A0\u202F]+[.,]\d{2})\s*(?:₽|руб\.?)?\s*([\s\S]*?)(?=\n\d{2}\.\d{2}\.\d{2}(?:\s+\d{2}:\d{2})?\s+\d{2}\.\d{2}\.\d{2}\s+[+]?\s*[\d\s\u00A0\u202F]+[.,]\d{2}|\z)""",
            options = setOf(RegexOption.MULTILINE),
        )
    }
}
