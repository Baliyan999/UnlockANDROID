package com.subnetik.unlock.presentation.screens.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subnetik.unlock.data.remote.dto.progress.TestAttemptDetail
import com.subnetik.unlock.data.remote.dto.progress.TestProgressSyncItem
import com.subnetik.unlock.data.remote.dto.progress.VocabProgressSyncItem
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

@Composable
fun TeacherProgressScreen(
    viewModel: TeacherHomeViewModel,
    isDark: Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()
    val student = uiState.selectedStudent ?: return

    // Force read isDark from uiState to ensure it's always fresh
    val effectiveIsDark = uiState.isDarkTheme ?: isDark

    val primaryText = if (effectiveIsDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (effectiveIsDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (effectiveIsDark) Color(0xFF1A2340) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (effectiveIsDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)

    var expandedTestLevel by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = effectiveIsDark)

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
                    color = if (effectiveIsDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
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
                // ── HSK Tests section ──────────────────────────────
                Column(modifier = Modifier.padding(horizontal = Brand.Spacing.lg)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = BrandBlue,
                        )
                        Spacer(Modifier.width(Brand.Spacing.sm))
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

                    for (level in 1..6) {
                        val testResult = testResults.find {
                            it.levelId == "$level" || it.levelId == "hsk$level"
                        }
                        val prevTest = if (level > 1) testResults.find {
                            it.levelId == "${level - 1}" || it.levelId == "hsk${level - 1}"
                        } else null
                        val isLocked = level > 1 && (prevTest == null || prevTest.bestScore < 8)

                        TestLevelCard(
                            level = level,
                            testResult = testResult,
                            isLocked = isLocked,
                            isDark = effectiveIsDark,
                            cardColor = cardColor,
                            strokeColor = strokeColor,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            isExpanded = expandedTestLevel == (testResult?.levelId ?: ""),
                            onToggleExpand = {
                                val id = testResult?.levelId ?: ""
                                expandedTestLevel = if (expandedTestLevel == id) null else id
                            },
                        )

                        Spacer(Modifier.height(Brand.Spacing.sm))
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.xl))

                // ── Vocabulary section ─────────────────────────────
                Column(modifier = Modifier.padding(horizontal = Brand.Spacing.lg)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = BrandTeal,
                        )
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
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        vocabItems.forEach { vocab ->
                            VocabLevelCard(
                                vocab = vocab,
                                isDark = effectiveIsDark,
                                cardColor = cardColor,
                                strokeColor = strokeColor,
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                            )
                            Spacer(Modifier.height(Brand.Spacing.sm))
                        }
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.xxxl))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════
// Test Level Card — rewritten from scratch, no Material Surface
// ════════════════════════════════════════════════════════════

