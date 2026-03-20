package com.subnetik.unlock.presentation.screens.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.R
import com.subnetik.unlock.data.remote.dto.admin.AdminGroup
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
import java.util.*

@Composable
fun TeacherHomeScreen(
    onNavigateToNotifications: () -> Unit,
    viewModel: TeacherHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()

    // Full-screen overlays
    if (uiState.selectedGroup != null) {
        TeacherGroupDetailScreen(
            viewModel = viewModel,
            isDark = isDark,
        )
        return
    }

    if (uiState.showNewHomework) {
        TeacherNewHomeworkScreen(
            viewModel = viewModel,
            isDark = isDark,
        )
        return
    }

    // Main teacher home
    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Top bar: UNLOCK logo + notification bell
            TeacherTopBar(
                isDark = isDark,
                unreadCount = uiState.unreadNotifications,
                onNotifications = onNavigateToNotifications,
            )

            // Welcome card
            TeacherGreetingCard(
                displayName = uiState.displayName,
                groupsCount = uiState.groups.size,
                studentsCount = uiState.totalStudents,
                isDark = isDark,
            )

            Spacer(Modifier.height(Brand.Spacing.md))

            // Stats row
            TeacherStatsRow(
                groupsCount = uiState.groups.size,
                studentsCount = uiState.totalStudents,
                todayCount = uiState.todayLessonsCount,
                isDark = isDark,
            )

            Spacer(Modifier.height(Brand.Spacing.xl))

            Column(
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.lg),
            ) {
                // Next lesson section
                TeacherNextLessonSection(uiState = uiState, isDark = isDark, onGroupClick = { viewModel.selectGroup(it) })

                // Today's lessons
                TeacherTodayLessonsSection(uiState = uiState, isDark = isDark, onGroupClick = { viewModel.selectGroup(it) })

                // Week schedule
                TeacherWeekScheduleSection(uiState = uiState, isDark = isDark)

                Spacer(Modifier.height(Brand.Spacing.xl))
            }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────

@Composable
private fun TeacherTopBar(
    isDark: Boolean,
    unreadCount: Int,
    onNotifications: () -> Unit,
) {
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val iconTint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        shape = Brand.Shapes.extraLarge,
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.unlock_logo),
                contentDescription = "Unlock",
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Text(
                text = "UNLOCK",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                letterSpacing = 2.sp,
            )
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge(containerColor = BrandCoral) {
                            Text("$unreadCount", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            ) {
                IconButton(onClick = onNotifications) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Уведомления",
                        tint = iconTint,
                    )
                }
            }
        }
    }
}

// ─── Greeting Card ────────────────────────────────────────────────

@Composable
private fun TeacherGreetingCard(
    displayName: String?,
    groupsCount: Int,
    studentsCount: Int,
    isDark: Boolean,
) {
    val firstName = displayName?.trim()?.split(" ")?.firstOrNull() ?: "Учитель"
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Brand.Spacing.lg),
        shape = Brand.Shapes.extraLarge,
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(modifier = Modifier.padding(Brand.Spacing.xl)) {
            // Role badge
            Surface(
                shape = Brand.Shapes.full,
                color = BrandGreen.copy(alpha = 0.2f),
            ) {
                Text(
                    text = "Преподаватель",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreen,
                )
            }

            Spacer(Modifier.height(Brand.Spacing.lg))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "你好, ",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Text(
                    text = firstName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
            }

            Spacer(Modifier.height(Brand.Spacing.sm))

            Text(
                text = "$groupsCount групп • $studentsCount ученика",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryText,
            )
        }
    }
}

// ─── Stats Row ────────────────────────────────────────────────────

