package com.example.ledger.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,
    secondary = PurpleSecondary,
    onSecondary = PurpleOnSecondary,
    secondaryContainer = PurpleSecondaryContainer,
    onSecondaryContainer = PurpleOnSecondaryContainer,
    background = WhiteBackground,
    onBackground = Color(0xFF1C1C1C),
    surface = WhiteSurface,
    onSurface = Color(0xFF1C1C1C),
    surfaceVariant = WhiteSurfaceVariant,
    onSurfaceVariant = Color(0xFF484848),
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    outline = OutlineDim,
    outlineVariant = OutlineDim
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6DB3FF),
    onPrimary = Color(0xFF003066),
    primaryContainer = Color(0xFF00468F),
    onPrimaryContainer = Color(0xFFDBE9FF),
    secondary = Color(0xFFC9C3FF),
    onSecondary = Color(0xFF2C2279),
    secondaryContainer = Color(0xFF433B90),
    onSecondaryContainer = Color(0xFFE8E5FF),
    background = Color(0xFF111111),
    onBackground = Color(0xFFE5E5E5),
    surface = Color(0xFF1C1C1C),
    onSurface = Color(0xFFE5E5E5),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFC6C6C6),
    inverseSurface = Color(0xFFE5E5E5),
    inverseOnSurface = Color(0xFF2D2D2D),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8E8E8E),
    outlineVariant = Color(0xFF484848)
)

fun Modifier.frostedBar(): Modifier = composed {
    this.background(FrostedBar)
}

fun Modifier.frostedCard(): Modifier = composed {
    this
        .clip(RoundedCornerShape(16.dp))
        .background(FrostedCard)
}

@Composable
fun LedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LedgerTypography,
        content = content
    )
}
