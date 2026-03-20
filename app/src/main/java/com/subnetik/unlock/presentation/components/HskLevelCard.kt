package com.subnetik.unlock.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subnetik.unlock.presentation.theme.Brand
import com.subnetik.unlock.presentation.theme.hskLevelColor
import com.subnetik.unlock.presentation.theme.hskLevelGradient

@Composable
fun HskLevelCard(
    level: Int,
    title: String,
    subtitle: String? = null,
    progress: Float = 0f,
    badge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = hskLevelGradient(level)
    val character = Brand.hskCharacters[level] ?: "字"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(Brand.Shapes.large)
            .clickable(onClick = onClick),
        shape = Brand.Shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = Brand.Elevation.medium),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors))
                .padding(Brand.Spacing.lg),
        ) {
            // Decorative character
            Text(
                text = character,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.12f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.xs)
            ) {
                // Badge
                if (badge != null) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }

                if (progress > 0f) {
                    Spacer(modifier = Modifier.height(Brand.Spacing.xs))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(Brand.Shapes.full),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }
}
