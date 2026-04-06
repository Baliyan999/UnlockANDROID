package com.subnetik.unlock.presentation.screens.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.data.remote.dto.admin.AdminGroup
import com.subnetik.unlock.data.remote.dto.admin.AdminHomeworkAssignment
import com.subnetik.unlock.data.remote.dto.admin.TeacherGroupRatingStudent
import com.subnetik.unlock.data.remote.dto.progress.TestAttemptDetail
import com.subnetik.unlock.data.remote.dto.progress.TestProgressSyncItem
import com.subnetik.unlock.data.remote.dto.progress.VocabProgressSyncItem
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TeacherGroupsScreen(
    viewModel: TeacherGroupsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()

    // Full-screen overlays — homework creation takes priority
    if (uiState.showNewHomework) {
        TeacherGroupsNewHomeworkScreen(viewModel = viewModel, isDark = isDark)
        return
    }

    if (uiState.selectedGroup != null) {
        TeacherGroupsDetailScreen(viewModel = viewModel, isDark = isDark)
        return
    }

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // Tab selector: Мои группы | Домашние задания
            val tabs = listOf("Мои группы", "Домашние задания")
            val selectedTab = uiState.selectedTab

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.sm),
                shape = Brand.Shapes.full,
                color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f),
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Surface(
                            onClick = { viewModel.selectTab(index) },
                            modifier = Modifier.weight(1f),
                            shape = Brand.Shapes.full,
                            color = if (isSelected) {
                                if (isDark) Color.White.copy(alpha = 0.12f) else Color.White
                            } else Color.Transparent,
                        ) {
                            Text(
                                text = title,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) primaryText else primaryText.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> MyGroupsTab(uiState = uiState, isDark = isDark, viewModel = viewModel)
                1 -> HomeworkTab(uiState = uiState, isDark = isDark, viewModel = viewModel)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// TAB 1: Мои группы
// ═══════════════════════════════════════════════════════════════

@Composable
private fun MyGroupsTab(
    uiState: TeacherGroupsUiState,
    isDark: Boolean,
    viewModel: TeacherGroupsViewModel,
) {
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val todayToken = remember { TeacherHomeViewModel.todayToken() }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        // Header with refresh
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Мои группы",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                IconButton(onClick = { viewModel.loadData() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        tint = BrandBlue,
                    )
                }
            }
        }

        // Stats row
        item {
            val todayGroups = uiState.groups.filter {
                TeacherHomeViewModel.matchesSchedule(it.scheduleDays, todayToken)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
            ) {
                GroupStatPill("${uiState.groups.size}", "ВСЕГО ГРУПП", BrandGreen, isDark, Modifier.weight(1f))
                GroupStatPill("${uiState.totalStudents}", "МОИХ УЧЕНИКОВ", BrandGold, isDark, Modifier.weight(1f))
                GroupStatPill("${todayGroups.size}", "УРОКОВ СЕГОДНЯ", BrandBlue, isDark, Modifier.weight(1f))
            }
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) }
            }
        } else {
            // Group cards
            items(uiState.groups, key = { it.id }) { group ->
                val isToday = TeacherHomeViewModel.matchesSchedule(group.scheduleDays, todayToken)
                TeacherGroupCard(
                    group = group,
                    isToday = isToday,
                    isDark = isDark,
                    onClick = { viewModel.selectGroup(group) },
                )
            }
        }

        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}

