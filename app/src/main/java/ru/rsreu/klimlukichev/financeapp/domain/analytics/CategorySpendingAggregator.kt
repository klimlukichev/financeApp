package ru.rsreu.klimlukichev.financeapp.domain.analytics

import ru.rsreu.klimlukichev.financeapp.domain.model.Category
import ru.rsreu.klimlukichev.financeapp.domain.model.CategorySpending

object CategorySpendingAggregator {

    fun aggregate(
        expenses: List<ScopedExpense>,
        categories: List<Category>,
    ): List<CategorySpending> {
        val totalsByCategoryId = expenses
            .groupBy { it.transaction.categoryId }
            .mapValues { (_, grouped) -> grouped.sumOf { it.transaction.amount } }

        return categories.mapNotNull { category ->
            val total = totalsByCategoryId[category.id] ?: 0.0
            if (total <= 0.0) return@mapNotNull null
            CategorySpending(
                categoryId = category.id,
                categoryName = category.name,
                totalAmount = total,
            )
        }.sortedByDescending { it.totalAmount }
    }
}
