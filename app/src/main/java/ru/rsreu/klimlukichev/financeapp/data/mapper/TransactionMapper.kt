package ru.rsreu.klimlukichev.financeapp.data.mapper

import ru.rsreu.klimlukichev.financeapp.data.local.entity.TransactionEntity
import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = amount,
    date = date,
    categoryId = categoryId,
    note = note,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    date = date,
    categoryId = categoryId,
    note = note,
)
