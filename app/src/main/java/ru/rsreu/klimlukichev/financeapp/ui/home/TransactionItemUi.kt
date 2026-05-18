package ru.rsreu.klimlukichev.financeapp.ui.home

import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType

data class TransactionItemUi(
    val id: Long,
    val amount: Double,
    val date: Long,
    val categoryId: Long,
    val categoryName: String,
    val note: String?,
    val colorInt: Int,
    val type: TransactionType,
    val sourceBank: String?,
    val sourceDescription: String?,
    val importHash: String?,
)
