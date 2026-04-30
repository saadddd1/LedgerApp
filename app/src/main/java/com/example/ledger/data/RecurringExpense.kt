package com.example.ledger.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_expenses",
    indices = [Index(value = ["isActive"])]
)
data class RecurringExpense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val category: String = "其他",
    val period: String = "MONTHLY",
    val dayOfMonth: Int = 1,
    val startMonth: Int = 1,
    val isActive: Boolean = true,
    val initialDeposit: Double? = null,
    val note: String? = null
)
