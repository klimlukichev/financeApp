package ru.rsreu.klimlukichev.financeapp.domain.usecase

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.flow.first
import ru.rsreu.klimlukichev.financeapp.domain.analytics.AnalyticsExpenseFilter
import ru.rsreu.klimlukichev.financeapp.domain.analytics.ScopedExpense
import ru.rsreu.klimlukichev.financeapp.domain.model.Category
import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction
import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.TransactionRepository
import ru.rsreu.klimlukichev.financeapp.domain.util.DatePeriodFactory
import java.io.OutputStream
import java.text.NumberFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExportPdfReportUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val analyticsExpenseFilter: AnalyticsExpenseFilter,
) {

    suspend operator fun invoke(
        year: Int,
        month: Int,
        outputStream: OutputStream,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Int {
        val period = DatePeriodFactory.monthOf(year, month, zoneId)
        val categories = categoryRepository.observeAll().first()
        val categoriesById = categories.associateBy { it.id }
        val transactions = transactionRepository.observeByPeriod(
            startDate = period.startInclusive,
            endDate = period.endInclusive,
        ).first().sortedByDescending { it.date }

        val scopedExpenses = transactions.map { transaction ->
            ScopedExpense(
                transaction = transaction,
                categoryName = categoriesById[transaction.categoryId]?.name.orEmpty(),
            )
        }
        val analyticsExpenses = analyticsExpenseFilter
            .filterCounted(scopedExpenses, zoneId)
            .map { it.transaction }

        outputStream.use { stream ->
            val document = PdfDocument()
            try {
                drawReport(
                    document = document,
                    yearMonth = YearMonth.of(year, month),
                    transactions = transactions,
                    analyticsExpenses = analyticsExpenses,
                    categoriesById = categoriesById,
                    zoneId = zoneId,
                )
                document.writeTo(stream)
            } finally {
                document.close()
            }
        }

        return transactions.size
    }

    private fun drawReport(
        document: PdfDocument,
        yearMonth: YearMonth,
        transactions: List<Transaction>,
        analyticsExpenses: List<Transaction>,
        categoriesById: Map<Long, Category>,
        zoneId: ZoneId,
    ) {
        var pageNumber = 1
        var page = document.startPage(createPageInfo(pageNumber))
        var canvas = page.canvas
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 48, 60)
            textSize = 24f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(90, 105, 112)
            textSize = 12f
        }
        val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 48, 60)
            textSize = 15f
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(43, 54, 59)
            textSize = 10f
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(105, 118, 124)
            textSize = 9f
        }

        val monthLabel = yearMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", RU_LOCALE))
            .replaceFirstChar { it.titlecase(RU_LOCALE) }
        canvas.drawText("Финансовый отчет", LEFT, 44f, titlePaint)
        canvas.drawText(monthLabel, LEFT, 64f, subtitlePaint)

        val expenses = analyticsExpenses
        val incomes = transactions.filter { it.type == TransactionType.INCOME }
        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = incomes.sumOf { it.amount }

        drawSummaryCard(canvas, 36f, 86f, "Расходы", totalExpense.formatMoney(), Color.rgb(229, 115, 115))
        drawSummaryCard(canvas, 215f, 86f, "Доходы", totalIncome.formatMoney(), Color.rgb(76, 175, 80))
        drawSummaryCard(canvas, 394f, 86f, "Операции", transactions.size.toString(), Color.rgb(33, 150, 243))

        val stats = expenses
            .groupBy { it.categoryId }
            .map { (categoryId, categoryTransactions) ->
                PdfCategoryStat(
                    categoryName = categoriesById[categoryId]?.name ?: "Без категории",
                    amount = categoryTransactions.sumOf { it.amount },
                    color = categoriesById[categoryId]?.colorInt ?: DEFAULT_CHART_COLORS[categoryId.hashCode().mod(DEFAULT_CHART_COLORS.size)],
                )
            }
            .filter { it.amount > 0.0 }
            .sortedByDescending { it.amount }

        canvas.drawText("Структура расходов", LEFT, 188f, headingPaint)
        if (stats.isEmpty()) {
            canvas.drawText("За выбранный месяц нет расходов.", LEFT, 214f, bodyPaint)
        } else {
            drawPieChart(canvas, stats, totalExpense, 42f, 210f, 180f)
            drawLegend(canvas, stats, totalExpense, 250f, 218f, bodyPaint, mutedPaint)
        }

        canvas.drawText("Операции", LEFT, 446f, headingPaint)
        var y = drawTransactionsHeader(canvas, 468f)

        transactions.forEach { transaction ->
            if (y > PAGE_HEIGHT - BOTTOM_MARGIN) {
                drawFooter(canvas, pageNumber, mutedPaint)
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(createPageInfo(pageNumber))
                canvas = page.canvas
                canvas.drawColor(Color.WHITE)
                canvas.drawText("Операции, продолжение", LEFT, 44f, headingPaint)
                y = drawTransactionsHeader(canvas, 68f)
            }

            drawTransactionRow(
                canvas = canvas,
                transaction = transaction,
                categoriesById = categoriesById,
                zoneId = zoneId,
                y = y,
                bodyPaint = bodyPaint,
                mutedPaint = mutedPaint,
            )
            y += TRANSACTION_ROW_HEIGHT
        }

        drawFooter(canvas, pageNumber, mutedPaint)
        document.finishPage(page)
    }

    private fun drawSummaryCard(canvas: Canvas, x: Float, y: Float, label: String, value: String, accentColor: Int) {
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(244, 249, 248)
            style = Paint.Style.FILL
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(90, 105, 112)
            textSize = 10f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 48, 60)
            textSize = 16f
            isFakeBoldText = true
        }

        val rect = RectF(x, y, x + 158f, y + 64f)
        canvas.drawRoundRect(rect, 18f, 18f, cardPaint)
        canvas.drawCircle(x + 18f, y + 21f, 5f, accentPaint)
        canvas.drawText(label, x + 32f, y + 25f, labelPaint)
        canvas.drawText(value, x + 18f, y + 50f, valuePaint)
    }

    private fun drawPieChart(canvas: Canvas, stats: List<PdfCategoryStat>, total: Double, x: Float, y: Float, size: Float) {
        val oval = RectF(x, y, x + size, y + size)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        var startAngle = -90f

        stats.forEach { stat ->
            val sweep = (stat.amount / total * 360.0).toFloat()
            paint.color = stat.color
            canvas.drawArc(oval, startAngle, sweep, true, paint)
            startAngle += sweep
        }

        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x + size / 2f, y + size / 2f, size * 0.28f, centerPaint)
    }

    private fun drawLegend(
        canvas: Canvas,
        stats: List<PdfCategoryStat>,
        total: Double,
        x: Float,
        y: Float,
        bodyPaint: Paint,
        mutedPaint: Paint,
    ) {
        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        var currentY = y

        stats.take(8).forEach { stat ->
            markerPaint.color = stat.color
            canvas.drawCircle(x, currentY - 4f, 5f, markerPaint)
            canvas.drawText(stat.categoryName, x + 14f, currentY, bodyPaint)
            canvas.drawText(
                "${stat.amount.formatMoney()} • ${stat.amount.percentOf(total)}",
                x + 14f,
                currentY + 14f,
                mutedPaint,
            )
            currentY += 31f
        }
    }

    private fun drawTransactionsHeader(
        canvas: Canvas,
        startY: Float,
    ): Float {
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 235, 235)
            strokeWidth = 1f
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 48, 60)
            textSize = 9f
            isFakeBoldText = true
        }
        var y = startY

        canvas.drawText("Дата", LEFT, y, headerPaint)
        canvas.drawText("Категория", 118f, y, headerPaint)
        canvas.drawText("Описание", 240f, y, headerPaint)
        canvas.drawText("Сумма", 500f, y, headerPaint)
        y += 10f
        canvas.drawLine(LEFT, y, PAGE_WIDTH - LEFT, y, linePaint)
        y += 16f
        return y
    }

    private fun drawTransactionRow(
        canvas: Canvas,
        transaction: Transaction,
        categoriesById: Map<Long, Category>,
        zoneId: ZoneId,
        y: Float,
        bodyPaint: Paint,
        mutedPaint: Paint,
    ) {
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 235, 235)
            strokeWidth = 1f
        }
        val incomePaint = Paint(bodyPaint).apply { color = Color.rgb(46, 125, 50) }
        val expensePaint = Paint(bodyPaint).apply { color = Color.rgb(198, 40, 40) }
        val date = Instant.ofEpochMilli(transaction.date)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
        val categoryName = categoriesById[transaction.categoryId]?.name ?: "—"
        val description = transaction.note.orEmpty().ifBlank { transaction.sourceDescription.orEmpty() }
        val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
        val amountPaint = if (transaction.type == TransactionType.INCOME) incomePaint else expensePaint

        canvas.drawText(date, LEFT, y, bodyPaint)
        canvas.drawText(categoryName.ellipsize(18), 118f, y, bodyPaint)
        canvas.drawText(description.ellipsize(38), 240f, y, mutedPaint)
        canvas.drawText(amountPrefix + transaction.amount.formatMoney(), 500f, y, amountPaint)
        canvas.drawLine(LEFT, y + 9f, PAGE_WIDTH - LEFT, y + 9f, linePaint)
    }

    private fun drawFooter(canvas: Canvas, pageNumber: Int, mutedPaint: Paint) {
        canvas.drawText(
            "Сформировано локально в приложении Финансы",
            LEFT,
            PAGE_HEIGHT - 18f,
            mutedPaint,
        )
        canvas.drawText(
            "Страница $pageNumber",
            PAGE_WIDTH - 92f,
            PAGE_HEIGHT - 18f,
            mutedPaint,
        )
    }

    private fun createPageInfo(pageNumber: Int): PdfDocument.PageInfo =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()

    private fun Double.formatMoney(): String =
        currencyFormat.format(this)

    private fun Double.percentOf(total: Double): String =
        if (total <= 0.0) "0%" else "${(this / total * 100.0).toInt()}%"

    private fun String.ellipsize(maxLength: Int): String =
        if (length <= maxLength) this else take(maxLength - 1) + "…"

    private fun Int.mod(size: Int): Int =
        Math.floorMod(this, size)

    private data class PdfCategoryStat(
        val categoryName: String,
        val amount: Double,
        val color: Int,
    )

    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val LEFT = 36f
        const val BOTTOM_MARGIN = 48f
        const val TRANSACTION_ROW_HEIGHT = 18f

        val RU_LOCALE: Locale = Locale.forLanguageTag("ru-RU")
        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(RU_LOCALE).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
        val DEFAULT_CHART_COLORS = listOf(
            Color.rgb(229, 115, 115),
            Color.rgb(100, 181, 246),
            Color.rgb(129, 199, 132),
            Color.rgb(255, 183, 77),
            Color.rgb(186, 104, 200),
            Color.rgb(77, 182, 172),
        )
    }
}
