package ru.rsreu.klimlukichev.financeapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.rsreu.klimlukichev.financeapp.data.importing.SberStatementParser
import ru.rsreu.klimlukichev.financeapp.data.importing.TBankStatementParser
import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType
import java.time.ZoneId

class BankStatementParsersTest {

    private val zoneId = ZoneId.of("Europe/Moscow")

    @Test
    fun `tbank parser extracts income and expense operations`() {
        val text = """
            Выписка по договору №5303360498
            Операции по карте № 553691******3712 ЛУКИЧЕВ КЛИМ
            25.03.26 25.03.26 + 3 000.00 ₽ + 3 000.00 ₽	Внутрибанковский перевод с договора
            5292109476
            01.04.26 21:19 01.04.26 149.99 ₽ 149.99 ₽	Оплата в ПЕРЕКРЕСТОК Рязань _P_QR
            02.04.26 02.04.26 + 1 500.00 + 1 500.00
            Пополнение. Система быстрых платежей
        """.trimIndent()

        val transactions = TBankStatementParser(zoneId).parse(text)

        assertEquals(3, transactions.size)
        assertEquals(TransactionType.INCOME, transactions[0].type)
        assertEquals(3_000.0, transactions[0].amount, 0.001)
        assertTrue(transactions[0].description.contains("5292109476"))
        assertEquals(TransactionType.EXPENSE, transactions[1].type)
        assertEquals(149.99, transactions[1].amount, 0.001)
        assertEquals(TransactionType.INCOME, transactions[2].type)
        assertEquals(1_500.0, transactions[2].amount, 0.001)
        assertTrue(transactions[2].description.contains("Пополнение"))
    }

    @Test
    fun `sber parser joins operation and description rows`() {
        val text = """
            Выписка по счёту дебетовой карты
            За период 19.04.2026 — 18.05.2026
            02.05.2026 16:42 Перевод СБП +5 822,00 5 901.00
            02.05.2026 425469 Перевод от Л. Клим Дмитриевич. Операция по карте
            ****7385
            23.04.2026 21:18 Супермаркеты 415,98 2 091,60
            23.04.2026 645620 GLOBUS TULA_P_QR OSINOVAYA GOR RUS. Операция по
            карте ****7385
        """.trimIndent()

        val transactions = SberStatementParser(zoneId).parse(text)

        assertEquals(2, transactions.size)
        assertEquals(TransactionType.INCOME, transactions[0].type)
        assertEquals(5_822.0, transactions[0].amount, 0.001)
        assertEquals("Перевод СБП", transactions[0].bankCategory)
        assertTrue(transactions[0].description.contains("Перевод от"))
        assertEquals(TransactionType.EXPENSE, transactions[1].type)
        assertEquals(415.98, transactions[1].amount, 0.001)
        assertTrue(transactions[1].description.contains("GLOBUS"))
        assertTrue(transactions[1].description.contains("Операция по карте"))
    }

    @Test
    fun `sber parser ignores signature and page footer lines`() {
        val text = """
            Выписка по счёту дебетовой карты
            За период 19.04.2026 — 18.05.2026
            23.04.2026 21:18 Супермаркеты 415,98 2 091,60
            23.04.2026 645620 GLOBUS TULA_P_QR. Операция по карте
            ****7385
            Страница 1 из 2
            Документ подписан усиленной квалифицированной электронной подписью
            Сведения об электронной подписи
            Владелец: ИВАНОВ ИВАН ИВАНОВИЧ
            Сертификат: 12 34 56 78 90
            24.04.2026 10:15 Прочие расходы 100,00 1 991,60
            24.04.2026 111111 COFFEE SHOP
        """.trimIndent()

        val transactions = SberStatementParser(zoneId).parse(text)

        assertEquals(2, transactions.size)
        assertTrue(transactions[0].description.contains("GLOBUS"))
        assertTrue(transactions[0].description.contains("Операция по карте"))
        assertFalse(transactions[0].description.contains("Страница", ignoreCase = true))
        assertFalse(transactions[0].description.contains("электронн", ignoreCase = true))
        assertFalse(transactions[0].description.contains("Сертификат", ignoreCase = true))
        assertFalse(transactions[0].description.contains("Владелец", ignoreCase = true))
    }

    @Test
    fun `sber parser strips footer merged into one pdf line`() {
        val text = """
            23.04.2026 21:18 Супермаркеты 415,98 2 091,60
            23.04.2026 645620 GLOBUS TULA_P_QR. Операция по карте Страница 1 из 2 Документ подписан усиленной квалифицированной электронной подписью
        """.trimIndent()

        val transactions = SberStatementParser(zoneId).parse(text)

        assertEquals(1, transactions.size)
        assertEquals("GLOBUS TULA_P_QR. Операция по карте", transactions[0].description)
    }
}
