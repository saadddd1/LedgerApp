package com.example.ledger.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_records",
    foreignKeys = [ForeignKey(
        entity = RecurringExpense::class,
        parentColumns = ["id"],
        childColumns = ["templateId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [
        Index(value = ["dateMillis"]),
        Index(value = ["templateId"]),
        Index(value = ["dateMillis", "source"])
    ]
)
data class ExpenseRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val dateMillis: Long,
    val category: String = "其他",
    val source: String = "MANUAL",
    val templateId: Int? = null,
    val note: String? = null
)
