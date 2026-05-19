package ru.rsreu.klimlukichev.financeapp.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ru.rsreu.klimlukichev.financeapp.MainActivity
import ru.rsreu.klimlukichev.financeapp.R
import ru.rsreu.klimlukichev.financeapp.domain.model.BudgetExceededInfo
import java.text.NumberFormat
import java.util.Locale

class FinanceNotificationManager(
    private val context: Context,
) {

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notifications_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notifications_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showDailyReminder() {
        showNotification(
            notificationId = DAILY_REMINDER_NOTIFICATION_ID,
            title = context.getString(R.string.daily_reminder_title),
            text = context.getString(R.string.daily_reminder_text),
        )
    }

    fun showWeeklyBudgetExceeded(info: BudgetExceededInfo) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("ru-RU")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
        showNotification(
            notificationId = WEEKLY_BUDGET_NOTIFICATION_ID,
            title = context.getString(R.string.weekly_budget_exceeded_title),
            text = context.getString(
                R.string.weekly_budget_exceeded_text,
                currencyFormat.format(info.spentAmount),
                currencyFormat.format(info.budgetLimit),
            ),
        )
    }

    private fun showNotification(
        notificationId: Int,
        title: String,
        text: String,
    ) {
        if (!canPostNotifications()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(createContentIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val CHANNEL_ID = "finance_reminders"
        const val DAILY_REMINDER_NOTIFICATION_ID = 1001
        const val WEEKLY_BUDGET_NOTIFICATION_ID = 1002
    }
}
