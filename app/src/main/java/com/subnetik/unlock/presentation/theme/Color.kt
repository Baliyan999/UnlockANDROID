package com.subnetik.unlock.presentation.theme

import androidx.compose.ui.graphics.Color

// Brand colors
val BrandBlue = Color(0xFF3A82F5)
val BrandTeal = Color(0xFF249EA3)
val BrandCoral = Color(0xFFF57840)
val BrandIndigo = Color(0xFF5261C7)
val BrandGold = Color(0xFFF5B038)
val BrandGreen = Color(0xFF33B34D)

// Light backgrounds
val MistBlue = Color(0xFFEEF5FD)
val WarmIvory = Color(0xFFFCFBF5)

// Dark backgrounds
val DarkNavy = Color(0xFF0F1429)
val DeepBlue = Color(0xFF141E47)

// HSK Level colors
val HskLevel1 = BrandBlue
val HskLevel2 = BrandTeal
val HskLevel3 = BrandIndigo
val HskLevel4 = BrandGold
val HskLevel5 = BrandCoral
val HskLevel6 = Color(0xFF8B2252)

// Gradient color lists (for Brush.linearGradient)
val GradientBlue = listOf(Color(0xFF3A82F5), Color(0xFF249EA3))
val GradientIndigo = listOf(Color(0xFF5261C7), Color(0xFF3A82F5))
val GradientCoral = listOf(Color(0xFFF57840), Color(0xFFF5B038))
val GradientGreen = listOf(Color(0xFF33B34D), Color(0xFF249EA3))
val GradientGold = listOf(Color(0xFFF5B038), Color(0xFFF57840))
val GradientDark = listOf(Color(0xFF141E47), Color(0xFF0F1429))

// HSK level gradient lists
val HskGradient1 = listOf(Color(0xFF3A82F5), Color(0xFF5B9DF7))
val HskGradient2 = listOf(Color(0xFF249EA3), Color(0xFF3BC4CA))
val HskGradient3 = listOf(Color(0xFF5261C7), Color(0xFF7B86D9))
val HskGradient4 = listOf(Color(0xFFF5B038), Color(0xFFF7C766))
val HskGradient5 = listOf(Color(0xFFF57840), Color(0xFFF79A6E))
val HskGradient6 = listOf(Color(0xFF8B2252), Color(0xFFAD3A70))

fun hskLevelColor(level: Int): Color = when (level) {
    1 -> HskLevel1
    2 -> HskLevel2
    3 -> HskLevel3
    4 -> HskLevel4
    5 -> HskLevel5
    6 -> HskLevel6
    else -> BrandBlue
}

fun hskLevelGradient(level: Int): List<Color> = when (level) {
    1 -> HskGradient1
    2 -> HskGradient2
    3 -> HskGradient3
    4 -> HskGradient4
    5 -> HskGradient5
    6 -> HskGradient6
    else -> GradientBlue
}
