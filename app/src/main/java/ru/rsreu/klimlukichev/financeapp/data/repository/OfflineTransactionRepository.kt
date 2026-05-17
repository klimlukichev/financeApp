package ru.rsreu.klimlukichev.financeapp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.rsreu.klimlukichev.financeapp.data.local.dao.TransactionDao
import ru.rsreu.klimlukichev.financeapp.data.mapper.toCategorySpending
import ru.rsreu.klimlukichev.financeapp.data.mapper.toDomain
import ru.rsreu.klimlukichev.financeapp.data.mapper.toEntity
import ru.rsreu.klimlukichev.financeapp.domain.model.CategorySpending
import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction
import ru.rsreu.klimlukichev.financeapp.domain.repository.TransactionRepository

class OfflineTransactionRepository(
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    override suspend fun insert(transaction: Transaction): Long =
        transactionDao.insert(transaction.toEntity())

    override fun observeByPeriod(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.observeByPeriod(startDate, endDate)
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        transactionDao.observeRecent(limit)
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeCategorySpending(
        startDate: Long,
        endDate: Long,
    ): Flow<List<CategorySpending>> =
        transactionDao.observeSumByCategories(startDate, endDate)
            .map { entities -> entities.map { it.toCategorySpending() } }

    override suspend fun deleteById(id: Long) {
        transactionDao.deleteById(id)
    }
}
