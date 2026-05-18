package ru.rsreu.klimlukichev.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("categoryId"),
        Index("date"),
        Index(value = ["importHash"], unique = true),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val date: Long,
    val categoryId: Long,
    val note: String?,
    @ColumnInfo(defaultValue = "EXPENSE")
    val type: String,
    val sourceBank: String?,
    val sourceDescription: String?,
    val importHash: String?,
)