@Composable
private fun GroupStatPill(
    value: String, label: String, tint: Color, isDark: Boolean, modifier: Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = tint.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, tint.copy(alpha = if (isDark) 0.3f else 0.2f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = tint)
            Spacer(Modifier.height(2.dp))
            Text(
                label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp,
                color = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TeacherGroupCard(
    group: AdminGroup, isToday: Boolean, isDark: Boolean, onClick: () -> Unit,
) {
    val hskColor = hskLevelColor(group.hskLevel)
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isToday) BrandGold.copy(alpha = 0.5f) else hskColor.copy(alpha = 0.3f)
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant

    val (timeStart, timeEnd) = remember(group.scheduleTime) {
        TeacherHomeViewModel.parseTimeRange(group.scheduleTime)
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
                    .size(48.dp)
                    .background(Brush.linearGradient(hskLevelGradient(group.hskLevel)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HSK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${group.hskLevel}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.width(Brand.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        group.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isToday) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = Brand.Shapes.full,
                            color = BrandGold.copy(alpha = 0.15f),
                        ) {
                            Text(
                                "Сегодня",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandGold,
                                fontSize = 10.sp,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = secondaryText)
                        Text("$timeStart - $timeEnd", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = secondaryText)
                        Text(group.scheduleDays ?: "", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                    }
                    if (group.classroom != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(12.dp), tint = secondaryText)
                            Text(group.classroom, style = MaterialTheme.typography.bodySmall, color = secondaryText)
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${group.studentsCount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = hskColor,
                )
                Text(
                    "учеников",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = secondaryText,
                )
            }

            Spacer(Modifier.width(4.dp))

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = secondaryText,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// TAB 2: Домашние задания
// ═══════════════════════════════════════════════════════════════

@Composable
private fun HomeworkTab(
    uiState: TeacherGroupsUiState,
    isDark: Boolean,
    viewModel: TeacherGroupsViewModel,
) {
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant

    // Sub-tabs: Задания | Рейтинг
    val hwTabs = listOf("Задания", "Рейтинг")
    val selectedHwTab = uiState.selectedHomeworkTab

    LazyColumn(
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        // Title + buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Домашние задания",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (selectedHwTab == 0) {
                        IconButton(onClick = { viewModel.openNewHomework() }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Создать", tint = BrandBlue)
                        }
                    }
                    IconButton(onClick = { viewModel.loadHomework() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить", tint = BrandBlue)
                    }
                }
            }
        }

        // Sub-tab row
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = Brand.Shapes.full,
                color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f),
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    hwTabs.forEachIndexed { index, title ->
                        val isSelected = selectedHwTab == index
                        Surface(
                            onClick = { viewModel.selectHomeworkTab(index) },
                            modifier = Modifier.weight(1f),
                            shape = Brand.Shapes.full,
                            color = if (isSelected) {
                                if (isDark) Color.White.copy(alpha = 0.12f) else Color.White
                            } else Color.Transparent,
                        ) {
                            Text(
                                text = title,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) primaryText else primaryText.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }

        when (selectedHwTab) {
            0 -> {
                // Stats row
                item {
                    val activeCount = uiState.assignments.count { it.isCompleted != true }
                    val completedCount = uiState.assignments.count { it.isCompleted == true }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                    ) {
                        GroupStatPill("$activeCount", "ВСЕГО ЗАДАНИЙ", BrandGreen, isDark, Modifier.weight(1f))
                        GroupStatPill("${uiState.groups.size}", "ГРУПП", BrandTeal, isDark, Modifier.weight(1f))
                        GroupStatPill("$completedCount", "ПРОСРОЧЕНО", BrandCoral, isDark, Modifier.weight(1f))
                    }
                }

                // Show completed toggle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleShowCompleted() },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (uiState.showCompleted) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = BrandTeal,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (uiState.showCompleted) "Скрыть завершённые" else "Показать завершённые",
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandTeal,
                        )
                    }
                }

                if (uiState.isLoadingHomework) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) }
                    }
                } else {
                    val filtered = if (uiState.showCompleted) {
                        uiState.assignments
                    } else {
                        uiState.assignments.filter { it.isCompleted != true }
                    }

                    if (filtered.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("Нет заданий", color = secondaryText)
                            }
                        }
                    } else {
                        items(filtered, key = { it.id }) { assignment ->
                            HomeworkCard(assignment = assignment, isDark = isDark, viewModel = viewModel)
                        }
                    }
                }
            }

            1 -> {
                // Rating tab - group filter chips (always from teacher's groups)
                item {
                    if (uiState.groups.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.groups, key = { it.id }) { group ->
                                val isSelected = uiState.ratingSelectedGroupId == group.id
                                Surface(
                                    onClick = { viewModel.selectRatingGroup(group.id) },
                                    shape = Brand.Shapes.full,
                                    color = if (isSelected) BrandBlue else if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f),
                                    border = if (isSelected) null else BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f)),
                                ) {
                                    Text(
                                        group.name,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else primaryText.copy(alpha = 0.7f),
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.isLoadingRating) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) }
                    }
                } else {
                    val students = uiState.groupRating?.rating
                        ?.sortedBy { it.rank }
                        ?: emptyList()

                    if (students.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    Icons.Default.BarChart,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = BrandGold.copy(alpha = 0.4f),
                                )
                                Spacer(Modifier.height(Brand.Spacing.md))
                                Text("Нет данных рейтинга", color = secondaryText)
                            }
                        }
                    } else {
                        items(students.size) { index ->
                            TeacherRatingStudentCard(
                                student = students[index],
                                rank = students[index].rank,
                                isDark = isDark,
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}

@Composable
private fun HomeworkCard(
    assignment: AdminHomeworkAssignment, isDark: Boolean, viewModel: TeacherGroupsViewModel,
) {
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant

    // isCompleted from backend = deadline has passed
    val isCompleted = assignment.isCompleted == true

    val strokeColor = when {
        isCompleted -> BrandCoral.copy(alpha = 0.5f)
        else -> if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    }

    val iconBgColor = when {
        isCompleted -> BrandCoral.copy(alpha = 0.15f)
        else -> BrandGold.copy(alpha = 0.15f)
    }

    val iconTint = when {
        isCompleted -> BrandCoral
        else -> BrandGold
    }

    Surface(
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
                    .size(44.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isCompleted) Icons.Default.Warning else Icons.Default.Assignment,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(Brand.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    assignment.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(12.dp), tint = secondaryText)
                        Text(assignment.groupName, style = MaterialTheme.typography.bodySmall, color = secondaryText, maxLines = 1)
                    }
                    if (assignment.dueDate != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = if (isCompleted) BrandCoral else secondaryText)
                            Text(
                                formatDueDate(assignment.dueDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompleted) BrandCoral else secondaryText,
                            )
                        }
                    }
                }
                if (!assignment.description.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        assignment.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = secondaryText,
            )
        }
    }
}

