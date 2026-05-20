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
                    .removeStatementArtifacts()
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

    private fun String.isNoise(): Boolean {
        if (isBlank()) return true

        if (startsWith("--") ||
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
            startsWith("ПАО Сбербанк", ignoreCase = true) ||
            startsWith("Выписка по сч", ignoreCase = true) ||
            startsWith("За период", ignoreCase = true) ||
            startsWith("СберБанк", ignoreCase = true) ||
            startsWith("www.", ignoreCase = true)
        ) {
            return true
        }

        return NOISE_LINE_REGEXES.any { it.matches(this) }
    }

    private fun String.removeCardMaskOnlyTail(): String =
        replace(Regex("""\s+\*{2,}\d{4}$"""), "")

    private fun String.removeStatementArtifacts(): String {
        var cutAt = length
        PAGE_FOOTER_REGEX.find(this)?.range?.first?.let { index ->
            if (index in 1 until cutAt) cutAt = index
        }
        FOOTER_TEXT_MARKERS.forEach { marker ->
            val index = indexOf(marker, ignoreCase = true)
            if (index in 1 until cutAt) cutAt = index
        }
        return substring(0, cutAt)
            .trimEnd()
            .removeCardMaskOnlyTail()
            .normalizeStatementSpaces()
    }

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

        val PAGE_FOOTER_REGEX = Regex(
            """\s+страница\s+\d+\s+из\s+\d+""",
            RegexOption.IGNORE_CASE,
        )

        val FOOTER_TEXT_MARKERS = listOf(
            "документ подписан",
            "документ сформирован",
            "сведения об электронн",
            "простая электронная подпись",
            "владелец:",
            "сертификат:",
            "издатель:",
            "действителен:",
            "серийный номер",
        )

        val NOISE_LINE_REGEXES = listOf(
            Regex("""^страница\s+\d+\s+из\s+\d+$""", RegexOption.IGNORE_CASE),
            Regex("""^\d+\s+из\s+\d+$"""),
            Regex("""^--\s*\d+\s+(?:of|из)\s+\d+\s*--$""", RegexOption.IGNORE_CASE),
            Regex("""^документ\s+подписан\b.*""", RegexOption.IGNORE_CASE),
            Regex("""^документ\s+сформирован\b.*""", RegexOption.IGNORE_CASE),
            Regex("""^сведения\s+об\s+электронн.*""", RegexOption.IGNORE_CASE),
            Regex("""^простая\s+электронная\s+подпись\b.*""", RegexOption.IGNORE_CASE),
            Regex("""^владелец\b.*""", RegexOption.IGNORE_CASE),
            Regex("""^сертификат\b.*""", RegexOption.IGNORE_CASE),
            Regex("""^издатель\b.*""", RegexOption.IGNORE_CASE),
            Regex("""^действителен\b.*""", RegexOption.IGNORE_CASE),
            Regex("""^серийн\w*\s+номер\b.*""", RegexOption.IGNORE_CASE),
            Regex("""^уц\s+.*""", RegexOption.IGNORE_CASE),
            Regex("""^\*{4}\d{4}$"""),
        )
    }
}
