package com.subnetik.unlock.presentation.screens.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

@Composable
fun StudentScheduleScreen(
    onBack: () -> Unit,
    viewModel: StudentScheduleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

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
                    "Расписание",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Spacer(Modifier.width(48.dp))
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Brand.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
                ) {
                    Spacer(Modifier.height(Brand.Spacing.sm))

                    // ─── Next Lesson Card (gradient) ──────────────
                    val schedule = uiState.schedule
                    if (schedule != null && schedule.scheduleDays != null) {
                        val nextLessonInfo = remember(schedule) {
                            findNextLesson(schedule.scheduleDays, schedule.scheduleTime)
                        }

                        if (nextLessonInfo != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Transparent,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.linearGradient(listOf(BrandIndigo, BrandBlue)),
                                            RoundedCornerShape(20.dp),
                                        )
                                        .padding(Brand.Spacing.xl),
                                ) {
                                    // Watermark
                                    Text(
                                        "学",
                                        fontSize = 80.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier.align(Alignment.TopEnd),
                                    )

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    if (nextLessonInfo.isOngoing) Icons.Default.GraphicEq else Icons.Default.CalendarMonth,
                                                    contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp),
                                                )
                                            }
                                            Spacer(Modifier.width(Brand.Spacing.md))
                                            Column {
                                                Text(
                                                    if (nextLessonInfo.isOngoing) "Занятие идёт!" else "Следующее занятие",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color.White.copy(alpha = 0.8f),
                                                )
                                                Text(
                                                    if (nextLessonInfo.isOngoing) {
                                                        "Идёт занятие • ${nextLessonInfo.minutesLeft} мин\nдо конца"
                                                    } else {
                                                        "${nextLessonInfo.dayName} в ${nextLessonInfo.time}"
                                                    },
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(Brand.Spacing.lg))

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.7f))
                                            Text(schedule.groupName, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                                        }

                                        Spacer(Modifier.height(Brand.Spacing.sm))

                                        val (startTime, endTime) = parseTimeRange(schedule.scheduleTime)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.7f))
                                            Text(
                                                "$startTime – $endTime  (${schedule.lessonDurationMinutes} мин)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.8f),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ─── Calendar Events ─────────────────────────────
                    run {
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        val futureEvents = uiState.calendarEvents.filter { it.date >= todayStr }
                        if (futureEvents.isNotEmpty()) {
                            com.subnetik.unlock.presentation.screens.calendar.CalendarEventsWidget(
                                events = futureEvents,
                                isDark = isDark,
                            )
                        }
                    }

                    // ─── Week Calendar ─────────────────────────────
                    if (schedule != null && schedule.scheduleDays != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = cardColor,
                            border = BorderStroke(1.dp, strokeColor),
                        ) {
                            Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                                Text("Эта неделя", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryText)
                                Spacer(Modifier.height(Brand.Spacing.md))

                                val weekDays = remember { getWeekDays() }
                                val activeDayTokens = remember(schedule.scheduleDays) {
                                    schedule.scheduleDays.lowercase().split(Regex("[\\s,]+")).map { it.trim() }.toSet()
                                }
                                val (startTime, _) = parseTimeRange(schedule.scheduleTime)
                                val today = Calendar.getInstance()
                                val todayDayOfMonth = today.get(Calendar.DAY_OF_MONTH)
                                val holidayDates = remember(uiState.calendarEvents) {
                                    uiState.calendarEvents
                                        .filter { it.eventType == "holiday" && it.cancelsLessons }
                                        .map { it.date }
                                        .toSet()
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                ) {
                                    weekDays.forEach { day ->
                                        val isActive = day.token in activeDayTokens
                                        val isToday = day.dayOfMonth == todayDayOfMonth
                                        val isPast = day.dayOfMonth < todayDayOfMonth
                                        val isHoliday = day.dateString in holidayDates

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                day.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = when {
                                                    isHoliday -> BrandCoral
                                                    isToday -> primaryText
                                                    isActive -> primaryText
                                                    else -> secondaryText.copy(alpha = 0.5f)
                                                },
                                                fontWeight = if (isToday || isActive || isHoliday) FontWeight.Bold else FontWeight.Normal,
                                            )
                                            Spacer(Modifier.height(6.dp))

                                            // iOS: past+active=green✓, today=dark+gray ring, future+active=blue+blue ring, others=dark gray
                                            val bgColor = when {
                                                isHoliday -> BrandCoral.copy(alpha = 0.15f)
                                                isPast && isActive -> BrandGreen
                                                isToday -> if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
                                                !isPast && isActive -> BrandBlue
                                                else -> if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
                                            }
                                            val borderC = when {
                                                isHoliday -> BrandCoral.copy(alpha = 0.3f)
                                                isToday -> if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f)
                                                !isPast && isActive -> BrandBlue.copy(alpha = 0.5f)
                                                else -> Color.Transparent
                                            }
                                            val textColor = when {
                                                isHoliday -> BrandCoral
                                                isPast && isActive -> Color.White
                                                isToday -> primaryText
                                                !isPast && isActive -> Color.White
                                                else -> secondaryText.copy(alpha = 0.4f)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .then(
                                                        if (borderC != Color.Transparent) Modifier.border(BorderStroke(2.dp, borderC), CircleShape)
                                                        else Modifier
                                                    )
                                                    .background(bgColor, CircleShape),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                if (isHoliday) {
                                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(16.dp))
                                                } else if (isPast && isActive) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                } else {
                                                    Text("${day.dayOfMonth}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                                                }
                                            }

                                            if (isActive && !isPast) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(startTime, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = BrandBlue)
                                            } else if (isPast && isActive) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(startTime, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = BrandGreen)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ─── Today's Lesson / Support Bookings ────────
                    val schedule2 = uiState.schedule
                    val todayToken = todayDayToken()
                    val hasLessonToday = schedule2?.scheduleDays?.lowercase()?.contains(todayToken) == true

                    if (hasLessonToday && schedule2 != null) {
                        val (startTime, endTime) = parseTimeRange(schedule2.scheduleTime)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = cardColor,
                            border = BorderStroke(1.dp, strokeColor),
                        ) {
                            Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Занятие", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryText)
                                    Surface(shape = Brand.Shapes.full, color = BrandBlue.copy(alpha = 0.12f)) {
                                        Text(
                                            "Запланировано",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandBlue,
                                        )
                                    }
                                }
                                Spacer(Modifier.height(Brand.Spacing.md))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = secondaryText)
                                    Text("$startTime – $endTime  ${schedule2.lessonDurationMinutes} мин", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrandBlue)
                                    Text(schedule2.groupName, style = MaterialTheme.typography.bodySmall, color = BrandBlue)
                                }
                            }
                        }
                    } else {
                        // No lesson today
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = cardColor,
                            border = BorderStroke(1.dp, strokeColor),
                        ) {
                            Column(
                                modifier = Modifier.padding(Brand.Spacing.xl),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("☕", fontSize = 36.sp)
                                Spacer(Modifier.height(Brand.Spacing.md))
                                Text("Нет занятий", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryText)
                                Spacer(Modifier.height(4.dp))
                                Text("В этот день можно отдохнуть 🍵", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                            }
                        }
                    }

                    // Support bookings (from schedule response)
                    val bookings = uiState.schedule?.supportBookings ?: emptyList()
                    if (bookings.isNotEmpty()) {
                        Text("Запись к Support", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryText)
                        bookings.forEach { booking ->
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
                                        modifier = Modifier.size(40.dp).background(BrandIndigo.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = BrandIndigo, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(Brand.Spacing.md))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(booking.supportTeacher, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = primaryText)
                                        Text(formatBookingDate(booking.sessionDatetime), style = MaterialTheme.typography.bodySmall, color = secondaryText)
                                    }
                                    val statusColor = when (booking.status.lowercase()) {
                                        "confirmed" -> BrandGreen
                                        "pending" -> BrandGold
                                        "cancelled" -> BrandCoral
                                        else -> secondaryText
                                    }
                                    val statusText = when (booking.status.lowercase()) {
                                        "confirmed" -> "Подтверждён"
                                        "pending" -> "Ожидание"
                                        "cancelled" -> "Отменён"
                                        "completed" -> "Завершён"
                                        else -> booking.status
                                    }
                                    Surface(shape = Brand.Shapes.full, color = statusColor.copy(alpha = 0.12f)) {
                                        Text(statusText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(Brand.Spacing.xl))
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────

private data class NextLessonInfo(val dayName: String, val time: String, val isOngoing: Boolean = false, val minutesLeft: Int = 0)

private data class WeekDay(val label: String, val token: String, val dayOfMonth: Int, val dateString: String = "")

private fun findNextLesson(scheduleDays: String, scheduleTime: String?): NextLessonInfo? {
    val dayMap = mapOf("пн" to Calendar.MONDAY, "вт" to Calendar.TUESDAY, "ср" to Calendar.WEDNESDAY, "чт" to Calendar.THURSDAY, "пт" to Calendar.FRIDAY, "сб" to Calendar.SATURDAY, "вс" to Calendar.SUNDAY)
    val dayNames = mapOf(Calendar.MONDAY to "Понедельник", Calendar.TUESDAY to "Вторник", Calendar.WEDNESDAY to "Среда", Calendar.THURSDAY to "Четверг", Calendar.FRIDAY to "Пятница", Calendar.SATURDAY to "Суббота", Calendar.SUNDAY to "Воскресенье")

    val activeDays = scheduleDays.lowercase().split(Regex("[\\s,]+")).mapNotNull { dayMap[it.trim()] }.toSet()
    if (activeDays.isEmpty()) return null

    val (startTime, _) = parseTimeRange(scheduleTime)
    val now = Calendar.getInstance()
    val nowDow = now.get(Calendar.DAY_OF_WEEK)
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val startMinutes = parseMinutes(startTime)

    val (_, endTimeStr) = parseTimeRange(scheduleTime)
    val endMinutes = parseMinutes(endTimeStr)

    // Check if lesson is ongoing right now
    if (nowDow in activeDays && startMinutes != null && endMinutes != null &&
        nowMinutes >= startMinutes && nowMinutes < endMinutes) {
        val left = endMinutes - nowMinutes
        return NextLessonInfo("Сейчас", startTime, isOngoing = true, minutesLeft = left)
    }

    // Check today (upcoming)
    if (nowDow in activeDays && (startMinutes == null || startMinutes > nowMinutes)) {
        return NextLessonInfo("Сегодня", startTime)
    }

    // Check next 7 days
    for (ahead in 1..7) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, ahead)
        if (cal.get(Calendar.DAY_OF_WEEK) in activeDays) {
            val name = if (ahead == 1) "Завтра" else dayNames[cal.get(Calendar.DAY_OF_WEEK)] ?: ""
            return NextLessonInfo(name, startTime)
        }
    }
    return null
}

