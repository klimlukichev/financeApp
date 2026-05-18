package ru.rsreu.klimlukichev.financeapp.data.mapper

import ru.rsreu.klimlukichev.financeapp.data.local.entity.TransactionEntity
import ru.rsreu.klimlukichev.financeapp.domain.model.Transaction
import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = amount,
    date = date,
    categoryId = categoryId,
    note = note,
    type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE),
    sourceBank = sourceBank,
    sourceDescription = sourceDescription,
    importHash = importHash,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    date = date,
    categoryId = categoryId,
    note = note,
    type = type.name,
    sourceBank = sourceBank,
    sourceDescription = sourceDescription,
    importHash = importHash,
)
