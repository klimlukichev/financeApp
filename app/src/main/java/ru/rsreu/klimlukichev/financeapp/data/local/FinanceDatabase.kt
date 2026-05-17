package ru.rsreu.klimlukichev.financeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.rsreu.klimlukichev.financeapp.data.local.dao.CategoryDao
import ru.rsreu.klimlukichev.financeapp.data.local.dao.TransactionDao
import ru.rsreu.klimlukichev.financeapp.data.local.entity.CategoryEntity
import ru.rsreu.klimlukichev.financeapp.data.local.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "finance_app.db"
    }
}
