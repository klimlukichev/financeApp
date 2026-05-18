package ru.rsreu.klimlukichev.financeapp

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.rsreu.klimlukichev.financeapp.data.local.DatabaseSeeder
import ru.rsreu.klimlukichev.financeapp.di.appModule
import ru.rsreu.klimlukichev.financeapp.notifications.DailyReminderScheduler
import ru.rsreu.klimlukichev.financeapp.notifications.FinanceNotificationManager

class FinanceApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val koinApp = startKoin {
            androidContext(this@FinanceApplication)
            modules(appModule)
        }
        koinApp.koin.get<FinanceNotificationManager>().createChannels()
        DailyReminderScheduler.schedule(this)
        applicationScope.launch {
            koinApp.koin.get<DatabaseSeeder>().seedIfEmpty()
        }
    }
}
