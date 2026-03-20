package com.subnetik.unlock.presentation.screens.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

private val hskDescriptions = mapOf(
    1 to "Начальный уровень. Базовые слова и фразы.",
    2 to "Базовый уровень. Простые предложения.",
    3 to "Средний уровень. Повседневное общение.",
    4 to "Продвинутый. Свободная беседа.",
    5 to "Высший. Академическая лексика.",
    6 to "Мастер. Носитель языка.",
)

@Composable
fun TestListScreen(
    onNavigateToTest: (Int) -> Unit,
    viewModel: TestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
        ) {
            // Title
            item(span = { GridItemSpan(2) }) {
                Text(
                    "Тест",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
            }

            // Shield icon + header
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Shield icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(BrandBlue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = BrandBlue,
                        )
                    }

                    Spacer(Modifier.height(Brand.Spacing.lg))

                    Text(
                        "Тесты HSK",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )

                    Spacer(Modifier.height(Brand.Spacing.sm))

                    Text(
                        "6 уровней · 100 вопросов в каждом",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText,
                    )
                    Text(
                        "10 случайных на каждый тест",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText,
                    )
                }
            }

            // Info badges
            item(span = { GridItemSpan(2) }) {
                Column(verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                    ) {
                        InfoBadge(Icons.Default.Shuffle, "Случайный порядок", BrandBlue, isDark, Modifier.weight(1f))
                        InfoBadge(Icons.Default.FormatListNumbered, "10 вопросов", BrandIndigo, isDark, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                    ) {
                        InfoBadge(Icons.Default.Timer, "10 минут", BrandGold, isDark, Modifier.weight(1f))
                        InfoBadge(Icons.Default.CheckCircle, "Проход: 80%", BrandGreen, isDark, Modifier.weight(1f))
                    }
                }
            }

            item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(4.dp)) }

            // HSK level cards
            items((1..6).toList()) { level ->
                val progress = uiState.levelProgress[level]
                val passed = progress?.passed == true
                val isLocked = level > 1 && uiState.levelProgress[level - 1]?.passed != true

                TestLevelCard(
                    level = level,
                    description = hskDescriptions[level] ?: "",
                    bestPercent = progress?.bestPercent,
                    attempts = progress?.attempts ?: 0,
                    passed = passed,
                    isLocked = isLocked,
                    lockReason = if (isLocked) "Пройдите HSK ${level - 1}" else null,
                    isDark = isDark,
                    onClick = { if (!isLocked) onNavigateToTest(level) },
                )
            }

            item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(Brand.Spacing.xl)) }
        }
    }
}

@Composable
private fun InfoBadge(
    icon: ImageVector,
    text: String,
    tint: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, tint.copy(alpha = if (isDark) 0.2f else 0.15f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = tint)
            Spacer(Modifier.width(6.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = tint,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TestLevelCard(
    level: Int,
    description: String,
    bestPercent: Int?,
    attempts: Int,
    passed: Boolean,
    isLocked: Boolean,
    lockReason: String?,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val gradientColors = hskLevelGradient(level)
    val character = Brand.hskCharacters[level] ?: "字"

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(0.72f),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors), RoundedCornerShape(18.dp)),
        ) {
            // Watermark character
            Text(
                text = character,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp),
            )

            if (isLocked) {
                // Locked overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Brand.Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Level circle (dimmed)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .alpha(0.5f)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$level",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White.copy(alpha = 0.5f),
                    )

                    Spacer(Modifier.height(Brand.Spacing.md))

                    Text(
                        lockReason ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "HSK $level",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                    )

                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
            } else {
                // Unlocked content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Brand.Spacing.md, vertical = Brand.Spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(4.dp))

                    // Level circle
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$level",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }

                    Spacer(Modifier.height(Brand.Spacing.md))

                    Text(
                        "HSK $level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )

                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.weight(1f))

                    // Progress
                    if (bestPercent != null && attempts > 0) {
                        LinearProgressIndicator(
                            progress = { bestPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(Brand.Shapes.full),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.25f),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${bestPercent}% · $attempts попытк${if (attempts == 1) "а" else if (attempts in 2..4) "и" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    // Button
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = Brand.Shapes.full,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            if (attempts > 0) "Пройти снова →" else "Начать →",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
