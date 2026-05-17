package ru.rsreu.klimlukichev.financeapp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.rsreu.klimlukichev.financeapp.data.local.dao.CategoryDao
import ru.rsreu.klimlukichev.financeapp.data.mapper.toDomain
import ru.rsreu.klimlukichev.financeapp.data.mapper.toEntity
import ru.rsreu.klimlukichev.financeapp.domain.model.Category
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository

class OfflineCategoryRepository(
    private val categoryDao: CategoryDao,
) : CategoryRepository {

    override suspend fun insert(category: Category): Long =
        categoryDao.insert(category.toEntity())

    override fun observeAll(): Flow<List<Category>> =
        categoryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): Category? =
        categoryDao.getById(id)?.toDomain()

    override suspend fun getDefaults(): List<Category> =
        categoryDao.getDefaults().map { it.toDomain() }
}
