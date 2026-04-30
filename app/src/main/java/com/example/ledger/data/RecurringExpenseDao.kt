package com.example.ledger.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveTemplates(): Flow<List<RecurringExpense>>

    @Query("SELECT * FROM recurring_expenses ORDER BY id ASC")
    fun getAllTemplates(): Flow<List<RecurringExpense>>

    @Insert
    suspend fun insertTemplate(template: RecurringExpense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTemplates(templates: List<RecurringExpense>)

    @Update
    suspend fun updateTemplate(template: RecurringExpense)

    @Query("DELETE FROM recurring_expenses WHERE id = :templateId")
    suspend fun deleteTemplate(templateId: Int)

    @Query("DELETE FROM recurring_expenses")
    suspend fun deleteAllTemplates()

    @Query("SELECT COUNT(*) FROM recurring_expenses")
    suspend fun getCount(): Int

    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 ORDER BY id ASC")
    suspend fun getActiveTemplatesList(): List<RecurringExpense>

    @Query("SELECT * FROM recurring_expenses ORDER BY id ASC")
    suspend fun getAllTemplatesList(): List<RecurringExpense>
}
