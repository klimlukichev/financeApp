package ru.rsreu.klimlukichev.financeapp.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object DailyReminderScheduler {

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntilNextReminder().toMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private fun delayUntilNextReminder(): Duration {
        val now = LocalDateTime.now()
        val todayReminder = now.toLocalDate().atTime(REMINDER_TIME)
        val nextReminder = if (now.isBefore(todayReminder)) {
            todayReminder
        } else {
            todayReminder.plusDays(1)
        }
        return Duration.between(now, nextReminder)
    }

    private val REMINDER_TIME = LocalTime.of(20, 0)
    private const val WORK_NAME = "daily_transaction_reminder"
}
