package com.example.ledger.ui.theme

import androidx.compose.ui.graphics.Color

// Primary: clean blue (iOS-style)
val BluePrimary = Color(0xFF007AFF)
val BlueOnPrimary = Color(0xFFFFFFFF)
val BluePrimaryContainer = Color(0xFFE3F2FF)
val BlueOnPrimaryContainer = Color(0xFF001A45)

// Secondary: warm purple accent
val PurpleSecondary = Color(0xFF6B5FF7)
val PurpleOnSecondary = Color(0xFFFFFFFF)
val PurpleSecondaryContainer = Color(0xFFEDEBFF)
val PurpleOnSecondaryContainer = Color(0xFF1A1163)

// Surfaces — Apple-style clean neutrals
val WhiteBackground = Color(0xFFF2F2F7)   // Apple systemBackground
val WhiteSurface = Color(0xFFFFFFFF)       // pure white cards
val WhiteSurfaceVariant = Color(0xFFF0F0F5)

// Dark surfaces
val DarkInverseSurface = Color(0xFF1C1C1E)
val DarkInverseOnSurface = Color(0xFFF5F5F7)

// Error (iOS red)
val ErrorRed = Color(0xFFFF3B30)
val ErrorContainer = Color(0xFFFFF0EF)

// Outline — subtle gray
val OutlineDim = Color(0xFFD1D1D6)

// Frosted glass — iOS 风格：更高不透明度 + 微暖白
val FrostedBar = Color(0xFFFBFBFD).copy(alpha = 0.82f)
val FrostedCard = Color(0xFFFCFCFE).copy(alpha = 0.76f)

// Dark frosted
val DarkFrostedBar = Color(0xFF1C1C1E).copy(alpha = 0.85f)
val DarkFrostedCard = Color(0xFF1C1C1E).copy(alpha = 0.78f)
