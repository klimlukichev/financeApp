package ru.rsreu.klimlukichev.financeapp.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.rsreu.klimlukichev.financeapp.data.importing.BankStatementParser
import ru.rsreu.klimlukichev.financeapp.data.importing.BankStatementParserFactory
import ru.rsreu.klimlukichev.financeapp.data.importing.OfflineBankStatementImportRepository
import ru.rsreu.klimlukichev.financeapp.data.importing.PdfTextExtractor
import ru.rsreu.klimlukichev.financeapp.data.importing.SberStatementParser
import ru.rsreu.klimlukichev.financeapp.data.importing.TBankStatementParser
import ru.rsreu.klimlukichev.financeapp.data.local.DatabaseSeeder
import ru.rsreu.klimlukichev.financeapp.data.local.FinanceDatabase
import ru.rsreu.klimlukichev.financeapp.data.local.themeSettingsDataStore
import ru.rsreu.klimlukichev.financeapp.data.repository.DataStoreBudgetRepository
import ru.rsreu.klimlukichev.financeapp.data.repository.DataStoreKeywordCategoryRepository
import ru.rsreu.klimlukichev.financeapp.data.repository.DataStoreThemeRepository
import ru.rsreu.klimlukichev.financeapp.data.repository.OfflineCategoryRepository
import ru.rsreu.klimlukichev.financeapp.data.repository.OfflineTransactionRepository
import ru.rsreu.klimlukichev.financeapp.domain.analytics.AnalyticsExpenseFilter
import ru.rsreu.klimlukichev.financeapp.domain.categorization.TransactionCategorizer
import ru.rsreu.klimlukichev.financeapp.domain.importing.BankStatementImportRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.BudgetRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.KeywordCategoryRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.ThemeRepository
import ru.rsreu.klimlukichev.financeapp.domain.repository.TransactionRepository
import ru.rsreu.klimlukichev.financeapp.domain.usecase.AddTransactionUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.CheckWeeklyBudgetUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.CategorizeByKeywordsUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.ExportTransactionsUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.ExportPdfReportUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.GetCategorySpendingUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.GetLastTransactionsUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.ImportBankStatementUseCase
import ru.rsreu.klimlukichev.financeapp.domain.usecase.RememberCategoryCorrectionUseCase
import ru.rsreu.klimlukichev.financeapp.notifications.FinanceNotificationManager
import ru.rsreu.klimlukichev.financeapp.ui.home.HomeViewModel

private val Context.keywordCategoryDataStore by preferencesDataStore(name = "keyword_categories")
private val Context.budgetSettingsDataStore by preferencesDataStore(name = "budget_settings")

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            FinanceDatabase::class.java,
            FinanceDatabase.DATABASE_NAME,
        )
            .addMigrations(FinanceDatabase.MIGRATION_1_2)
            .build()
    }

    single { get<FinanceDatabase>().transactionDao() }
    single { get<FinanceDatabase>().categoryDao() }

    single<TransactionRepository> { OfflineTransactionRepository(get()) }
    single<CategoryRepository> { OfflineCategoryRepository(get()) }
    single<KeywordCategoryRepository> {
        DataStoreKeywordCategoryRepository(androidContext().keywordCategoryDataStore)
    }
    single<BudgetRepository> {
        DataStoreBudgetRepository(androidContext().budgetSettingsDataStore)
    }
    single<ThemeRepository> {
        DataStoreThemeRepository(androidContext().themeSettingsDataStore)
    }
    single { PdfTextExtractor(androidContext()) }
    single<List<BankStatementParser>> { listOf(TBankStatementParser(), SberStatementParser()) }
    single { BankStatementParserFactory(get()) }
    single<BankStatementImportRepository> { OfflineBankStatementImportRepository(get(), get()) }
    single { FinanceNotificationManager(androidContext()) }
    single { TransactionCategorizer() }
    single { AnalyticsExpenseFilter() }

    single { DatabaseSeeder(get()) }

    factory { GetLastTransactionsUseCase(get()) }
    factory { GetCategorySpendingUseCase(get(), get(), get()) }
    factory { AddTransactionUseCase(get()) }
    factory { CategorizeByKeywordsUseCase(get(), get()) }
    factory { ExportTransactionsUseCase(get(), get()) }
    factory { ExportPdfReportUseCase(get(), get(), get()) }
    factory { CheckWeeklyBudgetUseCase(get(), get(), get(), get()) }
    factory { ImportBankStatementUseCase(get(), get(), get(), get()) }
    factory { RememberCategoryCorrectionUseCase(get(), get()) }

    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
