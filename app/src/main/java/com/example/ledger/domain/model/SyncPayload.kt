package com.example.ledger.domain.model

import com.example.ledger.data.AutoBill
import com.example.ledger.data.Item

data class SyncPayload(
    val items: List<Item>,
    val autoBills: List<AutoBill>
)
