package com.example.ledger.domain.model

data class Statistics(
    val totalSpent: Double,
    val totalRecovered: Double,
    val netSpend: Double,
    val totalDailyCost: Double,
    val worstItemName: String?,
    val worstDailyCost: Double,
    val monthDepreciation: Double,
    val activeItems: Int,
    val soldItems: Int,
    val billCount: Int,
    val thisMonthSpending: Double,
    val thisYearSpending: Double,
    val recentMonths: List<Pair<String, Double>>,
    val recentYears: List<Pair<String, Double>>
)
