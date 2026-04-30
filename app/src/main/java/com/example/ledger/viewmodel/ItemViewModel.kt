package com.example.ledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ledger.data.AppDatabase
import com.example.ledger.data.AuthSession
import com.example.ledger.data.AutoBill
import com.example.ledger.data.AutoBillDao
import com.example.ledger.data.Item
import com.example.ledger.data.ItemDao
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
    private val autoBillDao: AutoBillDao
) : ViewModel() {

    private val gson = Gson()
    private var syncing = false

    val items: StateFlow<List<Item>> = itemDao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingBills: StateFlow<List<AutoBill>> = autoBillDao.getPendingBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBills: StateFlow<List<AutoBill>> = autoBillDao.getAllAutoBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun exportAllData(): String {
        val items = itemDao.getAllItems().first()
        val autoBills = autoBillDao.getAllAutoBills().first()
        val payload = SyncPayload(items = items, autoBills = autoBills)
        return gson.toJson(payload)
    }

    suspend fun importAllData(json: String) {
        syncing = true
        try {
            val payload = gson.fromJson(json, SyncPayload::class.java)
            itemDao.deleteAllItems()
            autoBillDao.deleteAllAutoBills()
            if (payload.items.isNotEmpty()) {
                itemDao.insertAllItems(payload.items)
            }
            if (payload.autoBills.isNotEmpty()) {
                autoBillDao.insertAllAutoBills(payload.autoBills)
            }
        } finally {
            syncing = false
        }
    }

    private suspend fun triggerAutoSync() {
        if (syncing || !AuthSession.isVip.value) return
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

    class Factory(private val db: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ItemViewModel(db.itemDao(), db.autoBillDao()) as T
        }
    }
}