private fun getWeekDays(): List<WeekDay> {
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val tokens = listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс")
    val calDays = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)

    val cal = Calendar.getInstance()
    // Go to Monday of this week
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val daysFromMonday = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
    cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

    val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return labels.indices.map { i ->
        val c = cal.clone() as Calendar
        c.add(Calendar.DAY_OF_YEAR, i)
        WeekDay(labels[i], tokens[i], c.get(Calendar.DAY_OF_MONTH), df.format(c.time))
    }
}

private fun todayDayToken(): String {
    val dayTokens = mapOf(Calendar.MONDAY to "пн", Calendar.TUESDAY to "вт", Calendar.WEDNESDAY to "ср", Calendar.THURSDAY to "чт", Calendar.FRIDAY to "пт", Calendar.SATURDAY to "сб", Calendar.SUNDAY to "вс")
    return dayTokens[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)] ?: "пн"
}

private fun parseTimeRange(time: String?): Pair<String, String> {
    if (time == null) return "--:--" to "--:--"
    val parts = time.split("-", "–").map { it.trim() }
    return (parts.firstOrNull() ?: "--:--") to (parts.getOrNull(1) ?: "--:--")
}

private fun parseMinutes(time: String): Int? {
    val parts = time.split(":").mapNotNull { it.toIntOrNull() }
    return if (parts.size >= 2) parts[0] * 60 + parts[1] else null
}

private fun formatBookingDate(raw: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val output = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
        output.format(input.parse(raw.take(19))!!)
    } catch (_: Exception) { raw.take(16) }
}
