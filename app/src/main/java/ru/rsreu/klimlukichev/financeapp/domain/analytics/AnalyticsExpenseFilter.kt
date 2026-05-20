package ru.rsreu.klimlukichev.financeapp.domain.analytics

import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction
import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

/**
 * Решает, учитывать ли расход в аналитике (диаграмма, итог месяца, недельный бюджет).
 *
 * Адаптация подхода из статьи про личные финансы: переводы между своими картами
 * раздувают расходы, если считать все исходящие операции подряд. В приложении
 * импортируются только расходы, поэтому «входящее плечо» перевода в БД отсутствует —
 * для одиночных переводов используем явные формулировки, для нескольких банков —
 * эвристику начала цепочки (как в SQL из статьи).
 */
class AnalyticsExpenseFilter {

    fun isCountedInAnalytics(
        expense: ScopedExpense,
        periodExpenses: List<ScopedExpense>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        val transaction = expense.transaction
        if (transaction.type != TransactionType.EXPENSE) return false
        if (isClearlyInternalTransfer(expense)) return false
        if (!isTransferLike(expense)) return true
        return !isInternalTransferChainLeg(expense, periodExpenses, zoneId)
    }

    fun filterCounted(
        periodExpenses: List<ScopedExpense>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<ScopedExpense> =
        periodExpenses.filter { expense ->
            isCountedInAnalytics(expense, periodExpenses, zoneId)
        }

    private fun isClearlyInternalTransfer(expense: ScopedExpense): Boolean {
        val text = expense.combinedText()
        return INTERNAL_TRANSFER_PHRASES.any { phrase -> text.contains(phrase) } ||
            text.contains("vklad", ignoreCase = true)
    }

    private fun isTransferLike(expense: ScopedExpense): Boolean {
        if (expense.categoryName.equals(TRANSFERS_CATEGORY, ignoreCase = true)) return true
        val text = expense.combinedText()
        return TRANSFER_TEXT_MARKERS.any { marker -> text.contains(marker) }
    }

    /**
     * «Звено» внутренней цепочки переводов не должно попадать в аналитику.
     * В аналитику попадает только «начало цепочки» (как в статье).
     */
    private fun isInternalTransferChainLeg(
        expense: ScopedExpense,
        periodExpenses: List<ScopedExpense>,
        zoneId: ZoneId,
    ): Boolean {
        val transaction = expense.transaction
        val day = transaction.toLocalDate(zoneId)
        val amount = transaction.amount
        val bank = transaction.sourceBank?.trim().orEmpty()

        val peers = periodExpenses.filter { peer ->
            peer.transaction.type == TransactionType.EXPENSE &&
                isTransferLike(peer) &&
                peer.transaction.toLocalDate(zoneId) == day &&
                amountsEqual(peer.transaction.amount, amount)
        }

        if (peers.size <= 1) return false

        val otherBankPeers = peers.filter { peer ->
            val peerBank = peer.transaction.sourceBank?.trim().orEmpty()
            bank.isNotEmpty() && peerBank.isNotEmpty() && peerBank != bank
        }
        if (otherBankPeers.isEmpty()) return false

        val chainStartId = peers.minOfOrNull { it.transaction.id }
        val isChainStart = !hasSameBankCounterpart(expense, peers) &&
            otherBankPeers.size % 2 == 0 &&
            expense.transaction.id == chainStartId

        return !isChainStart
    }

    private fun hasSameBankCounterpart(
        expense: ScopedExpense,
        peers: List<ScopedExpense>,
    ): Boolean {
        val bank = expense.transaction.sourceBank?.trim().orEmpty()
        if (bank.isEmpty()) return false
        return peers.any { peer ->
            peer.transaction.id != expense.transaction.id &&
                peer.transaction.sourceBank?.trim().orEmpty() == bank
        }
    }

    private fun ScopedExpense.combinedText(): String =
        listOfNotNull(transaction.note, transaction.sourceDescription)
            .joinToString(" ")
            .lowercase()

    private fun Transaction.toLocalDate(zoneId: ZoneId): LocalDate =
        Instant.ofEpochMilli(date).atZone(zoneId).toLocalDate()

    private fun amountsEqual(left: Double, right: Double): Boolean =
        abs(left - right) < AMOUNT_EPSILON

    private companion object {
        const val TRANSFERS_CATEGORY = "Переводы"
        const val AMOUNT_EPSILON = 0.009

        val INTERNAL_TRANSFER_PHRASES = listOf(
            "перевод между счетами",
            "перевод между счётами",
            "между своими",
            "между счетами",
            "между счётами",
            "между картами",
            "на свой счет",
            "на свой счёт",
            "своему счету",
            "своему счёту",
            "перевод себе",
            "между своими счетами",
            "между своими счётами",
        )

        val TRANSFER_TEXT_MARKERS = listOf(
            "перевод",
            "сбп",
        )
    }
}
