package com.subnetik.unlock.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.subnetik.unlock.presentation.theme.GradientBlue
import com.subnetik.unlock.presentation.theme.GradientIndigo

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = GradientBlue,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors)),
        content = content
    )
}

@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    colors: List<Color> = GradientIndigo,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(Brush.verticalGradient(colors)),
        content = content
    )
}
