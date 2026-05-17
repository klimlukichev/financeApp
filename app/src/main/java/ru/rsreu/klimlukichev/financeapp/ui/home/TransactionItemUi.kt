package ru.rsreu.klimlukichev.financeapp.ui.home

data class TransactionItemUi(
    val id: Long,
    val amount: Double,
    val date: Long,
    val categoryName: String,
    val note: String?,
    val colorInt: Int,
)
