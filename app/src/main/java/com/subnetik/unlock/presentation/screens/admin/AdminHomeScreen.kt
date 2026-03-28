package com.subnetik.unlock.presentation.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import com.subnetik.unlock.data.remote.dto.admin.AdminLead
import com.subnetik.unlock.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminHomeScreen(
    onNavigateToNotifications: () -> Unit,
    viewModel: AdminHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero greeting header
        AdminGreetingHeader(
            displayName = uiState.displayName,
            isDark = isDark,
            unreadCount = uiState.unreadCount,
            onNotifications = onNavigateToNotifications,
        )

        // Widgets
        Column(
            modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Brand.Spacing.lg),
        ) {
            Spacer(Modifier.height(Brand.Spacing.sm))

            // Widget 1: Today Schedule
            TodayScheduleWidget(uiState)

            // Widget 2: School Stats
            SchoolStatsWidget(uiState)

            // Widget 3: Recent Leads
            RecentLeadsWidget(uiState)

            // Widget 4: Classrooms
            ClassroomWidget(uiState)

            // Widget 5: Shi Fu
            ShiFuWidget()

            Spacer(Modifier.height(Brand.Spacing.xl))
        }
    }
}

// ─── Greeting Header ───────────────────────────────────────────

@Composable
private fun AdminGreetingHeader(
    displayName: String?,
    isDark: Boolean,
    unreadCount: Int = 0,
    onNotifications: () -> Unit,
) {
    val firstName = displayName
        ?.trim()
        ?.split(" ")
        ?.firstOrNull()
        ?: "Админ"

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale("ru")) }
    val today = remember { dateFormat.format(Date()) }

    val headerBg = if (isDark) DarkNavy else Color(0xFFF0F2F8)
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val cardBorder = BorderStroke(
        1.dp,
        Brush.linearGradient(
            if (isDark) listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.04f))
            else listOf(Color.Black.copy(alpha = 0.06f), Color.Black.copy(alpha = 0.03f)),
        ),
    )
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
    val watermarkTint = if (isDark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.04f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBg)
            .statusBarsPadding()
            .padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        // Top bar card: logo + UNLOCK + bell
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = Brand.Shapes.extraLarge,
            color = cardColor,
            border = cardBorder,
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
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = onNotifications) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Уведомления",
                            tint = if (unreadCount > 0) BrandCoral else iconTint,
                        )
                    }
                    if (unreadCount > 0) {
                        Badge(
                            containerColor = BrandCoral,
                            modifier = Modifier.offset(x = (-4).dp, y = 4.dp),
                        ) {
                            Text(
                                "${minOf(unreadCount, 99)}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        // Greeting card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = Brand.Shapes.extraLarge,
            color = cardColor,
            border = cardBorder,
        ) {
            Box {
                // Document icon in bottom-right corner
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-12).dp, y = (-8).dp),
                    tint = watermarkTint,
                )

                Column(
                    modifier = Modifier.padding(Brand.Spacing.xl),
                ) {
                    // Role badge + date row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = Brand.Shapes.full,
                            color = BrandIndigo.copy(alpha = 0.25f),
                        ) {
                            Text(
                                text = "Администратор",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandIndigo.copy(alpha = 0.9f),
                            )
                        }
                        Text(
                            text = today,
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText,
                        )
                    }

                    Spacer(Modifier.height(Brand.Spacing.lg))

                    // Greeting with Chinese
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = secondaryText,
                        )
                        Text(
                            text = "Панель управления учебным центром UNLOCK",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryText,
                        )
                    }
                }
            }
        }
    }
}

// ─── Widget 1: Today Schedule ──────────────────────────────────

