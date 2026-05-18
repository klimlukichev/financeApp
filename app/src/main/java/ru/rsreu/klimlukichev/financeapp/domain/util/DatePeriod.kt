package ru.rsreu.klimlukichev.financeapp.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.temporal.WeekFields

data class DatePeriod(
    val startInclusive: Long,
    val endInclusive: Long,
)

object DatePeriodFactory {

    fun monthOf(instantMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): DatePeriod {
        val date = Instant.ofEpochMilli(instantMillis).atZone(zoneId).toLocalDate()
        return monthOf(date.year, date.monthValue, zoneId)
    }

    fun monthOf(year: Int, month: Int, zoneId: ZoneId = ZoneId.systemDefault()): DatePeriod {
        val yearMonth = YearMonth.of(year, month)
        val start = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = yearMonth.atEndOfMonth()
            .atTime(23, 59, 59, 999_000_000)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
        return DatePeriod(startInclusive = start, endInclusive = end)
    }

    fun recentDays(days: Int, zoneId: ZoneId = ZoneId.systemDefault()): DatePeriod {
        val endDate = LocalDate.now(zoneId)
        val startDate = endDate.minusDays(days.toLong() - 1)
        val start = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = endDate.atTime(23, 59, 59, 999_000_000)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
        return DatePeriod(startInclusive = start, endInclusive = end)
    }

    fun weekOf(instantMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): DatePeriod {
        val date = Instant.ofEpochMilli(instantMillis).atZone(zoneId).toLocalDate()
        val startDate = date.with(WeekFields.ISO.dayOfWeek(), 1)
        val endDate = startDate.plusDays(6)
        val start = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = endDate.atTime(23, 59, 59, 999_000_000)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
        return DatePeriod(startInclusive = start, endInclusive = end)
    }

    fun weekKey(instantMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val date = Instant.ofEpochMilli(instantMillis).atZone(zoneId).toLocalDate()
        val weekFields = WeekFields.ISO
        val weekBasedYear = date.get(weekFields.weekBasedYear())
        val weekOfYear = date.get(weekFields.weekOfWeekBasedYear())
        return "$weekBasedYear-W${weekOfYear.toString().padStart(2, '0')}"
    }
}
