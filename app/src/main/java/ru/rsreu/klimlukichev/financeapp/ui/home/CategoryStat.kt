package ru.rsreu.klimlukichev.financeapp.ui.home

data class CategoryStat(
    val categoryId: Long,
    val categoryName: String,
    val totalAmount: Double,
    val colorInt: Int,
)
