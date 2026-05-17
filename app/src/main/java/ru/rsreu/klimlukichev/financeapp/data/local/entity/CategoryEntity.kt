package ru.rsreu.klimlukichev.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconResId: Int,
    val colorInt: Int,
    val isDefault: Boolean,
)
