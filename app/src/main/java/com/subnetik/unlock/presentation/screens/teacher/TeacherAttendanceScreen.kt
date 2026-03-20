package com.subnetik.unlock.presentation.screens.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TeacherAttendanceScreen(
    viewModel: TeacherHomeViewModel,
    isDark: Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()
    val student = uiState.selectedStudent ?: return

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    val attendanceMap = uiState.attendanceMap
    val lessonDates = uiState.lessonDates
    val presentCount = attendanceMap.count { it.value }
    val totalCount = lessonDates.size
    val percent = if (totalCount > 0) presentCount * 100 / totalCount else 0

    val monthFormat = remember { SimpleDateFormat("LLLL yyyy", Locale("ru")) }
    val monthName = remember { monthFormat.format(Date()).replaceFirstChar { it.titlecase(Locale("ru")) } }

    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
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
                    student.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Surface(
                    onClick = { viewModel.closeAttendance() },
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

            Spacer(Modifier.height(Brand.Spacing.xl))

            // Attendance card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Brand.Spacing.lg),
                shape = RoundedCornerShape(18.dp),
                color = cardColor,
                border = BorderStroke(1.dp, strokeColor),
            ) {
                Column(modifier = Modifier.padding(Brand.Spacing.xl)) {
                    Text(
                        "Посещаемость",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )
                    Spacer(Modifier.height(Brand.Spacing.xs))
                    Text(
                        monthName,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText,
                    )

                    Spacer(Modifier.height(Brand.Spacing.lg))

                    // Date chips grid
                    val dateFormat = remember { SimpleDateFormat("dd.MM", Locale.US) }
                    val parseFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

                    // FlowRow-like: 4 per row
                    val chunks = lessonDates.chunked(4)
                    chunks.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                        ) {
                            row.forEach { date ->
                                val isPresent = attendanceMap[date] == true
                                val isToday = date == today
                                val displayDate = try {
                                    val parsed = parseFormat.parse(date)
                                    dateFormat.format(parsed!!)
                                } catch (_: Exception) {
                                    date.takeLast(5)
                                }

                                Surface(
                                    onClick = { viewModel.toggleAttendance(date) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isPresent) BrandTeal.copy(alpha = 0.15f) else if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isPresent) BrandTeal.copy(alpha = 0.4f) else if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            displayDate,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPresent) BrandTeal else primaryText,
                                        )
                                        if (isToday) {
                                            Text(
                                                "Сегодня",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = secondaryText,
                                            )
                                        }
                                    }
                                }
                            }
                            // Fill remaining space if row < 4
                            repeat(4 - row.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(Modifier.height(Brand.Spacing.lg))

                    // Total
                    Text(
                        "Итого: $presentCount/$totalCount • $percent%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText,
                    )

                    Spacer(Modifier.height(Brand.Spacing.lg))

                    // Save button
                    Button(
                        onClick = { viewModel.saveAttendance() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isSavingAttendance,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                        ),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(GradientIndigo),
                                    RoundedCornerShape(12.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (uiState.isSavingAttendance) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                            } else {
                                Text(
                                    "Сохранить",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
