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
fun EditItemDialog(
    item: Item,
    onDismiss: () -> Unit,
    onEdit: (name: String, price: Double, dateMillis: Long) -> Unit
) {
    val zone = ZoneId.systemDefault()
    var name by remember(item) { mutableStateOf(item.name) }
    var priceStr by remember(item) { mutableStateOf(if (item.price > 0) item.price.toString() else "") }
    var dateStr by remember(item) {
        mutableStateOf(
            Instant.ofEpochMilli(item.purchaseDateMillis).atZone(zone).toLocalDate().format(dateFormatter)
        )
    }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(14.dp),
        title = { Text("改一下信息", fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("叫什么") }, singleLine = true, isError = isError && name.isBlank(), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("花了多少钱") }, singleLine = true, isError = isError && (priceStr.toDoubleOrNull() ?: 0.0) <= 0, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = dateStr, onValueChange = { dateStr = it }, label = { Text("什么时候败的") }, singleLine = true, isError = isError && runCatching { LocalDate.parse(dateStr, dateFormatter) }.isFailure, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val price = priceStr.toDoubleOrNull() ?: 0.0
                val dateMillis = try {
                    LocalDate.parse(dateStr, dateFormatter).atStartOfDay(zone).toInstant().toEpochMilli()
                } catch (e: Exception) { 0L }
                if (name.isNotBlank() && price > 0 && dateMillis > 0) onEdit(name, price, dateMillis) else isError = true
            }) { Text("改好了", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("算了") } }
    )
}
