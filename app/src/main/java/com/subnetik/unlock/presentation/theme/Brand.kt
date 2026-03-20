package com.subnetik.unlock.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Brand {
    val cornerRadius = 16.dp
    val cornerRadiusSmall = 12.dp
    val cornerRadiusLarge = 22.dp

    val hskCharacters = mapOf(
        1 to "你",
        2 to "说",
        3 to "读",
        4 to "思",
        5 to "论",
        6 to "悟",
    )

    object Gradients {
        val primary get() = Brush.linearGradient(GradientBlue)
        val indigo get() = Brush.linearGradient(GradientIndigo)
        val coral get() = Brush.linearGradient(GradientCoral)
        val green get() = Brush.linearGradient(GradientGreen)
        val gold get() = Brush.linearGradient(GradientGold)
        val dark get() = Brush.linearGradient(GradientDark)
        fun hskLevel(level: Int) = Brush.linearGradient(hskLevelGradient(level))
    }

    object Spacing {
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
        val xxxl = 48.dp
    }

    object Shapes {
        val small = RoundedCornerShape(8.dp)
        val medium = RoundedCornerShape(12.dp)
        val large = RoundedCornerShape(16.dp)
        val extraLarge = RoundedCornerShape(22.dp)
        val full = RoundedCornerShape(50)
    }

    object Elevation {
        val none: Dp = 0.dp
        val small: Dp = 2.dp
        val medium: Dp = 4.dp
        val large: Dp = 8.dp
        val extraLarge: Dp = 16.dp
    }
}
