package com.example.ledger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Item::class, AutoBill::class, ExpenseRecord::class, RecurringExpense::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun autoBillDao(): AutoBillDao
    abstract fun expenseRecordDao(): ExpenseRecordDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS recurring_expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        category TEXT NOT NULL DEFAULT '其他',
                        period TEXT NOT NULL DEFAULT 'MONTHLY',
                        dayOfMonth INTEGER NOT NULL DEFAULT 1,
                        startMonth INTEGER NOT NULL DEFAULT 1,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        initialDeposit REAL,
                        note TEXT
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_expenses_isActive ON recurring_expenses(isActive)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS expense_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        dateMillis INTEGER NOT NULL,
                        category TEXT NOT NULL DEFAULT '其他',
                        source TEXT NOT NULL DEFAULT 'MANUAL',
                        templateId INTEGER,
                        note TEXT,
                        FOREIGN KEY (templateId) REFERENCES recurring_expenses(id) ON DELETE SET NULL
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_records_dateMillis ON expense_records(dateMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_records_templateId ON expense_records(templateId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_records_dateMillis_source ON expense_records(dateMillis, source)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ledger_database"
                )
                .addMigrations(MIGRATION_5_6)
                .fallbackToDestructiveMigration(false)
                .enableMultiInstanceInvalidation()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
