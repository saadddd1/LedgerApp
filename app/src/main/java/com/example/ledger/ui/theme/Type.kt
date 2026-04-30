package com.example.ledger.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ledger.R

val HarmonyOS = FontFamily(
    Font(R.font.harmonyos_sans_regular, FontWeight.Normal),
    Font(R.font.harmonyos_sans_medium, FontWeight.Medium),
    Font(R.font.harmonyos_sans_bold, FontWeight.Bold),
)

val LedgerTypography = Typography(
    // Big numbers — monthly total, net worth
    displayLarge = TextStyle(
        fontFamily = HarmonyOS, fontWeight = FontWeight.Bold, fontSize = 40.sp,
        lineHeight = 48.sp, letterSpacing = (-0.5).sp
    ),
    // Section headline — "家当总账", "花钱流水"
    headlineLarge = TextStyle(
        fontFamily = HarmonyOS, fontWeight = FontWeight.Bold, fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    // Sub headline
    headlineMedium = TextStyle(
        fontFamily = HarmonyOS, fontWeight = FontWeight.SemiBold, fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    // Card title, dialog title
    titleLarge = TextStyle(
        fontFamily = HarmonyOS, fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    // Subtitle, secondary card title
    titleMedium = TextStyle(
        fontFamily = HarmonyOS, fontWeight = FontWeight.Medium, fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Primary body — item names, main content
    bodyLarge = TextStyle(
        fontFamily = HarmonyOS, fontWeight = FontWeight.Normal, fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Secondary body — descriptions, secondary info
    bodyMedium = TextStyle(
        fontFamily = HarmonyOS, fontWeight = FontWeight.Normal, fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Metadata — dates, small labels, supporting text
    bodySmall = TextStyle(
        fontFamily = HarmonyOS, fontWeight = FontWeight.Normal, fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Button text, tab label
    labelLarge = TextStyle(
        fontFamily = HarmonyOS, fontWeight = FontWeight.Medium, fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Small label
    labelMedium = TextStyle(
        fontFamily = HarmonyOS, fontWeight = FontWeight.Medium, fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Caption, fine print
    labelSmall = TextStyle(
        fontFamily = HarmonyOS, fontWeight = FontWeight.Normal, fontSize = 11.sp,
        lineHeight = 16.sp
    ),
)
