package com.subnetik.unlock.presentation.screens.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

@Composable
fun TestResultScreen(
    level: Int,
    score: Int,
    total: Int,
    showTrialButton: Boolean = false,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    val percent = if (total > 0) (score * 100) / total else 0
    val passed = percent >= 80
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    val resultColor = if (passed) BrandGreen else BrandCoral
    val resultIcon = if (passed) Icons.Default.CheckCircle else Icons.Default.Cancel
    val resultTitle = if (passed) "Поздравляем!" else "Попробуйте ещё раз"

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Brand.Spacing.sm, vertical = Brand.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = primaryText)
                }
                Text(
                    "HSK $level",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Spacer(Modifier.width(48.dp))
            }

            // Result content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Brand.Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(Brand.Spacing.xxl))

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
                        // Result icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(resultColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                resultIcon,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = resultColor,
                            )
                        }

                        Spacer(Modifier.height(Brand.Spacing.lg))

                        Text(
                            resultTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                        )

                        Spacer(Modifier.height(Brand.Spacing.sm))

                        Text(
                            "HSK $level · $score из $total · $percent%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryText,
                        )

                        Spacer(Modifier.height(Brand.Spacing.xl))

                        if (showTrialButton) {
                            // CTA: book a trial lesson
                            OutlinedButton(
                                onClick = { /* TODO: navigate to booking */ },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = Brand.Shapes.full,
                                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)),
                            ) {
                                Text(
                                    "Записаться на пробный урок",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryText,
                                )
                            }

                            Spacer(Modifier.height(Brand.Spacing.md))
                        }

                        // Retry + Back buttons row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
                        ) {
                            Button(
                                onClick = onRetry,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = Brand.Shapes.full,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            ) {
                                Text(
                                    "Пройти снова",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }

                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = Brand.Shapes.full,
                                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)),
                            ) {
                                Text(
                                    "Назад",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryText,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
