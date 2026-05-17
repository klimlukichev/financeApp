package ru.rsreu.klimlukichev.financeapp.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val iconResId: Int,
    val colorInt: Int,
    val isDefault: Boolean,
)
