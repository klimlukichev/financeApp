package ru.rsreu.klimlukichev.financeapp.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.context.GlobalContext
import ru.rsreu.klimlukichev.financeapp.domain.usecase.CheckWeeklyBudgetUseCase

class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result =
        runCatching {
            val koin = GlobalContext.get()
            val notificationManager = koin.get<FinanceNotificationManager>()

            notificationManager.showDailyReminder()
            koin.get<CheckWeeklyBudgetUseCase>()()?.let { budgetInfo ->
                notificationManager.showWeeklyBudgetExceeded(budgetInfo)
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
}