@Composable
private fun TeacherStatsRow(
    groupsCount: Int,
    studentsCount: Int,
    todayCount: Int,
    isDark: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Brand.Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
    ) {
        TeacherStatPill(
            value = "$groupsCount",
            label = "ГРУПП",
            tint = BrandGreen,
            isDark = isDark,
            modifier = Modifier.weight(1f),
        )
        TeacherStatPill(
            value = "$studentsCount",
            label = "УЧЕНИКОВ",
            tint = BrandGold,
            isDark = isDark,
            modifier = Modifier.weight(1f),
        )
        TeacherStatPill(
            value = "$todayCount",
            label = "СЕГОДНЯ",
            tint = BrandBlue,
            isDark = isDark,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TeacherStatPill(
    value: String,
    label: String,
    tint: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val strokeColor = if (isDark) tint.copy(alpha = 0.3f) else tint.copy(alpha = 0.2f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = tint.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = tint,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─── Next Lesson Section ──────────────────────────────────────────

@Composable
private fun TeacherNextLessonSection(
    uiState: TeacherHomeUiState,
    isDark: Boolean,
    onGroupClick: (AdminGroup) -> Unit,
) {
    val todayToken = remember { TeacherHomeViewModel.todayToken() }
    val todayGroups = remember(uiState.groups) {
        uiState.groups
            .filter { TeacherHomeViewModel.matchesSchedule(it.scheduleDays, todayToken) }
            .sortedBy { TeacherHomeViewModel.parseStartTime(it.scheduleTime) }
    }

    val now = remember { Calendar.getInstance() }
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

    // Find next upcoming lesson today
    val nextTodayGroup = remember(todayGroups) {
        todayGroups.firstOrNull { group ->
            val (start, _) = TeacherHomeViewModel.parseTimeRange(group.scheduleTime)
            val startMins = TeacherHomeViewModel.parseMinutes(start)
            startMins != null && startMins > nowMinutes
        }
    }

    // If no upcoming today, find next day's first lesson
    data class NextLesson(val group: AdminGroup, val dayLabel: String, val daysAhead: Int)

    val nextLesson: NextLesson? = remember(uiState.groups, nextTodayGroup) {
        if (nextTodayGroup != null) {
            NextLesson(nextTodayGroup, "Сегодня", 0)
        } else {
            // Check next 7 days
            val dayTokens = listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс")
            val dayLabels = listOf("Завтра", "Послезавтра", "", "", "", "", "")
            val cal = Calendar.getInstance()
            val todayDow = cal.get(Calendar.DAY_OF_WEEK)
            // Map Calendar.MONDAY..SUNDAY to 0..6
            val dowToIdx = mapOf(
                Calendar.MONDAY to 0, Calendar.TUESDAY to 1, Calendar.WEDNESDAY to 2,
                Calendar.THURSDAY to 3, Calendar.FRIDAY to 4, Calendar.SATURDAY to 5, Calendar.SUNDAY to 6,
            )
            val todayIdx = dowToIdx[todayDow] ?: 0

            for (ahead in 1..6) {
                val nextIdx = (todayIdx + ahead) % 7
                val nextToken = dayTokens[nextIdx]
                val matchingGroups = uiState.groups
                    .filter { TeacherHomeViewModel.matchesSchedule(it.scheduleDays, nextToken) }
                    .sortedBy { TeacherHomeViewModel.parseStartTime(it.scheduleTime) }
                if (matchingGroups.isNotEmpty()) {
                    val label = if (ahead == 1) "Завтра" else if (ahead == 2) "Послезавтра" else {
                        val names = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                        names[nextIdx]
                    }
                    return@remember NextLesson(matchingGroups.first(), label, ahead)
                }
            }
            null
        }
    }

    if (nextLesson == null) return

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Column(verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
            ) {
                Text("👩‍🏫", fontSize = 18.sp)
                Text(
                    "Следующее занятие",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
            }

            val badgeText = if (nextLesson.daysAhead == 0) {
                val (start, _) = TeacherHomeViewModel.parseTimeRange(nextLesson.group.scheduleTime)
                val startMins = TeacherHomeViewModel.parseMinutes(start) ?: 0
                val diff = startMins - nowMinutes
                if (diff <= 60) "через $diff мин" else "через ${diff / 60} ч ${diff % 60} мин"
            } else {
                nextLesson.dayLabel
            }

            Surface(
                shape = Brand.Shapes.full,
                color = BrandTeal.copy(alpha = 0.12f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = BrandTeal)
                    Text(
                        badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandTeal,
                    )
                }
            }
        }

        // Next lesson card
        val (timeStart, _) = remember(nextLesson.group.scheduleTime) {
            TeacherHomeViewModel.parseTimeRange(nextLesson.group.scheduleTime)
        }
        val hskColor = hskLevelColor(nextLesson.group.hskLevel)
        val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
        val strokeColor = BrandGold.copy(alpha = 0.5f)

        Surface(
            onClick = { onGroupClick(nextLesson.group) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = cardColor,
            border = BorderStroke(1.dp, strokeColor),
        ) {
            Row(
                modifier = Modifier.padding(Brand.Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(hskLevelGradient(nextLesson.group.hskLevel)),
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HSK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${nextLesson.group.hskLevel}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(Modifier.width(Brand.Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        nextLesson.group.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(timeStart, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(nextLesson.dayLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📖 ${nextLesson.group.studentsCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─── Today's Lessons Section ──────────────────────────────────────

@Composable
private fun TeacherTodayLessonsSection(
    uiState: TeacherHomeUiState,
    isDark: Boolean,
    onGroupClick: (AdminGroup) -> Unit,
) {
    val todayToken = remember { TeacherHomeViewModel.todayToken() }
    val todayGroups = remember(uiState.groups) {
        uiState.groups
            .filter { TeacherHomeViewModel.matchesSchedule(it.scheduleDays, todayToken) }
            .sortedBy { TeacherHomeViewModel.parseStartTime(it.scheduleTime) }
    }

    if (todayGroups.isEmpty()) return

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Column(verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
        ) {
            Text("📋", fontSize = 18.sp)
            Text(
                "Занятия сегодня",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primaryText,
            )
        }

        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        todayGroups.forEach { group ->
            val (start, end) = TeacherHomeViewModel.parseTimeRange(group.scheduleTime)
            val startMins = TeacherHomeViewModel.parseMinutes(start) ?: 0
            val endMins = TeacherHomeViewModel.parseMinutes(end) ?: 0

            val status = when {
                nowMinutes > endMins -> "completed"
                nowMinutes in startMins..endMins -> "live"
                else -> "upcoming"
            }

            val diff = startMins - nowMinutes
            val remaining = endMins - nowMinutes
            val statusText = when (status) {
                "completed" -> "Завершён"
                "live" -> if (remaining > 0) "Идёт • ещё $remaining мин" else "Идёт"
                else -> if (diff <= 60) "Через $diff мин" else "Через ${diff / 60} ч ${diff % 60} мин"
            }

            TeacherTodayLessonCard(
                group = group,
                statusText = statusText,
                status = status,
                isDark = isDark,
                onClick = { onGroupClick(group) },
            )
        }
    }
}

@Composable
private fun TeacherTodayLessonCard(
    group: AdminGroup,
    statusText: String,
    status: String,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val (timeStart, timeEnd) = remember(group.scheduleTime) {
        TeacherHomeViewModel.parseTimeRange(group.scheduleTime)
    }
    val hskColor = hskLevelColor(group.hskLevel)
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = when (status) {
        "live" -> BrandGold.copy(alpha = 0.5f)
        "upcoming" -> hskColor.copy(alpha = 0.3f)
        else -> if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
    }
    val statusColor = when (status) {
        "completed" -> BrandGreen
        "live" -> BrandGold
        else -> BrandTeal
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Row(
            modifier = Modifier.padding(Brand.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // HSK badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.linearGradient(hskLevelGradient(group.hskLevel)),
                        RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HSK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${group.hskLevel}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.width(Brand.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "$timeStart - $timeEnd",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("📖 ${group.studentsCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        "${group.studentsCount}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = statusColor,
                )
            }
        }
    }
}

// ─── Lesson Card (next lesson) ────────────────────────────────────

@Composable
private fun TeacherLessonCard(
    group: AdminGroup,
    isDark: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit,
) {
    val (timeStart, _) = remember(group.scheduleTime) {
        TeacherHomeViewModel.parseTimeRange(group.scheduleTime)
    }
    val hskColor = hskLevelColor(group.hskLevel)
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isHighlighted) BrandGold.copy(alpha = 0.5f) else if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Row(
            modifier = Modifier.padding(Brand.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(timeStart, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Сегодня", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📖 ${group.studentsCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─── Week Schedule Section ────────────────────────────────────────

@Composable
private fun TeacherWeekScheduleSection(
    uiState: TeacherHomeUiState,
    isDark: Boolean,
) {
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val lessonsPerDay = remember(uiState.groups) {
        TeacherHomeViewModel.countLessonsPerDay(uiState.groups)
    }

    val todayShort = remember { TeacherHomeViewModel.todayShortTitle() }

    Column(verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
        ) {
            Text("📅", fontSize = 18.sp)
            Text(
                "Расписание на неделю",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primaryText,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.xs),
        ) {
            lessonsPerDay.forEach { (dayLabel, count) ->
                val isToday = dayLabel == todayShort
                TeacherWeekDayPill(
                    dayLabel = dayLabel,
                    count = count,
                    isToday = isToday,
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TeacherWeekDayPill(
    dayLabel: String,
    count: Int,
    isToday: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val bgColor = when {
        isToday -> BrandTeal.copy(alpha = 0.15f)
        else -> if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f)
    }
    val strokeColor = when {
        isToday -> BrandTeal.copy(alpha = 0.4f)
        else -> if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
    }
    val textColor = when {
        isToday -> BrandTeal
        count > 0 -> if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        else -> if (isDark) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                dayLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "$count",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        }
    }
}
