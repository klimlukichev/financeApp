package ru.rsreu.klimlukichev.financeapp.data.importing

import kotlin.math.abs

internal fun String.parseStatementAmount(): Double {
    val normalized = replace('\u00A0', ' ')
        .replace('\u202F', ' ')
        .replace('−', '-')
        .replace(Regex("[^0-9,.-]"), "")
        .replace(",", ".")
    return abs(normalized.toDouble())
}

internal fun String.normalizeStatementText(): String =
    replace('\u00A0', ' ')
        .replace('\u202F', ' ')
        .replace(Regex("[\\t ]+"), " ")
        .replace(Regex("\\s*\\n\\s*"), "\n")
        .trim()

internal fun String.normalizeStatementSpaces(): String =
    replace('\u00A0', ' ')
        .replace('\u202F', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
