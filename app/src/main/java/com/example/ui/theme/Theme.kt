package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BoldPrimary,
    onPrimary = BoldPrimaryContainer,
    primaryContainer = BoldPrimaryContainer,
    onPrimaryContainer = TextPrimary,
    secondary = BoldSecondary,
    onSecondary = BoldSecondaryContainer,
    secondaryContainer = BoldSecondaryContainer,
    onSecondaryContainer = TextPrimary,
    tertiary = BoldTertiary,
    onTertiary = BoldTertiaryContainer,
    tertiaryContainer = BoldTertiaryContainer,
    onTertiaryContainer = TextPrimary,
    background = BoldDarkCanvas,
    onBackground = TextPrimary,
    surface = StudioDarkBg,
    onSurface = TextPrimary,
    surfaceVariant = StudioCardBg,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    outlineVariant = StudioBorderGlow
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}