@Composable
private fun TeacherRatingStudentCard(
    student: TeacherGroupRatingStudent,
    rank: Int,
    isDark: Boolean,
) {
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant

    val rankColor = when (rank) {
        1 -> BrandGold
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> secondaryText
    }

    val trendIcon = when (student.trend) {
        "up" -> Icons.Default.TrendingUp
        "down" -> Icons.Default.TrendingDown
        else -> Icons.Default.TrendingFlat
    }
    val trendColor = when (student.trend) {
        "up" -> BrandGreen
        "down" -> BrandCoral
        else -> secondaryText
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Row(
            modifier = Modifier.padding(Brand.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(rankColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "#$rank",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = rankColor,
                )
            }

            Spacer(Modifier.width(Brand.Spacing.sm))

            // Avatar
            val avatarUrl = student.student?.avatarUrl?.let { url ->
                if (url.startsWith("http")) url else "https://unlocklingua.com$url"
            }
            if (avatarUrl != null) {
                coil3.compose.AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier.size(30.dp).background(BrandBlue.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        student.displayName.take(1),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                    )
                }
            }

            Spacer(Modifier.width(Brand.Spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        student.displayName.ifBlank { "?" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (student.trend != null) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            trendIcon,
                            contentDescription = student.trend,
                            modifier = Modifier.size(16.dp),
                            tint = trendColor,
                        )
                    }
                }
                Text(
                    "Выполнено: ${student.completedHomeworks} • Ср. оценка: ${student.averageGrade?.let { String.format("%.1f", it) } ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrandGold.copy(alpha = 0.12f),
            ) {
                Text(
                    "${student.ratingPoints}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrandGold,
                )
            }
        }
    }
}

private fun formatDueDate(raw: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val output = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
        val date = input.parse(raw.take(19))
        if (date != null) output.format(date) else raw.take(16)
    } catch (_: Exception) {
        try {
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val output = SimpleDateFormat("dd.MM.yyyy", Locale.US)
            val date = input.parse(raw.take(10))
            if (date != null) output.format(date) else raw.take(10)
        } catch (_: Exception) { raw.take(16) }
    }
}

// ═══════════════════════════════════════════════════════════════
// Group Detail (reuses existing screens but with own ViewModel)
// ═══════════════════════════════════════════════════════════════

