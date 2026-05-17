package ru.rsreu.klimlukichev.financeapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.rsreu.klimlukichev.financeapp.domain.model.Category

interface CategoryRepository {

    suspend fun insert(category: Category): Long

    fun observeAll(): Flow<List<Category>>

    suspend fun getById(id: Long): Category?

    suspend fun getDefaults(): List<Category>
}
