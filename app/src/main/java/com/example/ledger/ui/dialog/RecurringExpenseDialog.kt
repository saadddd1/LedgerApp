package com.example.ledger.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ledger.data.RecurringExpense

private val categories = listOf("居住", "通讯", "交通", "订阅", "其他")
private val periods = listOf(
    Triple("MONTHLY", "每月", "每个月固定扣款"),
    Triple("QUARTERLY", "每季度", "每三个月扣一次"),
    Triple("YEARLY", "每年", "每年扣一次")
)

@Composable
fun RecurringExpenseDialog(
    template: RecurringExpense? = null,
    onDismiss: () -> Unit,
    onAdd: (name: String, amount: Double, category: String, period: String, dayOfMonth: Int, startMonth: Int, initialDeposit: Double?, note: String?) -> Unit,
    onEdit: (template: RecurringExpense, name: String, amount: Double, category: String, period: String, dayOfMonth: Int, startMonth: Int, note: String?) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    val isEdit = template != null
    var name by remember { mutableStateOf(template?.name ?: "") }
    var amountStr by remember { mutableStateOf(if (template != null) String.format("%.2f", template.amount) else "") }
    var category by remember { mutableStateOf(template?.category ?: "其他") }
    var period by remember { mutableStateOf(template?.period ?: "MONTHLY") }
    var dayOfMonth by remember { mutableStateOf((template?.dayOfMonth ?: 1).toString()) }
    var startMonth by remember { mutableStateOf((template?.startMonth ?: 1).toString()) }
    var depositStr by remember { mutableStateOf(if (template?.initialDeposit != null) String.format("%.2f", template.initialDeposit) else "") }
    var note by remember { mutableStateOf(template?.note ?: "") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(14.dp),
        title = {
            Text(
                if (isEdit) "修改周期账单" else "设置周期账单",
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
                    label = { Text("账单名称") },
                    singleLine = true,
                    isError = isError && name.isBlank(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("金额") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError && (amountStr.toDoubleOrNull() ?: 0.0) <= 0,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                Text("类别", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
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
                Text("周期", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    periods.forEach { (key, label, _) ->
                        FilterChip(
                            selected = period == key,
                            onClick = { period = key },
                            label = { Text(label) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dayOfMonth,
                        onValueChange = { dayOfMonth = it.filter { c -> c.isDigit() } },
                        label = { Text("每月几号") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = isError && (dayOfMonth.toIntOrNull() ?: 0) !in 1..28,
                        modifier = Modifier.weight(1f)
                    )
                    if (period != "MONTHLY") {
                        OutlinedTextField(
                            value = startMonth,
                            onValueChange = { startMonth = it.filter { c -> c.isDigit() } },
                            label = { Text("起始月") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = isError && (startMonth.toIntOrNull() ?: 0) !in 1..12,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (!isEdit) {
                    OutlinedTextField(
                        value = depositStr,
                        onValueChange = { depositStr = it },
                        label = { Text("押金/初始费用 (选填)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
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
                val day = dayOfMonth.toIntOrNull() ?: 0
                val sMonth = startMonth.toIntOrNull() ?: 1
                val deposit = depositStr.toDoubleOrNull()
                if (name.isNotBlank() && amount > 0 && day in 1..28 && sMonth in 1..12) {
                    if (template != null) {
                        onEdit(template, name, amount, category, period, day, sMonth, note.ifBlank { null })
                    } else {
                        onAdd(name, amount, category, period, day, sMonth, deposit, note.ifBlank { null })
                    }
                } else isError = true
            }) { Text(if (isEdit) "改好了" else "设好了", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("算了") } }
    )
}
