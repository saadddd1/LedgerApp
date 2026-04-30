package com.example.ledger.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseRecordDao {
    @Query("SELECT * FROM expense_records ORDER BY dateMillis DESC")
    fun getAllRecords(): Flow<List<ExpenseRecord>>

    @Query("SELECT * FROM expense_records WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getRecordsBetween(startMillis: Long, endMillis: Long): Flow<List<ExpenseRecord>>

    @Insert
    suspend fun insertRecord(record: ExpenseRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRecords(records: List<ExpenseRecord>)

    @Update
    suspend fun updateRecord(record: ExpenseRecord)

    @Query("DELETE FROM expense_records WHERE id = :recordId")
    suspend fun deleteRecord(recordId: Int)

    @Query("DELETE FROM expense_records")
    suspend fun deleteAllRecords()

    @Query("SELECT COUNT(*) FROM expense_records")
    suspend fun getCount(): Int

    @Query("SELECT * FROM expense_records ORDER BY dateMillis DESC")
    suspend fun getAllRecordsList(): List<ExpenseRecord>

    @Query("SELECT COUNT(*) FROM expense_records WHERE templateId = :templateId AND dateMillis = :dateMillis AND source = 'SCHEDULED'")
    suspend fun countScheduledRecordsForTemplateOnDate(templateId: Int, dateMillis: Long): Int
}
