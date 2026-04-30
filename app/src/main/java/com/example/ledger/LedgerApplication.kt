package com.example.ledger

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ledger.worker.ExpenseAutoRecordWorker
import java.util.concurrent.TimeUnit

class LedgerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleExpenseAutoRecord()
    }

    private fun scheduleExpenseAutoRecord() {
        val request = PeriodicWorkRequestBuilder<ExpenseAutoRecordWorker>(12, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expense_auto_record",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
