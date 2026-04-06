package com.subnetik.unlock.presentation.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.subnetik.unlock.data.remote.dto.admin.AdminStudent
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

// ═══════════════════════════════════════════════════════════════
// Performance Event Screen (used from TeacherGroupDetailScreen)
// ═══════════════════════════════════════════════════════════════

@Composable
fun TeacherPerformanceEventScreen(
    viewModel: TeacherHomeViewModel,
    isDark: Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()
    val student = uiState.selectedStudent ?: return

    PerformanceEventContent(
        student = student,
        eventType = uiState.perfEventType,
        rawScore = uiState.perfRawScore,
        scoreDelta = uiState.perfScoreDelta,
        comment = uiState.perfComment,
        isSubmitting = uiState.isSubmittingPerformanceEvent,
        isSuccess = uiState.performanceEventSuccess,
        isDark = isDark,
        onEventTypeChange = viewModel::updatePerfEventType,
        onRawScoreChange = viewModel::updatePerfRawScore,
        onScoreDeltaChange = viewModel::updatePerfScoreDelta,
        onCommentChange = viewModel::updatePerfComment,
        onSubmit = viewModel::submitPerformanceEvent,
        onClose = viewModel::closePerformanceEvent,
        onDismissSuccess = viewModel::dismissPerformanceEventSuccess,
    )
}

// ═══════════════════════════════════════════════════════════════
// Performance Event Screen (used from TeacherGroupsScreen)
// ═══════════════════════════════════════════════════════════════

@Composable
fun TeacherGroupsPerformanceEventScreen(
    viewModel: TeacherGroupsViewModel,
    isDark: Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()
    val student = uiState.selectedStudent ?: return

    PerformanceEventContent(
        student = student,
        eventType = uiState.perfEventType,
        rawScore = uiState.perfRawScore,
        scoreDelta = uiState.perfScoreDelta,
        comment = uiState.perfComment,
        isSubmitting = uiState.isSubmittingPerformanceEvent,
        isSuccess = uiState.performanceEventSuccess,
        isDark = isDark,
        onEventTypeChange = viewModel::updatePerfEventType,
        onRawScoreChange = viewModel::updatePerfRawScore,
        onScoreDeltaChange = viewModel::updatePerfScoreDelta,
        onCommentChange = viewModel::updatePerfComment,
        onSubmit = viewModel::submitPerformanceEvent,
        onClose = viewModel::closePerformanceEvent,
        onDismissSuccess = viewModel::dismissPerformanceEventSuccess,
    )
}

