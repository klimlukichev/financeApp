package ru.rsreu.klimlukichev.financeapp.data.local.entity

import androidx.room.ColumnInfo

/**
 * Результат агрегирующего запроса: сумма транзакций по категории за период.
 */
data class CategorySumEntity(
    val categoryId: Long,
    val categoryName: String,
    @ColumnInfo(name = "totalAmount")
    val totalAmount: Double,
)
