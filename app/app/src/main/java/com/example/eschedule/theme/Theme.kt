package com.example.eschedule.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = UepBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = AppBackground,
    onPrimaryContainer = UepBlue,
    secondary = AccentBlue,
    onSecondary = SurfaceWhite,
    tertiary = UepYellow,
    onTertiary = UepBlue,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
)

@Composable
fun EScheduleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content,
    )
}
