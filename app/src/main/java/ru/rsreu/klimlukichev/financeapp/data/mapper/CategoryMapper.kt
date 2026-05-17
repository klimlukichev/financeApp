package ru.rsreu.klimlukichev.financeapp.data.mapper

import ru.rsreu.klimlukichev.financeapp.data.local.entity.CategoryEntity
import ru.rsreu.klimlukichev.financeapp.domain.model.Category

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    iconResId = iconResId,
    colorInt = colorInt,
    isDefault = isDefault,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    iconResId = iconResId,
    colorInt = colorInt,
    isDefault = isDefault,
)