// ═══════════════════════════════════════════════════════════════
// Shared Content
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PerformanceEventContent(
    student: AdminStudent,
    eventType: String,
    rawScore: Int,
    scoreDelta: Int,
    comment: String,
    isSubmitting: Boolean,
    isSuccess: Boolean,
    isDark: Boolean,
    onEventTypeChange: (String) -> Unit,
    onRawScoreChange: (Int) -> Unit,
    onScoreDeltaChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
    onDismissSuccess: () -> Unit,
) {
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    // Success snackbar
    if (isSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            onDismissSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Оценка",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Surface(
                    onClick = onClose,
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Brand.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            ) {
                // Student info card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = cardColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor),
                ) {
                    Row(
                        modifier = Modifier.padding(Brand.Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val avatarUrl = student.avatarUrl?.let { url ->
                            if (url.startsWith("http")) url else "https://unlocklingua.com$url"
                        }
                        if (avatarUrl != null) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = student.fullName,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(BrandGold.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    student.firstName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGold,
                                )
                            }
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
                                "Начисление баллов",
                                style = MaterialTheme.typography.bodySmall,
                                color = secondaryText,
                            )
                        }
                    }
                }

                // Event type selector
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = cardColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor),
                ) {
                    Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                        Text(
                            "Тип события",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = secondaryText,
                        )
                        Spacer(Modifier.height(Brand.Spacing.sm))

                        // Segmented buttons
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.04f),
                        ) {
                            Row(modifier = Modifier.padding(3.dp)) {
                                EventTypeTab(
                                    label = "Ответ",
                                    isSelected = eventType == "lesson_answer",
                                    onClick = { onEventTypeChange("lesson_answer") },
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f),
                                )
                                EventTypeTab(
                                    label = "Контрольная",
                                    isSelected = eventType == "control_test",
                                    onClick = { onEventTypeChange("control_test") },
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f),
                                )
                                EventTypeTab(
                                    label = "Ручная",
                                    isSelected = eventType == "manual_adjustment",
                                    onClick = { onEventTypeChange("manual_adjustment") },
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Description of selected type
                        Text(
                            when (eventType) {
                                "lesson_answer" -> "Ответ на уроке (0-100 баллов)"
                                "control_test" -> "Контрольная работа (0-100 баллов)"
                                "manual_adjustment" -> "Ручная корректировка (+/- баллы)"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText,
                        )
                    }
                }

                // Score input
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = cardColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor),
                ) {
                    Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                        if (eventType == "manual_adjustment") {
                            // Score delta input
                            Text(
                                "Корректировка баллов",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryText,
                            )
                            Spacer(Modifier.height(Brand.Spacing.sm))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                            ) {
                                // Minus button
                                FilledIconButton(
                                    onClick = { onScoreDeltaChange(scoreDelta - 1) },
                                    modifier = Modifier.size(40.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = BrandRed.copy(alpha = 0.12f),
                                        contentColor = BrandRed,
                                    ),
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Уменьшить", modifier = Modifier.size(20.dp))
                                }

                                // Delta display
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.04f),
                                ) {
                                    Text(
                                        text = if (scoreDelta >= 0) "+$scoreDelta" else "$scoreDelta",
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (scoreDelta >= 0) BrandGreen else BrandRed,
                                        textAlign = TextAlign.Center,
                                    )
                                }

                                // Plus button
                                FilledIconButton(
                                    onClick = { onScoreDeltaChange(scoreDelta + 1) },
                                    modifier = Modifier.size(40.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = BrandGreen.copy(alpha = 0.12f),
                                        contentColor = BrandGreen,
                                    ),
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Увеличить", modifier = Modifier.size(20.dp))
                                }
                            }

                            Spacer(Modifier.height(Brand.Spacing.sm))

                            // Quick adjustment buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.xs),
                            ) {
                                listOf(-10, -5, -1, 1, 5, 10).forEach { delta ->
                                    val isPositive = delta > 0
                                    Surface(
                                        onClick = { onScoreDeltaChange(scoreDelta + delta) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isPositive) BrandGreen.copy(alpha = 0.08f) else BrandRed.copy(alpha = 0.08f),
                                    ) {
                                        Text(
                                            text = if (isPositive) "+$delta" else "$delta",
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isPositive) BrandGreen else BrandRed,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        } else {
                            // Raw score input (0-100)
                            Text(
                                "Оценка (0-100)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryText,
                            )
                            Spacer(Modifier.height(Brand.Spacing.sm))

                            // Score display
                            Text(
                                "$rawScore",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    rawScore >= 80 -> BrandGreen
                                    rawScore >= 50 -> BrandGold
                                    else -> BrandRed
                                },
                                textAlign = TextAlign.Center,
                            )

                            Spacer(Modifier.height(Brand.Spacing.sm))

                            // Slider
                            Slider(
                                value = rawScore.toFloat(),
                                onValueChange = { onRawScoreChange(it.toInt()) },
                                valueRange = 0f..100f,
                                steps = 0,
                                colors = SliderDefaults.colors(
                                    thumbColor = BrandGold,
                                    activeTrackColor = BrandGold,
                                    inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                                ),
                            )

                            // Quick score buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.xs),
                            ) {
                                listOf(0, 25, 50, 75, 100).forEach { score ->
                                    val isSelected = rawScore == score
                                    Surface(
                                        onClick = { onRawScoreChange(score) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) BrandGold.copy(alpha = 0.2f)
                                        else if (isDark) Color.White.copy(alpha = 0.06f)
                                        else Color.Black.copy(alpha = 0.04f),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, BrandGold.copy(alpha = 0.4f)) else null,
                                    ) {
                                        Text(
                                            text = "$score",
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) BrandGold else secondaryText,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Comment field
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = cardColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor),
                ) {
                    Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                        Text(
                            "Комментарий (необязательно)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = secondaryText,
                        )
                        Spacer(Modifier.height(Brand.Spacing.sm))
                        OutlinedTextField(
                            value = comment,
                            onValueChange = onCommentChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Например: Хороший ответ") },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandGold,
                                unfocusedBorderColor = strokeColor,
                                cursorColor = BrandGold,
                            ),
                        )
                    }
                }

                // Submit button
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isSubmitting && !isSuccess,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGold,
                        contentColor = Color.White,
                        disabledContainerColor = BrandGold.copy(alpha = 0.4f),
                    ),
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                        Spacer(Modifier.width(Brand.Spacing.sm))
                        Text("Отправка...")
                    } else if (isSuccess) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(Brand.Spacing.sm))
                        Text("Отправлено!")
                    } else {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(Brand.Spacing.sm))
                        Text(
                            "Отправить",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.xl))
            }
        }

        // Success overlay
        if (isSuccess) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(Brand.Spacing.lg)
                    .padding(bottom = Brand.Spacing.xl),
                shape = RoundedCornerShape(14.dp),
                color = BrandGreen,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Brand.Spacing.sm))
                    Text(
                        "Баллы начислены!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun EventTypeTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) {
            if (isDark) Color.White.copy(alpha = 0.12f) else Color.White
        } else {
            Color.Transparent
        },
        shadowElevation = if (isSelected) 1.dp else 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BrandGold else primaryText.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
