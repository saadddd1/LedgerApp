package com.example.ledger.ui.tab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("这些年败掉的总数", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("¥${String.format("%.2f", stats.totalSpent)}", style = MaterialTheme.typography.headlineMedium)
                }
                if (stats.totalRecovered > 0) {
                    Text("回血 ¥${String.format("%.2f", stats.totalRecovered)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(20.dp))

            Column {
                Text("真正烧掉的钱", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text("¥${String.format("%.2f", stats.netSpend)}", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(20.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("每天一睁眼就亏掉", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("¥${String.format("%.2f", stats.totalDailyCost)}", style = MaterialTheme.typography.titleLarge)
                }
                if (stats.worstItemName != null && stats.worstDailyCost > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("最烧钱的家当", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(stats.worstItemName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("每天 ¥${String.format("%.2f", stats.worstDailyCost)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                    }
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("自动抓取账单", style = MaterialTheme.typography.bodyLarge)
                Text("${stats.billCount} 笔", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(16.dp))
            ExpandableOverviewRow(stats.recentMonths, MaterialTheme.colorScheme.error, bold = true)
            Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(16.dp))
            ExpandableOverviewRow(stats.recentYears, MaterialTheme.colorScheme.onSurface, bold = false)
        }
    }
}

@Composable
private fun StatusCard(stats: Statistics) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Text("家当现状", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("还在吃灰的", style = MaterialTheme.typography.bodyLarge)
                Text("${stats.activeItems} 件", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("成功脱手的", style = MaterialTheme.typography.bodyLarge)
                Text("${stats.soldItems} 件", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (stats.activeItems > 0) {
                Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("本月预计折旧", style = MaterialTheme.typography.bodyLarge)
                    Text("¥${String.format("%.2f", stats.monthDepreciation)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
private fun ExpandableOverviewRow(
    historyData: List<Pair<String, Double>>,
    valueColor: androidx.compose.ui.graphics.Color,
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
            Text("¥${String.format("%.2f", current.second)}",
                fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor)
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
                    Text("还没有更早的记录", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        textAlign = TextAlign.Center)
                } else {
                    past.forEachIndexed { i, (label, value) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("¥${String.format("%.2f", value)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        if (i < past.size - 1) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}
