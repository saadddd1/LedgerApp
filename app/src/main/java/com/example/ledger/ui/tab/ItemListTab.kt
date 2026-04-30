package com.example.ledger.ui.tab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ledger.data.Item
import com.example.ledger.ui.component.ItemCard

@Composable
fun ItemListTab(
    items: List<Item>,
    onDelete: (Int) -> Unit,
    onEdit: (Item) -> Unit,
    onSell: (Item) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "空空如也 — 连一件吃灰的家当都没有？",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items, key = { it.id }) { item ->
                ItemCard(
                    item = item,
                    onDelete = { onDelete(item.id) },
                    onEdit = { onEdit(item) },
                    onSell = { onSell(item) }
                )
            }
        }
    }
}