@Composable
private fun TodayScheduleWidget(uiState: AdminHomeUiState) {
    val todayToken = remember { AdminHomeViewModel.todayToken() }
    val todayTitle = remember { AdminHomeViewModel.todayShortTitle() }

    val todayGroups = remember(uiState.groups) {
        uiState.groups
            .filter { AdminHomeViewModel.matchesSchedule(it.scheduleDays, todayToken) }
            .sortedBy { AdminHomeViewModel.parseStartTime(it.scheduleTime) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = "Сейчас", icon = Icons.Default.Bolt, tint = BrandIndigo)
            Surface(
                shape = Brand.Shapes.full,
                color = BrandIndigo.copy(alpha = 0.12f),
            ) {
                Text(
                    text = todayTitle,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandIndigo,
                )
            }
        }

        if (uiState.isLoading) {
            LoadingPlaceholder()
        } else if (todayGroups.isEmpty()) {
            // No lessons
            StatusCard(
                icon = Icons.Default.NightsStay,
                iconColor = BrandIndigo.copy(alpha = 0.5f),
                title = "Сегодня занятий нет",
                subtitle = "Отдыхаем!",
                bgColor = BrandIndigo.copy(alpha = 0.06f),
            )
        } else {
            // Find current/next lesson
            val now = remember { Calendar.getInstance() }
            val currentOrNext = remember(todayGroups) {
                findCurrentOrNextGroup(todayGroups, now)
            }

            if (currentOrNext != null) {
                NowLessonCard(
                    group = currentOrNext.first,
                    isLive = currentOrNext.second,
                    remainingOrUntilMins = currentOrNext.third,
                )
            } else {
                StatusCard(
                    icon = Icons.Default.CheckCircle,
                    iconColor = BrandGreen,
                    title = "Все занятия завершены",
                    subtitle = "На сегодня всё!",
                    bgColor = BrandGreen.copy(alpha = 0.06f),
                )
            }
        }
    }
}

private fun findCurrentOrNextGroup(
    groups: List<AdminGroup>,
    now: Calendar,
): Triple<AdminGroup, Boolean, Int>? {
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

    // Check live
    for (g in groups) {
        val (start, end) = AdminHomeViewModel.parseTimeRange(g.scheduleTime)
        val startMins = parseMinutes(start) ?: continue
        val endMins = parseMinutes(end) ?: continue
        if (nowMinutes in startMins..endMins) {
            return Triple(g, true, endMins - nowMinutes)
        }
    }

    // Check upcoming
    for (g in groups) {
        val (start, _) = AdminHomeViewModel.parseTimeRange(g.scheduleTime)
        val startMins = parseMinutes(start) ?: continue
        if (startMins > nowMinutes) {
            return Triple(g, false, startMins - nowMinutes)
        }
    }

    return null
}

private fun parseMinutes(time: String): Int? {
    val parts = time.split(":").mapNotNull { it.toIntOrNull() }
    if (parts.size < 2) return null
    return parts[0] * 60 + parts[1]
}

