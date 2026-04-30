package com.example.ledger.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ledger.data.ExpenseRecord
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val categories = listOf("居住", "通讯", "交通", "订阅", "其他")

@Composable
fun EditExpenseDialog(
    record: ExpenseRecord,
    onDismiss: () -> Unit,
    onEdit: (name: String, amount: Double, dateMillis: Long, category: String, note: String?) -> Unit
) {
    val zone = ZoneId.systemDefault()
    val dateInstant = Instant.ofEpochMilli(record.dateMillis)
    val dateStr = dateInstant.atZone(zone).toLocalDate().format(dateFormatter)

    var name by remember { mutableStateOf(record.name) }
    var amountStr by remember { mutableStateOf(String.format("%.2f", record.amount)) }
    var date by remember { mutableStateOf(dateStr) }
    var category by remember { mutableStateOf(record.category) }
    var note by remember { mutableStateOf(record.note ?: "") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(14.dp),
        title = {
            Text(
                "改一下开销",
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("花了什么钱") },
                    singleLine = true,
                    isError = isError && name.isBlank(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("花了多少") },
                    singleLine = true,
                    isError = isError && (amountStr.toDoubleOrNull() ?: 0.0) <= 0,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("什么时候花的") },
                    singleLine = true,
                    isError = isError && runCatching { LocalDate.parse(date, dateFormatter) }.isFailure,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
                Text("类别", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注 (选填)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val dateMillis = try {
                    LocalDate.parse(date, dateFormatter).atStartOfDay(zone).toInstant().toEpochMilli()
                } catch (e: Exception) { 0L }
                if (name.isNotBlank() && amount > 0 && dateMillis > 0) {
                    onEdit(name, amount, dateMillis, category, note.ifBlank { null })
                } else isError = true
            }) { Text("改好了", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("算了") } }
    )
}
