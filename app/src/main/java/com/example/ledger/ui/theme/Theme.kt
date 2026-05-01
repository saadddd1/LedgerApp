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
    tertiary = TealTertiary,
    onTertiary = TealOnTertiary,
    tertiaryContainer = TealTertiaryContainer,
    onTertiaryContainer = TealOnTertiaryContainer,
    background = WhiteBackground,
    onBackground = Color(0xFF1A1C1E),
    surface = WhiteSurface,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = WhiteSurfaceVariant,
    onSurfaceVariant = Color(0xFF44464A),
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    outline = OutlineDim,
    outlineVariant = OutlineVariant
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF002C6E),
    primaryContainer = Color(0xFF0040A5),
    onPrimaryContainer = Color(0xFFDBEBFF),
    secondary = Color(0xFFD9D2FF),
    onSecondary = Color(0xFF2A1F80),
    secondaryContainer = Color(0xFF423B8F),
    onSecondaryContainer = Color(0xFFECEAFF),
    tertiary = Color(0xFF82E8F5),
    onTertiary = Color(0xFF003741),
    tertiaryContainer = Color(0xFF004F5D),
    onTertiaryContainer = Color(0xFFD3F8FD),
    background = Color(0xFF0D0D14),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF16161F),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF44464F),
    onSurfaceVariant = Color(0xFFC3C7CF),
    inverseSurface = Color(0xFFE1E2E8),
    inverseOnSurface = Color(0xFF2D3037),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8E9098),
    outlineVariant = Color(0xFF44464F)
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
