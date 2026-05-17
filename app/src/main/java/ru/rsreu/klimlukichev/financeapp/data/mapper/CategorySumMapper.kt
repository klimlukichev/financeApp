package ru.rsreu.klimlukichev.financeapp.data.mapper

import ru.rsreu.klimlukichev.financeapp.data.local.entity.CategorySumEntity
import ru.rsreu.klimlukichev.financeapp.domain.model.CategorySpending

fun CategorySumEntity.toCategorySpending(): CategorySpending = CategorySpending(
    categoryId = categoryId,
    categoryName = categoryName,
    totalAmount = totalAmount,
)