@Composable
fun TeacherGroupsDetailScreen(viewModel: TeacherGroupsViewModel, isDark: Boolean) {
    val uiState by viewModel.uiState.collectAsState()
    val group = uiState.selectedGroup ?: return

    // Homework creation overlay takes priority
    if (uiState.showNewHomework) {
        TeacherGroupsNewHomeworkScreen(viewModel = viewModel, isDark = isDark)
        return
    }

    if (uiState.showAttendance && uiState.selectedStudent != null) {
        TeacherGroupsAttendanceScreen(viewModel = viewModel, isDark = isDark)
        return
    }

    if (uiState.showProgress && uiState.selectedStudent != null) {
        TeacherGroupsProgressScreen(viewModel = viewModel, isDark = isDark)
        return
    }

    if (uiState.showPerformanceEvent && uiState.selectedStudent != null) {
        TeacherGroupsPerformanceEventScreen(viewModel = viewModel, isDark = isDark)
        return
    }

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val (timeStart, timeEnd) = remember(group.scheduleTime) { TeacherHomeViewModel.parseTimeRange(group.scheduleTime) }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Surface(onClick = { viewModel.closeGroupDetail() }, shape = Brand.Shapes.full, color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)) {
                    Text("Закрыть", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = primaryText)
                }
            }
            LazyColumn(contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.sm), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                item {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = cardColor, border = BorderStroke(1.dp, strokeColor)) {
                        Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(48.dp).background(Brush.linearGradient(hskLevelGradient(group.hskLevel)), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("HSK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("${group.hskLevel}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Spacer(Modifier.width(Brand.Spacing.md))
                                Column {
                                    Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                                    Text("Уровень HSK ${group.hskLevel}", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                                }
                            }
                            Spacer(Modifier.height(Brand.Spacing.lg))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                                DetailInfoChip(Icons.Default.Schedule, "$timeStart - $timeEnd", isDark, Modifier.weight(1f))
                                DetailInfoChip(Icons.Default.CalendarToday, group.scheduleDays ?: "", isDark, Modifier.weight(1f))
                                DetailInfoChip(Icons.Default.MeetingRoom, "Каб. ${group.classroom ?: "?"}", isDark, Modifier.weight(1f))
                                DetailInfoChip(Icons.Default.Groups, "${group.studentsCount}", isDark, Modifier.weight(1f))
                            }
                        }
                    }
                }
                // Homework section
                item {
                    Surface(onClick = { viewModel.openNewHomework(group.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = cardColor, border = BorderStroke(1.dp, strokeColor)) {
                        Row(modifier = Modifier.padding(Brand.Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(BrandIndigo.copy(alpha = 0.12f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = BrandIndigo, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(Brand.Spacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Задания группы", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = primaryText)
                                Text("Создать новое домашнее задание", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                            }
                            Box(modifier = Modifier.size(32.dp).background(BrandBlue.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                item { Text("Учащиеся группы    ${uiState.groupStudents.size}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryText) }
                if (uiState.isLoadingStudents) {
                    item { Box(modifier = Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xl), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) } }
                } else {
                    items(uiState.groupStudents, key = { it.id }) { student ->
                        GroupStudentCard(student = student, scheduleDays = group.scheduleDays, isDark = isDark, onProgressClick = { viewModel.openProgress(student) }, onAttendanceClick = { viewModel.openAttendance(student) }, onPerformanceClick = { viewModel.openPerformanceEvent(student) })
                    }
                }
                item { Spacer(Modifier.height(Brand.Spacing.xl)) }
            }
        }
    }
}

@Composable
private fun DetailInfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, isDark: Boolean, modifier: Modifier = Modifier) {
    val bgColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
    val textColor = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = bgColor) {
        Column(modifier = Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = textColor)
            Spacer(Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GroupStudentCard(student: com.subnetik.unlock.data.remote.dto.admin.AdminStudent, scheduleDays: String?, isDark: Boolean, onProgressClick: () -> Unit, onAttendanceClick: () -> Unit, onPerformanceClick: () -> Unit) {
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
    val isLessonDay = com.subnetik.unlock.data.remote.dto.admin.AdminStudent.isTodayLessonDay(scheduleDays)

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = cardColor, border = BorderStroke(1.dp, strokeColor)) {
        Row(modifier = Modifier.padding(Brand.Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            val avatarUrl = student.avatarUrl?.let { url -> if (url.startsWith("http")) url else "https://unlocklingua.com$url" }
            if (avatarUrl != null) {
                coil3.compose.AsyncImage(model = avatarUrl, contentDescription = student.fullName, modifier = Modifier.size(40.dp).clip(CircleShape))
            } else {
                Box(modifier = Modifier.size(40.dp).background(BrandBlue.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(student.firstName.take(1).uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = BrandBlue)
                }
            }
            Spacer(Modifier.width(Brand.Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(student.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = primaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(attendanceText, style = MaterialTheme.typography.bodySmall, color = secondaryText)
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
            IconButton(onClick = onPerformanceClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Star, contentDescription = "Баллы", tint = BrandGold, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = onProgressClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.BarChart, contentDescription = "Прогресс", tint = BrandTeal, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = onAttendanceClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "Посещаемость", tint = BrandBlue, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Attendance Screen (for Groups tab)
// ═══════════════════════════════════════════════════════════════

@Composable
fun TeacherGroupsAttendanceScreen(viewModel: TeacherGroupsViewModel, isDark: Boolean) {
    val uiState by viewModel.uiState.collectAsState()
    val student = uiState.selectedStudent ?: return
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    val cal = Calendar.getInstance()
    val monthName = SimpleDateFormat("LLLL yyyy", Locale("ru")).format(cal.time)
        .replaceFirstChar { it.uppercase() }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val presentCount = uiState.attendanceMap.values.count { it }
    val totalCount = uiState.lessonDates.size
    val pct = if (totalCount > 0) presentCount * 100 / totalCount else 0

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(student.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                Surface(onClick = { viewModel.closeAttendance() }, shape = Brand.Shapes.full, color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)) {
                    Text("Закрыть", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = primaryText)
                }
            }
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Brand.Spacing.lg)) {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = cardColor, border = BorderStroke(1.dp, strokeColor)) {
                    Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                        Text("Посещаемость", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                        Spacer(Modifier.height(4.dp))
                        Text(monthName, style = MaterialTheme.typography.bodySmall, color = secondaryText)
                        Spacer(Modifier.height(Brand.Spacing.lg))
                        // Date chips in FlowRow-like layout (4 per row)
                        val rows = uiState.lessonDates.chunked(4)
                        rows.forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                                row.forEach { date ->
                                    val isPresent = uiState.attendanceMap[date] == true
                                    val isToday = date == today
                                    val dayPart = date.takeLast(2) + "." + date.substring(5, 7)
                                    Surface(
                                        onClick = { viewModel.toggleAttendance(date) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isPresent) BrandTeal.copy(alpha = 0.15f) else if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f),
                                        border = BorderStroke(1.dp, if (isPresent) BrandTeal.copy(alpha = 0.4f) else if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)),
                                    ) {
                                        Column(modifier = Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(dayPart, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isPresent) BrandTeal else primaryText)
                                            if (isToday) {
                                                Text("Сегодня", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = secondaryText)
                                            }
                                        }
                                    }
                                }
                                // Pad remaining
                                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                            Spacer(Modifier.height(Brand.Spacing.sm))
                        }
                        Spacer(Modifier.height(Brand.Spacing.md))
                        Text("Итого: $presentCount/$totalCount • $pct%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = primaryText)
                        Spacer(Modifier.height(Brand.Spacing.lg))
                        Button(
                            onClick = { viewModel.saveAttendance() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = Brand.Shapes.full,
                            enabled = !uiState.isSavingAttendance,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                        ) {
                            if (uiState.isSavingAttendance) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Сохранить", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Progress Screen (for Groups tab)
// ═══════════════════════════════════════════════════════════════

@Composable
fun TeacherGroupsProgressScreen(viewModel: TeacherGroupsViewModel, isDark: Boolean) {
    val uiState by viewModel.uiState.collectAsState()
    val student = uiState.selectedStudent ?: return
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    var expandedTestLevel by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Прогресс", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                Surface(onClick = { viewModel.closeProgress() }, shape = Brand.Shapes.full, color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)) {
                    Text("Закрыть", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = primaryText)
                }
            }
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Brand.Spacing.lg).verticalScroll(rememberScrollState())) {
                // Student info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarUrl = student.avatarUrl?.let { url -> if (url.startsWith("http")) url else "https://unlocklingua.com$url" }
                    if (avatarUrl != null) {
                        coil3.compose.AsyncImage(model = avatarUrl, contentDescription = student.fullName, modifier = Modifier.size(48.dp).clip(CircleShape))
                    } else {
                        Box(modifier = Modifier.size(48.dp).background(BrandBlue.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(student.firstName.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandBlue)
                        }
                    }
                    Spacer(Modifier.width(Brand.Spacing.md))
                    Column {
                        Text(student.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                        Text("Академический прогресс", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                    }
                }
                Spacer(Modifier.height(Brand.Spacing.xl))

                if (uiState.isLoadingProgress) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else {
                    val progress = uiState.studentProgress
                    val testResults = progress?.tests ?: emptyList()

                    // HSK Tests
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(Brand.Spacing.sm))
                        Text("Тесты HSK", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                    }
                    Spacer(Modifier.height(Brand.Spacing.md))

                    for (level in 1..6) {
                        val testResult = testResults.find { it.levelId == "$level" || it.levelId == "hsk$level" }
                        val prevTest = if (level > 1) testResults.find {
                            it.levelId == "${level - 1}" || it.levelId == "hsk${level - 1}"
                        } else null
                        val isLocked = level > 1 && (prevTest == null || prevTest.bestScore < 8)

                        GroupsTestLevelCard(
                            level = level,
                            testResult = testResult,
                            isLocked = isLocked,
                            isDark = isDark,
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

                    Spacer(Modifier.height(Brand.Spacing.xl))

                    // Vocabulary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp), tint = BrandTeal)
                        Spacer(Modifier.width(Brand.Spacing.sm))
                        Text("Словарь", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                    }
                    Spacer(Modifier.height(Brand.Spacing.md))

                    val vocabItems = progress?.vocabulary ?: emptyList()
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
                            GroupsVocabLevelCard(
                                vocab = vocab,
                                isDark = isDark,
                                cardColor = cardColor,
                                strokeColor = strokeColor,
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                            )
                            Spacer(Modifier.height(Brand.Spacing.sm))
                        }
                    }

                    Spacer(Modifier.height(Brand.Spacing.xxxl))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Groups Test Level Card (with expandable answers)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun GroupsTestLevelCard(
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
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) secondaryText else primaryText,
                    )
                    Text(
                        when {
                            isCompleted -> {
                                val attemptsText = groupsRussianPlural(
                                    testResult!!.attempts,
                                    "попытка", "попытки", "попыток",
                                )
                                "${testResult.bestScore}/${testResult.totalQuestions} (${testResult.bestPercent}%) \u2022 ${testResult.attempts} $attemptsText"
                            }
                            isLocked -> "Пройдите HSK ${level - 1}"
                            else -> "Не начат"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText,
                    )
                }

                if (isLocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = secondaryText.copy(alpha = 0.5f),
                    )
                } else if (testResult != null && testResult.bestScore > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (testResult.bestScore >= 8) BrandTeal else BrandCoral,
                    ) {
                        Text(
                            if (testResult.bestScore >= 8) "Пройден" else "Не сдан",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 10.sp,
                        )
                    }
                }
            }

            // Expandable answers section
            if (testResult != null && !testResult.bestAttemptDetails.isNullOrEmpty() && !isLocked) {
                Spacer(Modifier.height(8.dp))

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
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = BrandBlue,
                    )
                    Text(
                        if (isExpanded) "Скрыть ответы" else "Показать ответы",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                        fontSize = 11.sp,
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(BrandTeal, CircleShape),
                                )
                                Text(
                                    "Верно: $correctCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTeal,
                                    fontSize = 11.sp,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(BrandCoral, CircleShape),
                                )
                                Text(
                                    "Ошибки: $wrongCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandCoral,
                                    fontSize = 11.sp,
                                )
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
                                GroupsQuestionDetailRow(
                                    index = idx + 1,
                                    detail = detail,
                                    isDark = isDark,
                                    primaryText = primaryText,
                                )
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

@Composable
private fun GroupsQuestionDetailRow(
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
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(accentColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("$index", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }

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
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = BrandTeal)
                    Text(detail.correctAnswer ?: "", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandTeal, fontSize = 11.sp)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp), tint = BrandCoral)
                    Text(
                        detail.selectedAnswer ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                        color = BrandCoral,
                        fontSize = 11.sp,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = BrandTeal)
                    Text(detail.correctAnswer ?: "", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandTeal, fontSize = 11.sp)
                }
            }
        }

        Icon(
            if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = accentColor,
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Groups Vocab Level Card (with detailed breakdown)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun GroupsVocabLevelCard(
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                GroupsVocabStatPill(
                    label = "Выучено",
                    count = vocab.knownCount,
                    color = BrandTeal,
                    secondaryText = secondaryText,
                    modifier = Modifier.weight(1f),
                )
                GroupsVocabStatPill(
                    label = "Повторить",
                    count = vocab.reviewCount,
                    color = BrandCoral,
                    secondaryText = secondaryText,
                    modifier = Modifier.weight(1f),
                )
                GroupsVocabStatPill(
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
private fun GroupsVocabStatPill(
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

private fun groupsRussianPlural(count: Int, one: String, few: String, many: String): String {
    val mod100 = count % 100
    val mod10 = count % 10
    return when {
        mod100 in 11..19 -> many
        mod10 == 1 -> one
        mod10 in 2..4 -> few
        else -> many
    }
}

// ═══════════════════════════════════════════════════════════════
// New Homework Screen
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherGroupsNewHomeworkScreen(viewModel: TeacherGroupsViewModel, isDark: Boolean) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val fieldBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(onClick = { viewModel.closeNewHomework() }, shape = Brand.Shapes.full, color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)) {
                    Text("Отмена", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = primaryText)
                }
                Text("Новое задание", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                Surface(
                    onClick = { viewModel.createHomework() },
                    shape = Brand.Shapes.full,
                    color = if (uiState.homeworkTitle.isNotBlank() && !uiState.isCreatingHomework)
                        BrandBlue.copy(alpha = 0.12f) else Color.Transparent,
                    enabled = uiState.homeworkTitle.isNotBlank() && !uiState.isCreatingHomework,
                ) {
                    Text(
                        "Создать",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (uiState.homeworkTitle.isNotBlank()) BrandBlue else secondaryText,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Brand.Spacing.lg).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.lg),
            ) {
                // Group selector chips
                Column {
                    Text("Группа", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = primaryText)
                    Spacer(Modifier.height(Brand.Spacing.sm))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.groups, key = { it.id }) { group ->
                            val isSelected = uiState.homeworkGroupId == group.id
                            Surface(
                                onClick = { viewModel.updateHomeworkGroupId(group.id) },
                                shape = Brand.Shapes.full,
                                color = if (isSelected) BrandBlue else if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f),
                            ) {
                                Text(
                                    group.name, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else primaryText.copy(alpha = 0.7f),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }

                // Title field
                Column {
                    Text("Название задания *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = primaryText)
                    Spacer(Modifier.height(Brand.Spacing.sm))
                    OutlinedTextField(
                        value = uiState.homeworkTitle,
                        onValueChange = { viewModel.updateHomeworkTitle(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Например: Урок 5, упражнения 1-3", color = secondaryText) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )
                }

                // Description field
                Column {
                    Text("Описание", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = primaryText)
                    Spacer(Modifier.height(Brand.Spacing.sm))
                    OutlinedTextField(
                        value = uiState.homeworkDescription,
                        onValueChange = { viewModel.updateHomeworkDescription(it) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                    )
                }

                // Deadline toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Установить дедлайн", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = primaryText)
                    Switch(
                        checked = uiState.homeworkHasDeadline,
                        onCheckedChange = { viewModel.toggleHomeworkDeadline(it) },
                    )
                }

                if (uiState.homeworkHasDeadline) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = uiState.homeworkDueDate ?: System.currentTimeMillis(),
                    )

                    LaunchedEffect(datePickerState.selectedDateMillis) {
                        datePickerState.selectedDateMillis?.let { viewModel.updateHomeworkDueDate(it) }
                    }

                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.fillMaxWidth(),
                        title = null,
                        headline = null,
                        showModeToggle = false,
                    )

                    // Time picker row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Время", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = primaryText)
                        var showTimePicker by remember { mutableStateOf(false) }
                        Surface(
                            onClick = { showTimePicker = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                        ) {
                            Text(
                                "%02d:%02d".format(uiState.homeworkDueHour, uiState.homeworkDueMinute),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = primaryText,
                            )
                        }

                        if (showTimePicker) {
                            val timePickerState = rememberTimePickerState(
                                initialHour = uiState.homeworkDueHour,
                                initialMinute = uiState.homeworkDueMinute,
                                is24Hour = true,
                            )
                            AlertDialog(
                                onDismissRequest = { showTimePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.updateHomeworkDueTime(timePickerState.hour, timePickerState.minute)
                                        showTimePicker = false
                                    }) { Text("OK") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showTimePicker = false }) { Text("Отмена") }
                                },
                                text = { TimePicker(state = timePickerState) },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.xl))
            }
        }

        if (uiState.isCreatingHomework) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