@Composable
private fun NowLessonCard(
    group: AdminGroup,
    isLive: Boolean,
    remainingOrUntilMins: Int,
) {
    val (timeStart, timeEnd) = remember(group.scheduleTime) {
        AdminHomeViewModel.parseTimeRange(group.scheduleTime)
    }
    val accentColor = if (isLive) BrandCoral else BrandBlue
    val hskColor = hskLevelColor(group.hskLevel)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Brand.Shapes.large,
        color = accentColor.copy(alpha = 0.06f),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.25f), accentColor.copy(alpha = 0.25f))),
        ),
    ) {
        Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
            // Status line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                ) {
                    if (isLive) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(BrandCoral, CircleShape),
                        )
                        Text(
                            "ИДЁТ УРОК",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrandCoral,
                        )
                    } else {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = BrandBlue,
                        )
                        Text(
                            "СЛЕДУЮЩИЙ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue,
                        )
                    }
                }

                Text(
                    text = if (isLive) "осталось $remainingOrUntilMins мин"
                    else if (remainingOrUntilMins <= 60) "через $remainingOrUntilMins мин"
                    else "в $timeStart",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor.copy(alpha = 0.8f),
                )
            }

            Spacer(Modifier.height(Brand.Spacing.sm))

            // Group name
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(Brand.Spacing.sm))

            // Details row
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                DetailTag(
                    icon = Icons.Default.Schedule,
                    text = "$timeStart – $timeEnd",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                group.teacher?.takeIf { it.isNotEmpty() }?.let {
                    DetailTag(
                        icon = Icons.Default.Person,
                        text = it,
                        color = BrandTeal,
                    )
                }
                group.classroom?.takeIf { it.isNotEmpty() }?.let {
                    DetailTag(
                        icon = Icons.Default.MeetingRoom,
                        text = it,
                        color = BrandGold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailTag(icon: ImageVector, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = color)
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

// ─── Widget 2: School Stats ────────────────────────────────────

@Composable
private fun SchoolStatsWidget(uiState: AdminHomeUiState) {
    val studentsCount = remember(uiState.users) { uiState.users.count { it.role == "student" } }
    val regularCount = remember(uiState.users) { uiState.users.count { it.role == "user" } }
    val pendingSupport = uiState.pendingSupportCount
    val activePromos = uiState.promocodeStats?.active ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        SectionLabel(text = "Школа в цифрах", icon = Icons.Default.BarChart, tint = BrandBlue)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricPill(
                    value = if (uiState.isLoading) "..." else "$studentsCount",
                    label = "Учащиеся",
                    tint = BrandBlue,
                    modifier = Modifier.weight(1f),
                )
                MetricPill(
                    value = if (uiState.isLoading) "..." else "$regularCount",
                    label = "Пользователи",
                    tint = BrandIndigo,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricPill(
                    value = if (uiState.isLoading) "..." else "$pendingSupport",
                    label = "Ожидают сапорта",
                    tint = if (pendingSupport > 0) BrandCoral else BrandGreen,
                    modifier = Modifier.weight(1f),
                )
                MetricPill(
                    value = if (uiState.isLoading) "..." else "$activePromos",
                    label = "Промокоды",
                    tint = BrandGold,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = tint.copy(alpha = 0.1f),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(listOf(tint.copy(alpha = 0.18f), tint.copy(alpha = 0.18f))),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = tint,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─── Widget 3: Recent Leads ────────────────────────────────────

@Composable
private fun RecentLeadsWidget(uiState: AdminHomeUiState) {
    val pendingLeads = remember(uiState.leads) {
        uiState.leads.filter { it.status == "pending" }.take(3)
    }
    val pendingCount = uiState.leadStats?.pending ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = "Новые заявки", icon = Icons.Default.Inbox, tint = BrandCoral)
            if (pendingCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(BrandCoral, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$pendingCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        if (uiState.isLoading) {
            repeat(2) { LoadingPlaceholder(height = 56.dp) }
        } else if (pendingLeads.isEmpty()) {
            StatusCard(
                icon = Icons.Default.CheckCircle,
                iconColor = BrandGreen,
                title = "Все заявки обработаны",
                subtitle = null,
                bgColor = BrandGreen.copy(alpha = 0.06f),
            )
        } else {
            pendingLeads.forEach { lead ->
                LeadCard(lead)
            }

            if (pendingCount > 3) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Brand.Shapes.medium,
                    color = BrandCoral.copy(alpha = 0.08f),
                ) {
                    Text(
                        text = "Смотреть все ($pendingCount) →",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandCoral,
                    )
                }
            }
        }
    }
}

@Composable
private fun LeadCard(lead: AdminLead) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
            ),
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Avatar circle with initial
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(BrandCoral.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = lead.name.take(1).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandCoral,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lead.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    lead.phone?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (lead.source.isNotEmpty()) {
                        Surface(
                            shape = Brand.Shapes.full,
                            color = BrandCoral.copy(alpha = 0.08f),
                        ) {
                            Text(
                                text = lead.source,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = BrandCoral.copy(alpha = 0.8f),
                            )
                        }
                    }
                }
            }

            // Time ago
            lead.createdAt?.let { dateStr ->
                Text(
                    text = timeAgo(dateStr),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─── Widget 4: Classrooms ──────────────────────────────────────

@Composable
private fun ClassroomWidget(uiState: AdminHomeUiState) {
    val todayToken = remember { AdminHomeViewModel.todayToken() }
    val todayGroups = remember(uiState.groups) {
        uiState.groups.filter { AdminHomeViewModel.matchesSchedule(it.scheduleDays, todayToken) }
    }

    if (todayGroups.isEmpty()) return

    val room1 = remember(todayGroups) { todayGroups.filter { it.classroom == "1" } }
    val room2 = remember(todayGroups) { todayGroups.filter { it.classroom == "2" } }

    Column(verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        SectionLabel(text = "Кабинеты сегодня", icon = Icons.Default.MeetingRoom, tint = BrandGold)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ClassroomColumn(number = "1", groups = room1, tint = BrandTeal, modifier = Modifier.weight(1f))
            ClassroomColumn(number = "2", groups = room2, tint = BrandIndigo, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ClassroomColumn(
    number: String,
    groups: List<AdminGroup>,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                ),
            ),
        ),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = tint.copy(alpha = 0.12f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.MeetingRoom,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = tint,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Каб. $number",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = tint,
                    )
                }
            }

            if (groups.isEmpty()) {
                Text(
                    "Свободен",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            } else {
                groups.forEach { group ->
                    ClassroomGroupCell(group)
                }
            }
        }
    }
}

@Composable
private fun ClassroomGroupCell(group: AdminGroup) {
    val (start, end) = remember(group.scheduleTime) {
        AdminHomeViewModel.parseTimeRange(group.scheduleTime)
    }
    val levelColor = hskLevelColor(group.hskLevel)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "$start – $end",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "HSK ${group.hskLevel}",
                style = MaterialTheme.typography.labelSmall,
                color = levelColor,
            )
            group.teacher?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ─── Widget 5: Shi Fu ──────────────────────────────────────────

@Composable
private fun ShiFuWidget() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Brand.Shapes.large,
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(BrandIndigo.copy(alpha = 0.1f), BrandBlue.copy(alpha = 0.06f)),
                    ),
                    Brand.Shapes.large,
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(BrandIndigo.copy(alpha = 0.2f), BrandBlue.copy(alpha = 0.1f)),
                    ),
                    shape = Brand.Shapes.large,
                ),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.snow_leopard_wave),
                    contentDescription = "Ши Фу",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Помощник Ши Фу",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "AI-ассистент для администратора",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = BrandIndigo.copy(alpha = 0.6f),
                )
            }
        }
    }
}

// ─── Shared Components ─────────────────────────────────────────

@Composable
private fun SectionLabel(text: String, icon: ImageVector, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = tint)
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatusCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String?,
    bgColor: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Brand.Shapes.large,
        color = bgColor,
    ) {
        Row(
            modifier = Modifier.padding(Brand.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = iconColor)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingPlaceholder(height: androidx.compose.ui.unit.Dp = 90.dp) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        shape = Brand.Shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    ) {}
}

// ─── Helpers ───────────────────────────────────────────────────

private fun timeAgo(isoString: String): String {
    return try {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
        )
        formats.forEach { it.timeZone = TimeZone.getTimeZone("UTC") }

        val date = formats.firstNotNullOfOrNull { runCatching { it.parse(isoString) }.getOrNull() }
            ?: return ""

        val seconds = (System.currentTimeMillis() - date.time) / 1000
        when {
            seconds < 3600 -> "${maxOf(1, seconds / 60)} мин"
            seconds < 86400 -> "${seconds / 3600} ч"
            else -> "${seconds / 86400} д"
        }
    } catch (_: Exception) {
        ""
    }
}
