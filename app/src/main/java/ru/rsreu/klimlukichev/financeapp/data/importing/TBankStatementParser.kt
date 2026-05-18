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
        val result = mutableListOf<PendingTBankOperation>()
        var current: PendingTBankOperation? = null

        text.lineSequence()
            .map { it.trim() }
            .forEach { line ->
                if (line.isBlank() || line.isNoise()) return@forEach

                val match = operationRegex.matchEntire(line)
                if (match != null) {
                    current?.let(result::add)
                    current = match.toPendingOperation()
                } else {
                    current = current?.copy(
                        description = listOf(current?.description, line)
                            .filterNotNull()
                            .joinToString(" ")
                            .normalizeSpaces(),
                    )
                }
            }

        current?.let(result::add)

        return result.map { operation ->
            BankStatementTransaction(
                bank = BANK_NAME,
                date = operation.date,
                amount = operation.amount,
                type = operation.type,
                description = operation.description.normalizeSpaces(),
            )
        }
    }

    private fun MatchResult.toPendingOperation(): PendingTBankOperation {
        val operationDate = groupValues[1]
        val operationTime = groupValues[2].takeIf { it.isNotBlank() }
        val isIncome = groupValues[3].isNotBlank() || groupValues[5].isNotBlank()
        val amount = groupValues[4].parseAmount()
        val description = groupValues.getOrNull(7).orEmpty()

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

    private fun String.parseAmount(): Double =
        replace(" ", "")
            .replace(",", ".")
            .toDouble()

    private fun String.normalizeSpaces(): String =
        replace(Regex("\\s+"), " ").trim()

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
            """^(\d{2}\.\d{2}\.\d{2})(?:\s+(\d{2}:\d{2}))?\s+\d{2}\.\d{2}\.\d{2}\s+(\+)?\s*([\d\s]+[.,]\d{2})\s*₽\s+(\+)?\s*([\d\s]+[.,]\d{2})\s*₽(?:\s+(.+))?$""",
        )
    }
}
