package com.example.ledger.domain.model

import com.example.ledger.data.AutoBill
import com.example.ledger.data.ExpenseRecord
import com.example.ledger.data.Item
import com.example.ledger.data.RecurringExpense

data class SyncPayload(
    val items: List<Item>,
    val autoBills: List<AutoBill>,
    val expenseRecords: List<ExpenseRecord>? = null,
    val recurringExpenses: List<RecurringExpense>? = null
)
