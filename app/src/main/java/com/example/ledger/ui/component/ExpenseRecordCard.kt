package com.example.ledger.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ledger.data.ExpenseRecord
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

private val categoryColors = mapOf(
    "居住" to androidx.compose.ui.graphics.Color(0xFFFF6B35),
    "通讯" to androidx.compose.ui.graphics.Color(0xFF007AFF),
    "交通" to androidx.compose.ui.graphics.Color(0xFF34C759),
    "订阅" to androidx.compose.ui.graphics.Color(0xFFAF52DE),
    "其他" to androidx.compose.ui.graphics.Color(0xFF8E8E93)
)

private val sourceLabels = mapOf(
    "MANUAL" to "手动录入",
    "SCHEDULED" to "自动记录",
    "DEPOSIT" to "押金"
)

@Composable
fun ExpenseRecordCard(
    record: ExpenseRecord,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val zone = ZoneId.systemDefault()
    val dateStr = Instant.ofEpochMilli(record.dateMillis).atZone(zone).toLocalDate().format(dateFormatter)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(176.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = (categoryColors[record.category] ?: categoryColors["其他"]!!).copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            record.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColors[record.category] ?: categoryColors["其他"]!!,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Box(contentAlignment = Alignment.TopEnd) {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "更多", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("改一下", style = MaterialTheme.typography.bodyMedium) },
                            onClick = { expanded = false; onEdit() },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text("删掉", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) },
                            onClick = { expanded = false; onDelete() },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    sourceLabels[record.source] ?: record.source,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "¥${String.format("%.2f", record.amount)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
