package com.subnetik.unlock.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlue.copy(alpha = 0.12f),
    onPrimaryContainer = BrandBlue,
    secondary = BrandTeal,
    onSecondary = Color.White,
    tertiary = BrandGold,
    onTertiary = Color.White,
    error = Color(0xFFDC2626),
    onError = Color.White,
    background = MistBlue,
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlue.copy(alpha = 0.2f),
    onPrimaryContainer = Color(0xFF93C5FD),
    secondary = BrandTeal,
    onSecondary = Color.White,
    tertiary = BrandGold,
    onTertiary = Color.Black,
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D),
    background = DarkNavy,
    onBackground = Color(0xFFF8FAFC),
    surface = DeepBlue,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
)

@Composable
fun UnlockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UnlockTypography,
        content = content
    )
}
