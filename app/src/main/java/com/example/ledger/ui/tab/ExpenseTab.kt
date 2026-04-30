package com.example.ledger.ui.tab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ledger.data.ExpenseRecord
import com.example.ledger.ui.component.ExpenseRecordCard

@Composable
fun ExpenseTab(
    expenseRecords: List<ExpenseRecord>,
    thisMonthTotal: Double,
    onDelete: (Int) -> Unit,
    onEdit: (ExpenseRecord) -> Unit,
    onManageRecurring: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (expenseRecords.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "还没有记过一笔生活开销\n从今天开始记账吧",
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
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "本月生活开销",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "¥${String.format("%.2f", thisMonthTotal)}",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FilledTonalButton(onClick = onManageRecurring) {
                            Text("管理周期账单", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            items(expenseRecords, key = { it.id }) { record ->
                ExpenseRecordCard(
                    record = record,
                    onDelete = { onDelete(record.id) },
                    onEdit = { onEdit(record) }
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
