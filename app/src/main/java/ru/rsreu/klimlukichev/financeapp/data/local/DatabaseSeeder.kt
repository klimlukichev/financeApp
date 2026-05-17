package ru.rsreu.klimlukichev.financeapp.data.local

import kotlinx.coroutines.flow.first
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository

class DatabaseSeeder(
    private val categoryRepository: CategoryRepository,
) {

    suspend fun seedIfEmpty() {
        if (categoryRepository.observeAll().first().isEmpty()) {
            DefaultCategories.items.forEach { categoryRepository.insert(it) }
        }
    }
}
