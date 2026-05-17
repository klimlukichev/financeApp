package ru.rsreu.klimlukichev.financeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.rsreu.klimlukichev.financeapp.data.local.entity.CategorySumEntity
import ru.rsreu.klimlukichev.financeapp.data.local.entity.TransactionEntity

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT * FROM transactions
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date DESC
        """,
    )
    fun observeByPeriod(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        ORDER BY date DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT
            c.id AS categoryId,
            c.name AS categoryName,
            COALESCE(SUM(t.amount), 0) AS totalAmount
        FROM categories c
        LEFT JOIN transactions t
            ON t.categoryId = c.id
            AND t.date >= :startDate
            AND t.date <= :endDate
        GROUP BY c.id, c.name
        ORDER BY totalAmount DESC
        """,
    )
    fun observeSumByCategories(startDate: Long, endDate: Long): Flow<List<CategorySumEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?
}
