package ru.rsreu.klimlukichev.financeapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.rsreu.klimlukichev.financeapp.domain.model.CategorySpending
import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction

interface TransactionRepository {

    suspend fun insert(transaction: Transaction): Long

    suspend fun insertAllIgnore(transactions: List<Transaction>): List<Long>

    fun observeByPeriod(startDate: Long, endDate: Long): Flow<List<Transaction>>

    fun observeRecent(limit: Int): Flow<List<Transaction>>

    fun observeCategorySpending(startDate: Long, endDate: Long): Flow<List<CategorySpending>>

    suspend fun deleteById(id: Long)
}
