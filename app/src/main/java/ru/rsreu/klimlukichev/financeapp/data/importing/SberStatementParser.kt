package ru.rsreu.klimlukichev.financeapp.data.importing

import ru.rsreu.klimlukichev.financeapp.domain.importing.BankStatementTransaction
import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class SberStatementParser(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : BankStatementParser {

    override fun canParse(text: String): Boolean =
        text.contains("СберБанк", ignoreCase = true) ||
            text.contains("Выписка по счёту дебетовой карты", ignoreCase = true) ||
            text.contains("Выписка по счету дебетовой карты", ignoreCase = true)

    override fun parse(text: String): List<BankStatementTransaction> {
        val result = mutableListOf<PendingSberOperation>()
        var current: PendingSberOperation? = null

        text.normalizeStatementText()
            .lineSequence()
            .map { it.trim() }
            .forEach { line ->
                if (line.isBlank() || line.isNoise()) return@forEach

                val operationMatch = operationRegex.matchEntire(line)
                if (operationMatch != null) {
                    current?.let(result::add)
                    current = operationMatch.toPendingOperation()
                    return@forEach
                }

                val description = descriptionRegex.matchEntire(line)?.groupValues?.get(1) ?: line
                current = current?.copy(
                    description = listOfNotNull(current?.description, description)
                        .joinToString(" ")
                        .normalizeStatementSpaces(),
                )
            }

        current?.let(result::add)

        return result.map { operation ->
            BankStatementTransaction(
                bank = BANK_NAME,
                date = operation.date,
                amount = operation.amount,
                type = operation.type,
                description = operation.description
                    .removeCardMaskOnlyTail()
                    .ifBlank { operation.bankCategory }
                    .normalizeStatementSpaces(),
                bankCategory = operation.bankCategory,
            )
        }
    }

    private fun MatchResult.toPendingOperation(): PendingSberOperation {
        val operationDate = groupValues[1]
        val operationTime = groupValues[2]
        val bankCategory = groupValues[3].normalizeStatementSpaces()
        val amountText = groupValues[4]

        val date = LocalDate.parse(operationDate, dateFormatter)
        val time = LocalTime.parse(operationTime, timeFormatter)
        val dateTime = LocalDateTime.of(date, time)

        return PendingSberOperation(
            date = dateTime.atZone(zoneId).toInstant().toEpochMilli(),
            amount = amountText.parseStatementAmount(),
            type = if (amountText.trim().startsWith("+")) TransactionType.INCOME else TransactionType.EXPENSE,
            bankCategory = bankCategory,
            description = "",
        )
    }

    private fun String.isNoise(): Boolean =
        startsWith("--") ||
            startsWith("ДАТА ОПЕРАЦИИ", ignoreCase = true) ||
            startsWith("Дата обработки", ignoreCase = true) ||
            startsWith("и код авторизации", ignoreCase = true) ||
            startsWith("КАТЕГОРИЯ", ignoreCase = true) ||
            startsWith("Описание операции", ignoreCase = true) ||
            startsWith("СУММА", ignoreCase = true) ||
            startsWith("Сумма в валюте", ignoreCase = true) ||
            startsWith("ОСТАТОК", ignoreCase = true) ||
            startsWith("В валюте счёта", ignoreCase = true) ||
            startsWith("Продолжение на следующей", ignoreCase = true) ||
            startsWith("Дата формирования", ignoreCase = true) ||
            startsWith("ПАО Сбербанк", ignoreCase = true)

    private fun String.removeCardMaskOnlyTail(): String =
        replace(Regex("""\s+\*{2,}\d{4}$"""), "")

    private data class PendingSberOperation(
        val date: Long,
        val amount: Double,
        val type: TransactionType,
        val bankCategory: String,
        val description: String,
    )

    private companion object {
        const val BANK_NAME = "Сбер"

        val dateFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.forLanguageTag("ru-RU"))
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val operationRegex = Regex(
            """^(\d{2}\.\d{2}\.\d{4})\s+(\d{2}:\d{2})\s+(.+?)\s+([+−-]?\s*[\d\s\u00A0\u202F]+[,.]\d{2})\s+([+−-]?\s*[\d\s\u00A0\u202F]+[,.]\d{2})$""",
        )
        val descriptionRegex = Regex("""^\d{2}\.\d{2}\.\d{4}\s+\d+\s+(.+)$""")
    }
}