@Composable
private fun TestLevelCard(
    level: Int,
    testResult: TestProgressSyncItem?,
    isLocked: Boolean,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
) {
    val isCompleted = testResult != null && testResult.bestScore > 0
    // Explicit opaque colors — no alpha tricks
    val bg = if (isDark) Color(0xFF162033) else Color(0xFFF0F2F5)
    val lockedBg = if (isDark) Color(0xFF1B2640) else Color(0xFFE8EBF0)
    val border = if (isDark) Color(0xFF2A3B5A) else Color(0xFFD1D5DB)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (isLocked) 0.55f else 1f }
            .background(if (isLocked) lockedBg else bg, RoundedCornerShape(14.dp))
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Level number badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        BrandBlue.copy(alpha = if (isLocked) 0.08f else 0.18f),
                        RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "$level",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLocked) secondaryText else BrandBlue,
                )
            }

            Spacer(Modifier.width(Brand.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "HSK $level",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLocked) secondaryText else primaryText,
                )
                Text(
                    when {
                        isCompleted -> {
                            val att = russianPlural(testResult!!.attempts, "попытка", "попытки", "попыток")
                            "${testResult.bestScore}/${testResult.totalQuestions} (${testResult.bestPercent}%) \u2022 ${testResult.attempts} $att"
                        }
                        isLocked -> "Пройдите HSK ${level - 1}"
                        else -> "Не начат"
                    },
                    fontSize = 12.sp,
                    color = secondaryText,
                )
            }

            // Lock icon or pass/fail badge
            if (isLocked) {
                Icon(Icons.Default.Lock, null, Modifier.size(14.dp), tint = secondaryText.copy(alpha = 0.5f))
            } else if (isCompleted) {
                val passed = testResult!!.bestScore >= 8
                Box(
                    modifier = Modifier
                        .background(if (passed) BrandTeal else BrandCoral, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        if (passed) "Пройден" else "Не сдан",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }

        // Expandable answers
        if (testResult != null && !testResult.bestAttemptDetails.isNullOrEmpty() && !isLocked) {
            Spacer(Modifier.height(8.dp))

            // Toggle
            Row(
                modifier = Modifier
                    .clickable(onClick = onToggleExpand)
                    .background(BrandBlue.copy(alpha = 0.08f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null, Modifier.size(14.dp), tint = BrandBlue,
                )
                Text(
                    if (isExpanded) "Скрыть ответы" else "Показать ответы",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    val details = testResult.bestAttemptDetails!!
                    val correctCount = details.count { it.isCorrect == true }
                    val wrongCount = details.size - correctCount

                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(8.dp).background(BrandTeal, CircleShape))
                            Text("Верно: $correctCount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandTeal)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(8.dp).background(BrandCoral, CircleShape))
                            Text("Ошибки: $wrongCount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandCoral)
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    val divColor = if (isDark) Color(0xFF2A3B5A) else Color(0xFFD1D5DB)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, divColor, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp)),
                    ) {
                        details.forEachIndexed { idx, detail ->
                            QuestionDetailRow(idx + 1, detail, isDark, primaryText)
                            if (idx < details.lastIndex) {
                                HorizontalDivider(color = divColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════
// Question Detail Row (matching iOS questionDetailRow)
// ════════════════════════════════════════════════════════════

@Composable
private fun QuestionDetailRow(
    index: Int,
    detail: TestAttemptDetail,
    isDark: Boolean,
    primaryText: Color,
) {
    val isCorrect = detail.isCorrect == true
    val accentColor = if (isCorrect) BrandTeal else BrandCoral
    val bgColor = if (isDark) {
        if (isCorrect) Color(0xFF0D2620) else Color(0xFF2A1215)
    } else {
        accentColor.copy(alpha = 0.04f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Number badge
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(accentColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$index",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
        }

        // Question and answer details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                detail.prompt ?: "",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                fontSize = 12.sp,
            )

            Spacer(Modifier.height(4.dp))

            if (isCorrect) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = BrandTeal,
                    )
                    Text(
                        detail.correctAnswer ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandTeal,
                        fontSize = 11.sp,
                    )
                }
            } else {
                // Wrong answer with strikethrough
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = BrandCoral,
                    )
                    Text(
                        detail.selectedAnswer ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.LineThrough,
                        ),
                        color = BrandCoral,
                        fontSize = 11.sp,
                    )
                }
                // Correct answer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = BrandTeal,
                    )
                    Text(
                        detail.correctAnswer ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandTeal,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        // Status icon
        Icon(
            if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = accentColor,
        )
    }
}

// ════════════════════════════════════════════════════════════
// Vocab Level Card (matching iOS vocabLevelCard)
// ════════════════════════════════════════════════════════════

@Composable
private fun VocabLevelCard(
    vocab: VocabProgressSyncItem,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
) {
    val notStudied = maxOf(0, vocab.totalWords - vocab.knownCount - vocab.reviewCount)
    val barFraction = if (vocab.totalWords > 0) vocab.knownCount.toFloat() / vocab.totalWords else 0f
    val bg = if (isDark) Color(0xFF162033) else Color(0xFFF0F2F5)
    val brd = if (isDark) Color(0xFF2A3B5A) else Color(0xFFD1D5DB)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(14.dp))
            .border(1.dp, brd, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
            // HSK level + percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "HSK ${vocab.level}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Text(
                    "${vocab.percent.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandTeal,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { barFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = BrandTeal,
                trackColor = if (isDark) Color.White.copy(alpha = 0.08f)
                else Color.Black.copy(alpha = 0.06f),
            )

            Spacer(Modifier.height(10.dp))

            // Three-column breakdown: Выучено / Повторить / Не изучено
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                VocabStatPill(
                    label = "Выучено",
                    count = vocab.knownCount,
                    color = BrandTeal,
                    secondaryText = secondaryText,
                    modifier = Modifier.weight(1f),
                )
                VocabStatPill(
                    label = "Повторить",
                    count = vocab.reviewCount,
                    color = BrandCoral,
                    secondaryText = secondaryText,
                    modifier = Modifier.weight(1f),
                )
                VocabStatPill(
                    label = "Не изучено",
                    count = notStudied,
                    color = secondaryText,
                    secondaryText = secondaryText,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

@Composable
private fun VocabStatPill(
    label: String,
    count: Int,
    color: Color,
    secondaryText: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "$count",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 14.sp,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = secondaryText,
            fontSize = 9.sp,
        )
    }
}

// ════════════════════════════════════════════════════════════
// Helpers
// ════════════════════════════════════════════════════════════

private fun russianPlural(count: Int, one: String, few: String, many: String): String {
    val mod100 = count % 100
    val mod10 = count % 10
    return when {
        mod100 in 11..19 -> many
        mod10 == 1 -> one
        mod10 in 2..4 -> few
        else -> many
    }
}
