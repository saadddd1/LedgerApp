package com.example.ledger.ui.tab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ledger.data.AutoBill
import com.example.ledger.data.Item
import com.example.ledger.domain.model.Statistics
import com.example.ledger.domain.usecase.CalculateStatistics

@Composable
fun OverviewTab(
    items: List<Item>,
    allBills: List<AutoBill>,
    modifier: Modifier = Modifier
) {
    val statistics = remember(items, allBills) {
        CalculateStatistics.calculate(items, allBills)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { AssetSummaryCard(statistics) }
        item { SpendingHistoryCard(statistics) }
        item { StatusCard(statistics) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun AssetSummaryCard(stats: Statistics) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Text("家当总账", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(20.dp))
            OverviewRow("这些年败掉的总数", "¥${String.format("%.2f", stats.totalSpent)}", 18.sp)
            if (stats.totalRecovered > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                OverviewRow("在咸鱼上回的血", "¥${String.format("%.2f", stats.totalRecovered)}", 16.sp, MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(16.dp))
            OverviewRow("真正烧掉的钱", "¥${String.format("%.2f", stats.netSpend)}", 26.sp, MaterialTheme.colorScheme.error, bold = true)
            Spacer(modifier = Modifier.height(16.dp))
            OverviewRow("每天一睁眼就亏掉", "¥${String.format("%.2f", stats.totalDailyCost)}", 16.sp)
            if (stats.worstItemName != null && stats.worstDailyCost > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("最烧钱的家当", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${stats.worstItemName} 每天 ¥${String.format("%.2f", stats.worstDailyCost)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpendingHistoryCard(stats: Statistics) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Text("花钱流水", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(20.dp))
            OverviewRow("自动抓取账单", "${stats.billCount} 笔", 18.sp)
            Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(16.dp))
            ExpandableOverviewRow(stats.recentMonths, MaterialTheme.colorScheme.error, 26.sp, bold = true)
            Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(16.dp))
            ExpandableOverviewRow(stats.recentYears, MaterialTheme.colorScheme.onSurface, 18.sp)
        }
    }
}

@Composable
private fun StatusCard(stats: Statistics) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Text("家当现状", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(20.dp))
            OverviewRow("还在吃灰的", "${stats.activeItems} 件", 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            OverviewRow("成功脱手的", "${stats.soldItems} 件", 16.sp, MaterialTheme.colorScheme.primary)
            if (stats.activeItems > 0) {
                Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(16.dp))
                OverviewRow("本月预计折旧", "¥${String.format("%.2f", stats.monthDepreciation)}", 18.sp, MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun ExpandableOverviewRow(
    historyData: List<Pair<String, Double>>,
    valueColor: androidx.compose.ui.graphics.Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    bold: Boolean = false
) {
    if (historyData.isEmpty()) return
    val current = historyData.first()
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { expanded = !expanded }
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(current.first, style = MaterialTheme.typography.bodyLarge)
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp).size(16.dp)
                )
            }
            Text("¥${String.format("%.2f", current.second)}", fontWeight = if (bold) FontWeight.Black else FontWeight.SemiBold, fontSize = fontSize, color = valueColor)
        }

        AnimatedVisibility(visible = expanded) {
            val past = historyData.drop(1)
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .heightIn(max = 240.dp).verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (past.isEmpty()) {
                    Text("还没有更早的记录", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), textAlign = TextAlign.Center)
                } else {
                    past.forEachIndexed { i, (label, value) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            Text("¥${String.format("%.2f", value)}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        if (i < past.size - 1) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewRow(
    label: String,
    value: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    bold: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, fontWeight = if (bold) FontWeight.Black else FontWeight.SemiBold, fontSize = fontSize, color = valueColor)
    }
}
