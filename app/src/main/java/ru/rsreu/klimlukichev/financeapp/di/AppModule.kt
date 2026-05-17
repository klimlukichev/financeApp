package ru.rsreu.klimlukichev.financeapp.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.rsreu.klimlukichev.financeapp.data.local.DatabaseSeeder
import ru.rsreu.klimlukichev.financeapp.data.local.FinanceDatabase
import ru.rsreu.klimlukichev.financeapp.data.repository.OfflineCategoryRepository
import ru.rsreu.klimlukichev.financeapp.data.repository.OfflineTransactionRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.TransactionRepository
import ru.rsreu.klimlukichev.financeapp.domain.usecase.AddTransactionUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.GetCategorySpendingUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.GetLastTransactionsUseCase
import ru.rsreu.klimlukichev.financeapp.ui.home.HomeViewModel

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            FinanceDatabase::class.java,
            FinanceDatabase.DATABASE_NAME,
        ).build()
    }

    single { get<FinanceDatabase>().transactionDao() }
    single { get<FinanceDatabase>().categoryDao() }

    single<TransactionRepository> { OfflineTransactionRepository(get()) }
    single<CategoryRepository> { OfflineCategoryRepository(get()) }

    single { DatabaseSeeder(get()) }

    factory { GetLastTransactionsUseCase(get()) }
    factory { GetCategorySpendingUseCase(get()) }
    factory { AddTransactionUseCase(get()) }

    viewModel { HomeViewModel(get(), get(), get(), get()) }
}
