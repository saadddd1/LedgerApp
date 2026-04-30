package com.example.ledger.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ledger.data.Item
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@Composable
fun ItemCard(
    item: Item,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onSell: () -> Unit
) {
    val now = Instant.now()
    val zone = ZoneId.systemDefault()
    val purchaseInstant = Instant.ofEpochMilli(item.purchaseDateMillis)
    val endInstant = if (item.isSold) item.soldDateMillis?.let { Instant.ofEpochMilli(it) } ?: now else now
    val daysPassed = max(ChronoUnit.DAYS.between(purchaseInstant, endInstant), 1)
    val netCost = if (item.isSold) max(item.price - item.residualValue, 0.0) else item.price
    val dailyCost = netCost / daysPassed

    val dateStr = purchaseInstant.atZone(zone).toLocalDate().format(dateFormatter)
    val soldDateStr = item.soldDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(zone).toLocalDate().format(dateFormatter)
    }

    val alphaFactor by animateFloatAsState(
        targetValue = if (item.isSold) 0.5f else 1f,
        animationSpec = tween(400),
        label = "alpha"
    )

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
                        text = item.name,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alphaFactor)
                    )
                    if (item.isSold) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alphaFactor),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "已脱手",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alphaFactor)
                            )
                        }
                    }
                }
                Box(contentAlignment = Alignment.TopEnd) {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "更多", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("改一下") },
                            onClick = { expanded = false; onEdit() },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text("不要了", color = MaterialTheme.colorScheme.error) },
                            onClick = { expanded = false; onDelete() },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("入手价: ¥${String.format("%.2f", item.price)}", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = alphaFactor))
                    Text("入手日: $dateStr", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alphaFactor))
                }
                if (item.isSold) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("回血了: ¥${String.format("%.2f", item.residualValue)}", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alphaFactor))
                        if (soldDateStr != null)
                            Text("脱手日: $soldDateStr", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alphaFactor))
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("陪伴了你 $daysPassed 天", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alphaFactor))
                    if (!item.isSold) {
                        TextButton(onClick = onSell, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(30.dp)) {
                            Text("挂了咸鱼没？", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("每天烧你", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alphaFactor))
                    Text(
                        "¥${String.format("%.2f", dailyCost)}",
                        fontSize = 28.sp,
                        color = if (item.isSold) MaterialTheme.colorScheme.onSurface.copy(alpha = alphaFactor) else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
