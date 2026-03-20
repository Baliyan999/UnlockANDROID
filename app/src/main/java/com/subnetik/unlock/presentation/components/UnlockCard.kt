package com.subnetik.unlock.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.subnetik.unlock.presentation.theme.Brand

@Composable
fun UnlockCard(
    modifier: Modifier = Modifier,
    elevation: Dp = Brand.Elevation.medium,
    accentColors: List<Color>? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
    val cardElevation = CardDefaults.cardElevation(defaultElevation = elevation)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = Brand.Shapes.large,
            colors = cardColors,
            elevation = cardElevation,
        ) {
            if (accentColors != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Brush.horizontalGradient(accentColors))
                )
            }
            Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = Brand.Shapes.large,
            colors = cardColors,
            elevation = cardElevation,
        ) {
            if (accentColors != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Brush.horizontalGradient(accentColors))
                )
            }
            Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                content()
            }
        }
    }
}
