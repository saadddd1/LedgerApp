package com.example.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ledger.data.AppDatabase
import com.example.ledger.data.AuthSession
import com.example.ledger.data.AutoBill
import com.example.ledger.data.AutoBillDao
import com.example.ledger.data.ExpenseRecord
import com.example.ledger.data.ExpenseRecordDao
import com.example.ledger.data.Item
import com.example.ledger.data.ItemDao
import com.example.ledger.data.RecurringExpense
import com.example.ledger.data.RecurringExpenseDao
import com.example.ledger.domain.model.SyncPayload
import com.example.ledger.network.ApiClient
import com.example.ledger.network.SyncUploadRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemViewModel(
    private val itemDao: ItemDao,
    private val autoBillDao: AutoBillDao,
    private val expenseRecordDao: ExpenseRecordDao,
    private val recurringExpenseDao: RecurringExpenseDao
) : ViewModel() {

    private val gson = Gson()
    private var syncing = false

    val items: StateFlow<List<Item>> = itemDao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingBills: StateFlow<List<AutoBill>> = autoBillDao.getPendingBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBills: StateFlow<List<AutoBill>> = autoBillDao.getAllAutoBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseRecords: StateFlow<List<ExpenseRecord>> = expenseRecordDao.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTemplates: StateFlow<List<RecurringExpense>> = recurringExpenseDao.getActiveTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTemplates: StateFlow<List<RecurringExpense>> = recurringExpenseDao.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun exportAllData(): String {
        val items = itemDao.getAllItems().first()
        val autoBills = autoBillDao.getAllAutoBills().first()
        val expenseRecords = expenseRecordDao.getAllRecordsList()
        val recurringExpenses = recurringExpenseDao.getAllTemplatesList()
        val payload = SyncPayload(
            items = items,
            autoBills = autoBills,
            expenseRecords = expenseRecords,
            recurringExpenses = recurringExpenses
        )
        return gson.toJson(payload)
    }

    suspend fun importAllData(json: String) {
        syncing = true
        try {
            val payload = gson.fromJson(json, SyncPayload::class.java)
            itemDao.deleteAllItems()
            autoBillDao.deleteAllAutoBills()
            expenseRecordDao.deleteAllRecords()
            recurringExpenseDao.deleteAllTemplates()
            if (payload.items.isNotEmpty()) itemDao.insertAllItems(payload.items)
            if (payload.autoBills.isNotEmpty()) autoBillDao.insertAllAutoBills(payload.autoBills)
            if (payload.expenseRecords.isNotEmpty()) expenseRecordDao.insertAllRecords(payload.expenseRecords)
            if (payload.recurringExpenses.isNotEmpty()) recurringExpenseDao.insertAllTemplates(payload.recurringExpenses)
        } finally {
            syncing = false
        }
    }

    private suspend fun triggerAutoSync() {
        if (syncing || !AuthSession.isLoggedIn.value) return
        val token = AuthSession.token.value ?: return
        try {
            val json = exportAllData()
            ApiClient.apiService.uploadSyncData("Bearer $token", SyncUploadRequest(json))
            AuthSession.updateLastSyncTime(System.currentTimeMillis())
            AuthSession.publishSyncEvent("自动同步成功")
        } catch (e: Exception) {
            AuthSession.publishSyncEvent("自动同步失败")
        }
    }

    fun addItem(name: String, price: Double, dateMillis: Long, residualValue: Double) {
        viewModelScope.launch {
            itemDao.insertItem(Item(
                name = name, price = price, purchaseDateMillis = dateMillis, residualValue = residualValue
            ))
            triggerAutoSync()
        }
    }

    fun sellItem(item: Item, soldPrice: Double, soldDateMillis: Long) {
        viewModelScope.launch {
            itemDao.updateItem(item.copy(isSold = true, residualValue = soldPrice, soldDateMillis = soldDateMillis))
            triggerAutoSync()
        }
    }

    fun updateItemDetails(item: Item, newName: String, newPrice: Double, newDateMillis: Long) {
        viewModelScope.launch {
            itemDao.updateItem(item.copy(name = newName, price = newPrice, purchaseDateMillis = newDateMillis))
            triggerAutoSync()
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            itemDao.deleteItem(id)
            triggerAutoSync()
        }
    }

    fun dismissAutoBill(bill: AutoBill) {
        viewModelScope.launch {
            autoBillDao.deleteAutoBill(bill.id)
            triggerAutoSync()
        }
    }

    fun updateBillDetails(bill: AutoBill, merchantName: String, amount: Double, timestamp: Long) {
        viewModelScope.launch {
            autoBillDao.updateAutoBill(bill.copy(merchantName = merchantName, amount = amount, timestampMillis = timestamp))
            triggerAutoSync()
        }
    }

    fun convertBillToItem(bill: AutoBill, itemName: String, residual: Double) {
        viewModelScope.launch {
            autoBillDao.updateAutoBill(bill.copy(isProcessed = true))
            itemDao.insertItem(Item(
                name = itemName, price = bill.amount, purchaseDateMillis = bill.timestampMillis, residualValue = residual
            ))
            triggerAutoSync()
        }
    }

    // Expense record operations

    fun addExpenseRecord(name: String, amount: Double, dateMillis: Long, category: String, note: String?) {
        viewModelScope.launch {
            expenseRecordDao.insertRecord(ExpenseRecord(
                name = name, amount = amount, dateMillis = dateMillis,
                category = category, source = "MANUAL", note = note
            ))
            triggerAutoSync()
        }
    }

    fun updateExpenseRecord(record: ExpenseRecord, name: String, amount: Double, dateMillis: Long, category: String, note: String?) {
        viewModelScope.launch {
            expenseRecordDao.updateRecord(record.copy(
                name = name, amount = amount, dateMillis = dateMillis,
                category = category, note = note
            ))
            triggerAutoSync()
        }
    }

    fun deleteExpenseRecord(id: Int) {
        viewModelScope.launch {
            expenseRecordDao.deleteRecord(id)
            triggerAutoSync()
        }
    }

    // Recurring expense operations

    fun addRecurringExpense(
        name: String, amount: Double, category: String,
        period: String, dayOfMonth: Int, startMonth: Int,
        initialDeposit: Double?, note: String?
    ) {
        viewModelScope.launch {
            val templateId = recurringExpenseDao.insertTemplate(RecurringExpense(
                name = name, amount = amount, category = category,
                period = period, dayOfMonth = dayOfMonth, startMonth = startMonth,
                initialDeposit = initialDeposit, note = note
            )).toInt()
            if (initialDeposit != null && initialDeposit > 0) {
                expenseRecordDao.insertRecord(ExpenseRecord(
                    name = "$name (押金)", amount = initialDeposit,
                    dateMillis = System.currentTimeMillis(),
                    category = category, source = "DEPOSIT",
                    templateId = templateId
                ))
            }
            triggerAutoSync()
        }
    }

    fun updateRecurringExpense(
        template: RecurringExpense, name: String, amount: Double,
        category: String, period: String, dayOfMonth: Int, startMonth: Int, note: String?
    ) {
        viewModelScope.launch {
            recurringExpenseDao.updateTemplate(template.copy(
                name = name, amount = amount, category = category,
                period = period, dayOfMonth = dayOfMonth, startMonth = startMonth, note = note
            ))
            triggerAutoSync()
        }
    }

    fun toggleRecurringExpense(template: RecurringExpense) {
        viewModelScope.launch {
            recurringExpenseDao.updateTemplate(template.copy(isActive = !template.isActive))
            triggerAutoSync()
        }
    }

    fun deleteRecurringExpense(id: Int) {
        viewModelScope.launch {
            recurringExpenseDao.deleteTemplate(id)
            triggerAutoSync()
        }
    }

    class Factory(private val db: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ItemViewModel(
                db.itemDao(), db.autoBillDao(),
                db.expenseRecordDao(), db.recurringExpenseDao()
            ) as T
        }
    }
}
