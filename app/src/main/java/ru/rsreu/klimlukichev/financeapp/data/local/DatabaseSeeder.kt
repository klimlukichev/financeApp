package ru.rsreu.klimlukichev.financeapp.data.local

import kotlinx.coroutines.flow.first
import ru.rsreu.klimlukichev.financeapp.domain.repository.CategoryRepository

class DatabaseSeeder(
    private val categoryRepository: CategoryRepository,
) {

    suspend fun seedIfEmpty() {
        val existingCategoryNames = categoryRepository.observeAll()
            .first()
            .map { it.name.lowercase() }
            .toSet()

        DefaultCategories.items
            .filter { it.name.lowercase() !in existingCategoryNames }
            .forEach { categoryRepository.insert(it) }
    }
}
