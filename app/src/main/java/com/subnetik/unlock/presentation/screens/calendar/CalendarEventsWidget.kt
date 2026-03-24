package com.subnetik.unlock.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subnetik.unlock.data.remote.dto.calendar.CalendarEventResponse
import com.subnetik.unlock.presentation.theme.*

/**
 * Widget showing calendar events/announcements on home screen.
 * Used by admin, teacher, and student home screens.
 */
@Composable
fun CalendarEventsWidget(
    events: List<CalendarEventResponse>,
    isDark: Boolean = true,
    modifier: Modifier = Modifier,
) {
    if (events.isEmpty()) return

    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val primaryText = if (isDark) Color.White else Color.Black
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(18.dp))
                Text("Объявления", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryText)
            }
            Surface(
                shape = CircleShape,
                color = BrandCoral,
            ) {
                Text(
                    "${events.size}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }

        // Event cards
        events.forEach { event ->
            CalendarEventCard(event = event, isDark = isDark)
        }
    }
}

@Composable
fun CalendarEventCard(
    event: CalendarEventResponse,
    isDark: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val eventColor = when (event.color) {
        "red" -> BrandCoral
        "blue" -> BrandBlue
        "yellow" -> BrandGold
        "green" -> BrandGreen
        "orange" -> Color(0xFFFF9800)
        else -> BrandBlue
    }
    val typeIcon = when (event.eventType) {
        "holiday" -> Icons.Default.WbSunny
        "event" -> Icons.Default.Event
        "note" -> Icons.Default.StickyNote2
        else -> Icons.Default.CalendarMonth
    }
    val typeLabel = when (event.eventType) {
        "holiday" -> "Выходной"
        "event" -> "Событие"
        "note" -> "Заметка"
        else -> "Событие"
    }
    val cardBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val primaryText = if (isDark) Color.White else Color.Black
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)

    // Format date: "2026-03-21" -> "21 марта"
    val formattedDate = formatEventDate(event.date, event.endDate)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, eventColor.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Color bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(eventColor),
            )

            // Icon
            Icon(typeIcon, contentDescription = null, tint = eventColor, modifier = Modifier.size(22.dp))

            // Content
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(event.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = primaryText)
                if (!event.description.isNullOrBlank()) {
                    Text(event.description, fontSize = 12.sp, color = secondaryText, maxLines = 2)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(formattedDate, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = eventColor)
                    if (event.cancelsLessons) {
                        Surface(shape = CircleShape, color = BrandCoral) {
                            Text(
                                "Занятия отменены",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                            )
                        }
                    }
                }
            }

            // Type badge
            Surface(shape = CircleShape, color = eventColor.copy(alpha = 0.15f)) {
                Text(
                    typeLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    color = eventColor,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

private fun formatEventDate(date: String, endDate: String?): String {
    val months = arrayOf("", "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря")
    val parts = date.split("-")
    if (parts.size != 3) return date
    val day = parts[2].toIntOrNull() ?: return date
    val month = parts[1].toIntOrNull() ?: return date
    val monthName = if (month in 1..12) months[month] else ""

    if (!endDate.isNullOrBlank() && endDate != date) {
        val endParts = endDate.split("-")
        val endDay = endParts.getOrNull(2)?.toIntOrNull()
        val endMonth = endParts.getOrNull(1)?.toIntOrNull()
        if (endDay != null && endMonth != null) {
            val endMonthName = if (endMonth in 1..12) months[endMonth] else ""
            return if (month == endMonth) "$day–$endDay $monthName"
            else "$day $monthName – $endDay $endMonthName"
        }
    }
    return "$day $monthName"
}
