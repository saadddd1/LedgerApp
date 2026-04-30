package com.example.ledger.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ledger.data.AutoBill
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

@Composable
fun AutoBillCard(
    bill: AutoBill,
    onDismiss: () -> Unit,
    onConvert: () -> Unit,
    onEdit: () -> Unit
) {
    val dateStr = Instant.ofEpochMilli(bill.timestampMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(dateFormatter)

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
                Text(
                    text = bill.merchantName,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(contentAlignment = Alignment.TopEnd) {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "更多", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        if (!bill.paymentMethod.isNullOrBlank()) {
                            DropdownMenuItem(
                                text = { Text("怎么付的：${bill.paymentMethod}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                onClick = { expanded = false }
                            )
                        }
                        if (!bill.fullPayeeName.isNullOrBlank()) {
                            DropdownMenuItem(
                                text = { Text("钱给了谁：${bill.fullPayeeName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                onClick = { expanded = false }
                            )
                            HorizontalDivider()
                        } else if (!bill.paymentMethod.isNullOrBlank()) {
                            HorizontalDivider()
                        }
                        DropdownMenuItem(
                            text = { Text("改一下", color = MaterialTheme.colorScheme.primary) },
                            onClick = { expanded = false; onEdit() },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text("不是我的账", color = MaterialTheme.colorScheme.error) },
                            onClick = { expanded = false; onDismiss() },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = "忽略", tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("渠道: ${bill.appSource}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dateStr, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "¥${String.format("%.2f", bill.amount)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FilledTonalButton(
                    onClick = onConvert,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = "导入", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("计入家当", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
