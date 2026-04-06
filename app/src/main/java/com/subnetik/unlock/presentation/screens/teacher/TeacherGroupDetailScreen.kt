package com.subnetik.unlock.presentation.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.subnetik.unlock.data.remote.dto.admin.AdminStudent
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

@Composable
fun TeacherGroupDetailScreen(
    viewModel: TeacherHomeViewModel,
    isDark: Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()
    val group = uiState.selectedGroup ?: return

    // Homework creation overlay takes priority
    if (uiState.showNewHomework) {
        TeacherNewHomeworkScreen(viewModel = viewModel, isDark = isDark)
        return
    }

    // Attendance overlay
    if (uiState.showAttendance && uiState.selectedStudent != null) {
        TeacherAttendanceScreen(viewModel = viewModel, isDark = isDark)
        return
    }

    // Progress overlay
    if (uiState.showProgress && uiState.selectedStudent != null) {
        TeacherProgressScreen(viewModel = viewModel, isDark = isDark)
        return
    }

    // Performance event overlay
    if (uiState.showPerformanceEvent && uiState.selectedStudent != null) {
        TeacherPerformanceEventScreen(viewModel = viewModel, isDark = isDark)
        return
    }

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    val (timeStart, timeEnd) = remember(group.scheduleTime) {
        TeacherHomeViewModel.parseTimeRange(group.scheduleTime)
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
                    group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    onClick = { viewModel.closeGroupDetail() },
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

            LazyColumn(
                contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            ) {
                // Group info card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = cardColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor),
                    ) {
                        Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // HSK badge
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            Brush.linearGradient(hskLevelGradient(group.hskLevel)),
                                            RoundedCornerShape(12.dp),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("HSK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("${group.hskLevel}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Spacer(Modifier.width(Brand.Spacing.md))
                                Column {
                                    Text(
                                        group.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText,
                                    )
                                    Text(
                                        "Уровень HSK ${group.hskLevel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = secondaryText,
                                    )
                                }
                            }

                            Spacer(Modifier.height(Brand.Spacing.lg))

                            // Info chips row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                            ) {
                                InfoChip(icon = Icons.Default.Schedule, text = "$timeStart - $timeEnd", isDark = isDark, modifier = Modifier.weight(1f))
                                InfoChip(icon = Icons.Default.CalendarToday, text = group.scheduleDays ?: "", isDark = isDark, modifier = Modifier.weight(1f))
                                InfoChip(icon = Icons.Default.MeetingRoom, text = "Каб. ${group.classroom ?: "?"}", isDark = isDark, modifier = Modifier.weight(1f))
                                InfoChip(icon = Icons.Default.Groups, text = "${group.studentsCount}", isDark = isDark, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Homework section
                item {
                    Surface(
                        onClick = { viewModel.openNewHomework(group.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = cardColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor),
                    ) {
                        Row(
                            modifier = Modifier.padding(Brand.Spacing.lg),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(BrandIndigo.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = BrandIndigo, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(Brand.Spacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Задания группы",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText,
                                )
                                Text(
                                    "Создать новое домашнее задание",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = secondaryText,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(BrandBlue.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Students header
                item {
                    Text(
                        "Учащиеся группы    ${uiState.groupStudents.size}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )
                }

                if (uiState.isLoadingStudents) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Brand.Spacing.xl),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                } else {
                    items(uiState.groupStudents, key = { it.id }) { student ->
                        TeacherStudentCard(
                            student = student,
                            scheduleDays = group.scheduleDays,
                            isDark = isDark,
                            onProgressClick = { viewModel.openProgress(student) },
                            onAttendanceClick = { viewModel.openAttendance(student) },
                            onPerformanceClick = { viewModel.openPerformanceEvent(student) },
                        )
                    }
                }

                item { Spacer(Modifier.height(Brand.Spacing.xl)) }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
    val textColor = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = textColor)
            Spacer(Modifier.height(4.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TeacherStudentCard(
    student: AdminStudent,
    scheduleDays: String?,
    isDark: Boolean,
    onProgressClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onPerformanceClick: () -> Unit,
) {
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant

    val monthTotal = student.currentMonthTotalLessons(scheduleDays)
    val monthPresent = student.currentMonthAttendanceCount
    val monthPercent = student.currentMonthAttendancePercent(scheduleDays)
    val attendanceText = if (monthTotal > 0) {
        "Посещаемость: $monthPresent/$monthTotal • $monthPercent%"
    } else {
        "Посещаемость: Нет занятий"
    }

    val isLessonDay = AdminStudent.isTodayLessonDay(scheduleDays)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor),
    ) {
        Row(
            modifier = Modifier.padding(Brand.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            val avatarUrl = student.avatarUrl?.let { url ->
                if (url.startsWith("http")) url else "https://unlocklingua.com$url"
            }

            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = student.fullName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(BrandBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        student.firstName.take(1).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                    )
                }
            }

            Spacer(Modifier.width(Brand.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    student.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    attendanceText,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                )
            }

            // Today's attendance status icon (only on lesson days)
            if (isLessonDay) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            if (student.todayAttended) BrandGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.10f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (student.todayAttended) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (student.todayAttended) "Присутствует" else "Отсутствует",
                        tint = if (student.todayAttended) BrandGreen else Color.Gray,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
            }

            // Performance event button
            IconButton(
                onClick = onPerformanceClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Баллы",
                    tint = BrandGold,
                    modifier = Modifier.size(22.dp),
                )
            }

            // Progress button
            IconButton(
                onClick = onProgressClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "Прогресс",
                    tint = BrandTeal,
                    modifier = Modifier.size(22.dp),
                )
            }

            // Attendance button
            IconButton(
                onClick = onAttendanceClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Посещаемость",
                    tint = BrandBlue,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
