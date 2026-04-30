package com.example.ledger.domain.usecase

import com.example.ledger.data.AutoBill
import com.example.ledger.data.ExpenseRecord
import com.example.ledger.data.Item
import com.example.ledger.data.RecurringExpense
import com.example.ledger.domain.model.Statistics
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max

object CalculateStatistics {

    private val monthFormat = DateTimeFormatter.ofPattern("yyyy-MM")
    private val yearFormat = DateTimeFormatter.ofPattern("yyyy")
    private val monthLabelFormat = DateTimeFormatter.ofPattern("yyyy年 MM月")

    fun calculate(
        items: List<Item>,
        allBills: List<AutoBill>,
        expenseRecords: List<ExpenseRecord> = emptyList(),
        activeTemplates: List<RecurringExpense> = emptyList()
    ): Statistics {
        val nowMillis = System.currentTimeMillis()
        val now = Instant.ofEpochMilli(nowMillis)
        val zone = ZoneId.systemDefault()

        val totalSpent = items.sumOf { it.price }
        val totalRecovered = items.filter { it.isSold }.sumOf { it.residualValue }
        val netSpend = max(totalSpent - totalRecovered, 0.0)

        val activeItems = items.count { !it.isSold }
        val soldItems = items.count { it.isSold }
        val billCount = allBills.size

        var totalDailyCost = 0.0
        var worstItemName: String? = null
        var worstDailyCost = 0.0

        for (item in items) {
            val endMillis = if (item.isSold) item.soldDateMillis ?: nowMillis else nowMillis
            val diffMillis = max(endMillis - item.purchaseDateMillis, 0)
            val daysPassed = max(ChronoUnit.DAYS.between(
                Instant.ofEpochMilli(item.purchaseDateMillis),
                Instant.ofEpochMilli(endMillis)
            ), 1)

            val netCost = if (item.isSold) max(item.price - item.residualValue, 0.0) else item.price
            val dailyCost = netCost / daysPassed
            totalDailyCost += dailyCost

            if (!item.isSold && dailyCost > worstDailyCost) {
                worstDailyCost = dailyCost
                worstItemName = item.name
            }
        }

        val daysInMonth = YearMonth.now().lengthOfMonth()
        val monthDepreciation = totalDailyCost * daysInMonth

        val monthlySums = mutableMapOf<String, Double>()
        val yearlySums = mutableMapOf<String, Double>()

        for (bill in allBills) {
            val instant = Instant.ofEpochMilli(bill.timestampMillis)
            val localDate = instant.atZone(zone).toLocalDate()
            monthlySums.merge(localDate.format(monthFormat), bill.amount, Double::plus)
            yearlySums.merge(localDate.format(yearFormat), bill.amount, Double::plus)
        }

        val thisMonthKey = YearMonth.now().format(monthFormat)
        val thisMonthSpending = monthlySums[thisMonthKey] ?: 0.0

        val thisYearKey = LocalDate.now().format(yearFormat)
        val thisYearSpending = yearlySums[thisYearKey] ?: 0.0

        val recentMonths = mutableListOf<Pair<String, Double>>()
        recentMonths.add(Pair("这个月已经烧了", thisMonthSpending))
        monthlySums.keys
            .filter { it != thisMonthKey }
            .sortedDescending()
            .forEach { key ->
                val label = runCatching {
                    YearMonth.parse(key).format(monthLabelFormat)
                }.getOrDefault(key)
                recentMonths.add(Pair(label, monthlySums[key] ?: 0.0))
            }

        val recentYears = mutableListOf<Pair<String, Double>>()
        recentYears.add(Pair("今年累计败掉", thisYearSpending))
        yearlySums.keys
            .filter { it != thisYearKey }
            .sortedDescending()
            .forEach { key ->
                recentYears.add(Pair("${key}年", yearlySums[key] ?: 0.0))
            }

        // Living expenses
        val totalLivingExpenses = expenseRecords.sumOf { it.amount }

        val livingMonthlyMap = mutableMapOf<String, Double>()
        var thisMonthLivingExpenses = 0.0
        var thisYearLivingExpenses = 0.0

        for (record in expenseRecords) {
            val instant = Instant.ofEpochMilli(record.dateMillis)
            val localDate = instant.atZone(zone).toLocalDate()
            val monthKey = localDate.format(monthFormat)
            livingMonthlyMap.merge(monthKey, record.amount, Double::plus)
            if (monthKey == thisMonthKey) thisMonthLivingExpenses += record.amount
            if (localDate.format(yearFormat) == thisYearKey) thisYearLivingExpenses += record.amount
        }

        val monthlyLivingList = mutableListOf<Pair<String, Double>>()
        monthlyLivingList.add(Pair("这个月生活费", thisMonthLivingExpenses))
        livingMonthlyMap.keys
            .filter { it != thisMonthKey }
            .sortedDescending()
            .forEach { key ->
                val label = runCatching {
                    YearMonth.parse(key).format(monthLabelFormat)
                }.getOrDefault(key)
                monthlyLivingList.add(Pair(label, livingMonthlyMap[key] ?: 0.0))
            }

        val activeRecurringCount = activeTemplates.size
        val livingVsAssetsRatio = if (totalLivingExpenses > totalSpent) "生活费为主"
            else if (totalSpent > 0) "家当为主" else "暂无数据"

        return Statistics(
            totalSpent = totalSpent,
            totalRecovered = totalRecovered,
            netSpend = netSpend,
            totalDailyCost = totalDailyCost,
            worstItemName = worstItemName,
            worstDailyCost = worstDailyCost,
            monthDepreciation = monthDepreciation,
            activeItems = activeItems,
            soldItems = soldItems,
            billCount = billCount,
            thisMonthSpending = thisMonthSpending,
            thisYearSpending = thisYearSpending,
            recentMonths = recentMonths,
            recentYears = recentYears,
            totalLivingExpenses = totalLivingExpenses,
            thisMonthLivingExpenses = thisMonthLivingExpenses,
            thisYearLivingExpenses = thisYearLivingExpenses,
            monthlyLivingExpenses = monthlyLivingList,
            activeRecurringCount = activeRecurringCount,
            livingVsAssetsRatio = livingVsAssetsRatio
        )
    }
}
