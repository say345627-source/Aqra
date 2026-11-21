package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = Color.White,
    primaryContainer = EmeraldLightContainer,
    onPrimaryContainer = EmeraldDeep,
    secondary = GoldWarm,
    onSecondary = Color.White,
    secondaryContainer = GoldMutedContainer,
    onSecondaryContainer = EmeraldDeep,
    tertiary = EmeraldMedium,
    onTertiary = Color.White,
    background = ParchmentBackground,
    onBackground = InkDark,
    surface = ParchmentSurface,
    onSurface = InkDark,
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = InkMuted,
    outline = GoldBorderLight,
    outlineVariant = GoldBorderLight.copy(alpha = 0.55f)
)

private val DarkColorScheme = darkColorScheme(
    primary = NightEmeraldPrimary,
    onPrimary = NightBackground,
    primaryContainer = NightSurfaceVariant,
    onPrimaryContainer = NightTextPrimary,
    secondary = NightGold,
    onSecondary = NightBackground,
    secondaryContainer = Color(0xFF2B271B),
    onSecondaryContainer = GoldSoft,
    tertiary = EmeraldMedium,
    onTertiary = NightTextPrimary,
    background = NightBackground,
    onBackground = NightTextPrimary,
    surface = NightSurface,
    onSurface = NightTextPrimary,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightTextMuted,
    outline = NightGoldBorder,
    outlineVariant = NightGoldBorder.copy(alpha = 0.6f)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun QuranAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
