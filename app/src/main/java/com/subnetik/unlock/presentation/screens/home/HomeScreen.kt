package com.subnetik.unlock.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.R
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

@Composable
fun HomeScreen(
    onNavigateToVocabulary: () -> Unit,
    onNavigateToTests: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToSupportBooking: () -> Unit = {},
    onNavigateToPayment: () -> Unit = {},
    onNavigateToMarket: () -> Unit = {},
    onNavigateToReferral: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    val firstName = uiState.displayName?.trim()?.split(" ")?.firstOrNull() ?: "Гость"

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ─── Top Bar ──────────────────────────────────────
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
                            if (uiState.unreadCount > 0) {
                                Badge(containerColor = BrandCoral) {
                                    Text("${uiState.unreadCount}", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Уведомления",
                                tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // ─── Greeting Card ────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Brand.Spacing.lg),
                shape = Brand.Shapes.extraLarge,
                color = cardColor,
                border = BorderStroke(1.dp, strokeColor),
            ) {
                Column(modifier = Modifier.padding(Brand.Spacing.xl)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Role badge
                        Surface(
                            shape = Brand.Shapes.full,
                            color = BrandGreen.copy(alpha = 0.2f),
                        ) {
                            Text(
                                text = "Учащийся",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreen,
                            )
                        }

                        // Token balance (always show)
                        run {
                            Surface(
                                shape = Brand.Shapes.full,
                                color = BrandGold.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.3f)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    val balanceText = if (uiState.tokenBalance >= 1000)
                                        "%.1fk".format(uiState.tokenBalance / 1000f)
                                    else "${uiState.tokenBalance}"
                                    Text(
                                        balanceText,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandGold,
                                    )
                                    Image(
                                        painter = painterResource(R.drawable.unlock_token_logo),
                                        contentDescription = "Токены",
                                        modifier = Modifier.size(24.dp).clip(CircleShape),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(Brand.Spacing.lg))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("你好, ", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryText)
                        Text(firstName, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryText)
                    }

                    Spacer(Modifier.height(Brand.Spacing.sm))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandBlue)
                        Text(
                            "Продолжай учиться – 学无止境",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Brand.Spacing.xl))

            // ─── Calendar Events ──────────────────────────────
            if (uiState.calendarEvents.isNotEmpty()) {
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                val futureEvents = uiState.calendarEvents.filter { it.date >= todayStr }
                if (futureEvents.isNotEmpty()) {
                    com.subnetik.unlock.presentation.screens.calendar.CalendarEventsWidget(
                        events = futureEvents,
                        isDark = isDark,
                        modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                    )
                    Spacer(Modifier.height(Brand.Spacing.xl))
                }
            }

            // ─── Учебный кабинет ──────────────────────────────
            Text(
                "Учебный кабинет",
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryText,
            )

            Spacer(Modifier.height(Brand.Spacing.md))

            Column(
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            ) {
                StudentMenuItem(
                    icon = Icons.Default.CalendarMonth,
                    iconBg = BrandIndigo,
                    title = "Расписание",
                    subtitle = "Просмотр ваших занятий и уроков.",
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = onNavigateToSchedule,
                )

                StudentMenuItem(
                    icon = Icons.Default.Payment,
                    iconBg = BrandGreen,
                    title = "Оплата",
                    subtitle = "История оплат и квитанции.",
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = onNavigateToPayment,
                )

                StudentMenuItem(
                    icon = Icons.Default.Translate,
                    iconBg = BrandBlue,
                    title = "Словарь HSK",
                    subtitle = "Лексика и карточки по уровням HSK 1-6.",
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = onNavigateToVocabulary,
                )

                StudentMenuItem(
                    icon = Icons.Default.ShoppingCart,
                    iconBg = BrandTeal,
                    title = "Unlock Market",
                    subtitle = "Обмен токенов на товары и призы.",
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = onNavigateToMarket,
                )

                StudentMenuItem(
                    icon = Icons.Default.Groups,
                    iconBg = BrandCoral,
                    title = "Записаться к Support-преподавателю",
                    subtitle = "Запись к Support-преподавателям.",
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = onNavigateToSupportBooking,
                )

                StudentMenuItem(
                    icon = Icons.Default.CardGiftcard,
                    iconBg = BrandGold,
                    title = "Пригласить друга",
                    subtitle = "Получай токены за приглашённых друзей.",
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = onNavigateToReferral,
                )
            }

            Spacer(Modifier.height(Brand.Spacing.xxl))
        }
    }
}

@Composable
private fun StudentMenuItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Row(
            modifier = Modifier.padding(Brand.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconBg, modifier = Modifier.size(22.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// ─── Schedule helpers ──────────────────────────────────────

private fun findNextLessonHome(scheduleDays: String, scheduleTime: String?): Pair<String, String>? {
    val dayMap = mapOf("пн" to java.util.Calendar.MONDAY, "вт" to java.util.Calendar.TUESDAY, "ср" to java.util.Calendar.WEDNESDAY, "чт" to java.util.Calendar.THURSDAY, "пт" to java.util.Calendar.FRIDAY, "сб" to java.util.Calendar.SATURDAY, "вс" to java.util.Calendar.SUNDAY)
    val dayNames = mapOf(java.util.Calendar.MONDAY to "Понедельник", java.util.Calendar.TUESDAY to "Вторник", java.util.Calendar.WEDNESDAY to "Среда", java.util.Calendar.THURSDAY to "Четверг", java.util.Calendar.FRIDAY to "Пятница", java.util.Calendar.SATURDAY to "Суббота", java.util.Calendar.SUNDAY to "Воскресенье")

    val activeDays = scheduleDays.lowercase().split(Regex("[\\s,]+")).mapNotNull { dayMap[it.trim()] }.toSet()
    if (activeDays.isEmpty()) return null

    val (startTime, _) = parseTimeRangeHome(scheduleTime)
    val now = java.util.Calendar.getInstance()
    val nowDow = now.get(java.util.Calendar.DAY_OF_WEEK)
    val nowMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
    val startMinutes = startTime.split(":").mapNotNull { it.toIntOrNull() }.let { if (it.size >= 2) it[0] * 60 + it[1] else null }

    if (nowDow in activeDays && (startMinutes == null || startMinutes > nowMinutes)) {
        return "Сегодня" to startTime
    }
    for (ahead in 1..7) {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, ahead)
        if (cal.get(java.util.Calendar.DAY_OF_WEEK) in activeDays) {
            val name = if (ahead == 1) "Завтра" else dayNames[cal.get(java.util.Calendar.DAY_OF_WEEK)] ?: ""
            return name to startTime
        }
    }
    return null
}

private fun parseTimeRangeHome(time: String?): Pair<String, String> {
    if (time == null) return "--:--" to "--:--"
    val parts = time.split("-", "–").map { it.trim() }
    return (parts.firstOrNull() ?: "--:--") to (parts.getOrNull(1) ?: "--:--")
}
