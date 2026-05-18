package ru.rsreu.klimlukichev.financeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.rsreu.klimlukichev.financeapp.data.local.dao.CategoryDao
import ru.rsreu.klimlukichev.financeapp.data.local.dao.TransactionDao
import ru.rsreu.klimlukichev.financeapp.data.local.entity.CategoryEntity
import ru.rsreu.klimlukichev.financeapp.data.local.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "finance_app.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN sourceBank TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN sourceDescription TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN importHash TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_transactions_importHash ON transactions(importHash)")
            }
        }
    }
}
