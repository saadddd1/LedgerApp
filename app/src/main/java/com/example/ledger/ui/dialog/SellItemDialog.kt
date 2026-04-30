package com.example.ledger.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ledger.data.Item
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@Composable
fun SellItemDialog(
    item: Item,
    onDismiss: () -> Unit,
    onSell: (soldPrice: Double, soldDateMillis: Long) -> Unit
) {
    var priceStr by remember { mutableStateOf(if (item.residualValue > 0) item.residualValue.toString() else "") }
    var dateStr by remember { mutableStateOf(LocalDate.now().format(dateFormatter)) }
    var isError by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(14.dp),
        title = { Text("终于卖了？", fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column {
                Text("来算算【${item.name}】到底亏了你多少钱。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp), textAlign = TextAlign.Center)
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("卖了多少钱") }, singleLine = true, isError = isError && priceStr.isBlank(), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = dateStr, onValueChange = { dateStr = it }, label = { Text("哪天出手的") }, singleLine = true, isError = isError && runCatching { LocalDate.parse(dateStr, dateFormatter) }.isFailure, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val price = priceStr.toDoubleOrNull() ?: 0.0
                val dateMillis = try {
                    LocalDate.parse(dateStr, dateFormatter).atStartOfDay(zone).toInstant().toEpochMilli()
                } catch (e: Exception) { 0L }
                if (dateMillis > 0) onSell(price, dateMillis) else isError = true
            }) { Text("面对现实", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("算了") } }
    )
}
