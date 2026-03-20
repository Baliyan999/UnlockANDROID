package com.subnetik.unlock.presentation.screens.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

@Composable
fun TeacherProgressScreen(
    viewModel: TeacherHomeViewModel,
    isDark: Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()
    val student = uiState.selectedStudent ?: return

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Прогресс",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Surface(
                    onClick = { viewModel.closeProgress() },
                    shape = Brand.Shapes.full,
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                ) {
                    Text(
                        "Закрыть",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText,
                    )
                }
            }

            Spacer(Modifier.height(Brand.Spacing.md))

            // Student info
            Row(
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BrandBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        student.firstName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                    )
                }
                Spacer(Modifier.width(Brand.Spacing.md))
                Column {
                    Text(
                        student.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )
                    Text(
                        "Академический прогресс",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText,
                    )
                }
            }

            Spacer(Modifier.height(Brand.Spacing.xl))

            if (uiState.isLoadingProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Brand.Spacing.xxxl),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                // HSK Tests section
                Column(modifier = Modifier.padding(horizontal = Brand.Spacing.lg)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = BrandBlue)
                        Text(
                            "Тесты HSK",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                        )
                    }

                    Spacer(Modifier.height(Brand.Spacing.md))

                    val progress = uiState.studentProgress
                    val testResults = progress?.tests ?: emptyList()

                    // HSK 1-6 levels
                    for (level in 1..6) {
                        val testResult = testResults.find { it.levelId == "$level" }
                        val isCompleted = testResult != null && testResult.bestScore > 0
                        val isLocked = level > 1 && testResults.none { it.levelId == "${level - 1}" && it.bestScore > 0 }

                        val statusText = when {
                            isCompleted -> "Пройден: ${testResult?.bestScore}/${testResult?.totalQuestions}"
                            isLocked -> "Пройдите HSK ${level - 1}"
                            else -> "Не начат"
                        }

                        HskLevelCard(
                            level = level,
                            statusText = statusText,
                            isLocked = isLocked,
                            isCompleted = isCompleted,
                            isDark = isDark,
                            cardColor = cardColor,
                            strokeColor = strokeColor,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                        )

                        Spacer(Modifier.height(Brand.Spacing.sm))
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.xl))

                // Vocabulary section
                Column(modifier = Modifier.padding(horizontal = Brand.Spacing.lg)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("🅰", fontSize = 18.sp)
                        Spacer(Modifier.width(Brand.Spacing.sm))
                        Text(
                            "Словарь",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                        )
                    }

                    Spacer(Modifier.height(Brand.Spacing.md))

                    val vocabItems = uiState.studentProgress?.vocabulary ?: emptyList()
                    if (vocabItems.isEmpty()) {
                        Text(
                            "Ученик ещё не изучал словарь",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    } else {
                        vocabItems.forEach { vocab ->
                            Text(
                                "HSK ${vocab.level}: ${vocab.knownCount} из ${vocab.totalWords} слов",
                                style = MaterialTheme.typography.bodyMedium,
                                color = primaryText,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.xxxl))
            }
        }
    }
}

@Composable
private fun HskLevelCard(
    level: Int,
    statusText: String,
    isLocked: Boolean,
    isCompleted: Boolean,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
) {
    val hskColor = hskLevelColor(level)
    val alpha = if (isLocked) 0.5f else 1f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = cardColor.copy(alpha = alpha),
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Row(
            modifier = Modifier.padding(Brand.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Level badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(hskLevelGradient(level).map { it.copy(alpha = alpha) }),
                        RoundedCornerShape(10.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "$level",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = alpha),
                )
            }

            Spacer(Modifier.width(Brand.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "HSK $level",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText.copy(alpha = alpha),
                )
                Text(
                    statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText.copy(alpha = alpha),
                )
            }

            if (isLocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = secondaryText.copy(alpha = 0.5f),
                )
            }
        }
    }
}
