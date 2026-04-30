package com.example.ledger.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ledger.data.AutoBill
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

@Composable
fun EditBillDialog(
    bill: AutoBill,
    onDismiss: () -> Unit,
    onEdit: (merchantName: String, amount: Double, timestamp: Long) -> Unit
) {
    var merchantName by remember(bill) { mutableStateOf(bill.merchantName) }
    var amountStr by remember(bill) { mutableStateOf(bill.amount.toString()) }
    var dateStr by remember(bill) {
        mutableStateOf(
            Instant.ofEpochMilli(bill.timestampMillis).atZone(ZoneId.systemDefault()).toLocalDateTime().format(dateFormatter)
        )
    }
    var isError by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(14.dp),
        title = { Text("改一下账单", fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column {
                Text("渠道: ${bill.appSource}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(value = merchantName, onValueChange = { merchantName = it }, label = { Text("在哪儿花的") }, singleLine = true, isError = isError && merchantName.isBlank(), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("花了多少 (¥)") }, singleLine = true, isError = isError && (amountStr.toDoubleOrNull() ?: 0.0) <= 0, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = dateStr, onValueChange = { dateStr = it }, label = { Text("什么时候 (MM-dd HH:mm)") }, singleLine = true, isError = isError && runCatching { dateFormatter.parse(dateStr) }.isFailure, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val dateMillis = try {
                    dateFormatter.parse(dateStr)?.let {
                        Instant.from(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(zone).parse(dateStr)).toEpochMilli()
                    } ?: 0L
                } catch (e: Exception) { 0L }
                if (merchantName.isNotBlank() && amount > 0 && dateMillis > 0) onEdit(merchantName, amount, dateMillis) else isError = true
            }) { Text("改好了", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("算了") } }
    )
}
