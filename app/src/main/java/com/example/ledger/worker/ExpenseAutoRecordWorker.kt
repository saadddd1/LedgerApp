package com.example.ledger.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ledger.data.AppDatabase
import com.example.ledger.data.ExpenseRecord
import java.time.LocalDate
import java.time.ZoneId

class ExpenseAutoRecordWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val templates = db.recurringExpenseDao().getActiveTemplatesList()
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val todayStartMillis = today.atStartOfDay(zone).toInstant().toEpochMilli()

        for (template in templates) {
            if (!shouldRecordToday(template, today)) continue

            val alreadyRecorded = db.expenseRecordDao()
                .countScheduledRecordsForTemplateOnDate(template.id, todayStartMillis)
            if (alreadyRecorded > 0) continue

            db.expenseRecordDao().insertRecord(
                ExpenseRecord(
                    name = template.name,
                    amount = template.amount,
                    dateMillis = todayStartMillis,
                    category = template.category,
                    source = "SCHEDULED",
                    templateId = template.id
                )
            )
        }
        return Result.success()
    }

    private fun shouldRecordToday(
        template: com.example.ledger.data.RecurringExpense,
        today: LocalDate
    ): Boolean {
        val maxDay = today.lengthOfMonth()
        val effectiveDay = minOf(template.dayOfMonth, maxDay)
        if (today.dayOfMonth != effectiveDay) return false
        return when (template.period) {
            "MONTHLY" -> true
            "QUARTERLY" -> (today.monthValue - template.startMonth) % 3 == 0
            "YEARLY" -> today.monthValue == template.startMonth
            else -> false
        }
    }
}
