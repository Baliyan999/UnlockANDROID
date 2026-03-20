package com.subnetik.unlock.presentation.screens.test

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

@Composable
fun TestSessionScreen(
    level: Int,
    onComplete: (score: Int, total: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: TestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    // Start test or show start screen
    var testStarted by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.testComplete) {
        if (uiState.testComplete && testStarted) {
            onComplete(uiState.score, uiState.totalQuestions)
        }
    }

    val color = hskLevelColor(level)
    val question = uiState.currentQuestion

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar: back + title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Brand.Spacing.sm, vertical = Brand.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = primaryText,
                    )
                }
                Text(
                    "HSK $level",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Spacer(Modifier.width(48.dp)) // balance
            }

            if (!testStarted) {
                // ═══ START SCREEN ═══
                TestStartContent(
                    level = level,
                    bestPercent = uiState.levelProgress[level]?.bestPercent,
                    attempts = uiState.levelProgress[level]?.attempts ?: 0,
                    isDark = isDark,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    onStart = {
                        viewModel.startTest(level)
                        testStarted = true
                    },
                )
            } else if (question != null) {
                // ═══ QUESTION SCREEN ═══
                TestQuestionContent(
                    level = level,
                    uiState = uiState,
                    viewModel = viewModel,
                    isDark = isDark,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    color = color,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun TestStartContent(
    level: Int,
    bestPercent: Int?,
    attempts: Int,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
    cardColor: Color,
    strokeColor: Color,
    onStart: () -> Unit,
) {
    val descriptions = mapOf(
        1 to "Начальный уровень. Базовые слова и фразы.",
        2 to "Базовый уровень. Простые предложения.",
        3 to "Средний уровень. Повседневное общение.",
        4 to "Продвинутый. Свободная беседа.",
        5 to "Высший. Академическая лексика.",
        6 to "Мастер. Носитель языка.",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Brand.Spacing.lg)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Brand.Spacing.xxl))

        // Main card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = cardColor,
            border = BorderStroke(1.dp, strokeColor),
        ) {
            Column(
                modifier = Modifier.padding(Brand.Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Document icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(BrandBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = BrandBlue,
                    )
                }

                Spacer(Modifier.height(Brand.Spacing.lg))

                Text(
                    "HSK $level",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )

                Spacer(Modifier.height(Brand.Spacing.sm))

                Text(
                    descriptions[level] ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryText,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(Brand.Spacing.xl))

                // Info badges row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                ) {
                    StartInfoBadge(Icons.Default.FormatListNumbered, "10 из 100", BrandBlue, isDark, Modifier.weight(1f))
                    StartInfoBadge(Icons.Default.CheckCircle, "Проход: 80%", BrandTeal, isDark, Modifier.weight(1f))
                    StartInfoBadge(Icons.Default.Timer, "10 мин", BrandGold, isDark, Modifier.weight(1f))
                }

                // Best result
                if (bestPercent != null && attempts > 0) {
                    Spacer(Modifier.height(Brand.Spacing.lg))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = BrandTeal,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Лучший результат: $bestPercent%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandTeal,
                        )
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.xl))

                // Start button
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = Brand.Shapes.full,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                ) {
                    Text(
                        if (attempts > 0) "Пройти снова" else "Начать тест",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StartInfoBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = tint.copy(alpha = 0.08f),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = tint)
            Spacer(Modifier.height(4.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TestQuestionContent(
    level: Int,
    uiState: TestUiState,
    viewModel: TestViewModel,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
    cardColor: Color,
    strokeColor: Color,
    color: Color,
) {
    val question = uiState.currentQuestion ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Brand.Spacing.lg),
    ) {
        Spacer(Modifier.height(Brand.Spacing.md))

        // Progress header card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = cardColor,
            border = BorderStroke(1.dp, strokeColor),
        ) {
            Column(modifier = Modifier.padding(Brand.Spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "HSK $level",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                        Text(
                            formatTime(uiState.remainingSeconds),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.remainingSeconds < 60) BrandCoral else secondaryText,
                        )
                        Text(
                            "${uiState.currentQuestionIndex + 1}/${uiState.totalQuestions}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = secondaryText,
                        )
                    }
                }
                Spacer(Modifier.height(Brand.Spacing.sm))
                LinearProgressIndicator(
                    progress = {
                        if (uiState.totalQuestions > 0)
                            (uiState.currentQuestionIndex + 1).toFloat() / uiState.totalQuestions
                        else 0f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(Brand.Shapes.full),
                    color = BrandBlue,
                    trackColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                )
            }
        }

        Spacer(Modifier.height(Brand.Spacing.lg))

        // Question + answers card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = cardColor,
            border = BorderStroke(1.dp, strokeColor),
        ) {
            Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                Text(
                    text = question.prompt,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )

                Spacer(Modifier.height(Brand.Spacing.lg))

                // Answer options
                question.answers.forEach { answer ->
                    val isSelected = uiState.selectedAnswerId == answer.id
                    val showResult = uiState.answerRevealed

                    val answerBg by animateColorAsState(
                        targetValue = when {
                            showResult && answer.isCorrect -> BrandGreen.copy(alpha = 0.12f)
                            showResult && isSelected && !answer.isCorrect -> BrandCoral.copy(alpha = 0.12f)
                            isSelected -> BrandBlue.copy(alpha = 0.08f)
                            else -> if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.04f)
                        },
                        animationSpec = tween(300),
                        label = "bg",
                    )

                    val answerBorder by animateColorAsState(
                        targetValue = when {
                            showResult && answer.isCorrect -> BrandGreen.copy(alpha = 0.5f)
                            showResult && isSelected && !answer.isCorrect -> BrandCoral.copy(alpha = 0.5f)
                            isSelected -> BrandBlue.copy(alpha = 0.3f)
                            else -> Color.Transparent
                        },
                        animationSpec = tween(300),
                        label = "border",
                    )

                    Surface(
                        onClick = { if (!showResult) viewModel.selectAnswer(answer.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = answerBg,
                        border = if (answerBorder != Color.Transparent) BorderStroke(1.dp, answerBorder) else null,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Brand.Spacing.lg, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = answer.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = primaryText,
                                fontWeight = if (showResult && answer.isCorrect) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                            )
                            if (showResult && answer.isCorrect) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = BrandGreen)
                            } else if (showResult && isSelected && !answer.isCorrect) {
                                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(20.dp), tint = BrandCoral)
                            }
                        }
                    }
                }
            }
        }

        // Explanation
        if (uiState.answerRevealed && question.explanation != null) {
            Spacer(Modifier.height(Brand.Spacing.md))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = BrandBlue.copy(alpha = 0.06f),
                border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.15f)),
            ) {
                Text(
                    text = question.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryText,
                    modifier = Modifier.padding(Brand.Spacing.md),
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Next button
        Button(
            onClick = { viewModel.nextQuestion() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Brand.Spacing.lg)
                .height(50.dp),
            shape = Brand.Shapes.full,
            enabled = uiState.answerRevealed,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandBlue,
                disabledContainerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f),
            ),
        ) {
            Text(
                if (uiState.currentQuestionIndex < uiState.totalQuestions - 1)
                    "Следующий вопрос" else "Завершить тест",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (uiState.answerRevealed) Color.White else secondaryText,
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
