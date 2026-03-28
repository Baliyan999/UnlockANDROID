package com.subnetik.unlock.presentation.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subnetik.unlock.data.remote.dto.notification.InboxNotification
import com.subnetik.unlock.presentation.components.EmptyState
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.Brand
import com.subnetik.unlock.presentation.theme.BrandBlue
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val themePreference by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
    val isDark = themePreference ?: isSystemInDarkTheme()
    var selectedNotification by remember { mutableStateOf<InboxNotification?>(null) }

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val notifIconBg = if (isDark) Color(0xFF2A3A5C) else BrandBlue.copy(alpha = 0.12f)
    val notifCardBgUnread = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
    val notifCardBgRead = if (isDark) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.7f)
    val notifAccent = BrandBlue
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        "Уведомления",
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = primaryText,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = notifAccent)
                    }
                }
                uiState.notifications.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Notifications,
                        title = "Нет уведомлений",
                        subtitle = "Здесь будут ваши уведомления",
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // "Прочитать все" button
                        if (uiState.notifications.any { !it.isRead }) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    TextButton(onClick = { viewModel.markAllRead() }) {
                                        Icon(
                                            Icons.Default.CheckCircleOutline,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = notifAccent,
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Прочитать все", color = notifAccent, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        items(uiState.notifications, key = { it.id }) { notification ->
                            NotificationItem(
                                notification = notification,
                                isDark = isDark,
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                notifIconBg = notifIconBg,
                                notifCardBgUnread = notifCardBgUnread,
                                notifCardBgRead = notifCardBgRead,
                                notifAccent = notifAccent,
                                strokeColor = strokeColor,
                                onClick = {
                                    selectedNotification = notification
                                    if (!notification.isRead) viewModel.markRead(notification.id)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail sheet
    selectedNotification?.let { notif ->
        val sheetContainerColor = if (isDark) Color(0xFF1A2540) else Color.White
        ModalBottomSheet(
            onDismissRequest = { selectedNotification = null },
            containerColor = sheetContainerColor,
        ) {
            NotificationDetailContent(
                notification = notif,
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
                notifIconBg = notifIconBg,
                notifAccent = notifAccent,
                onClose = { selectedNotification = null },
            )
        }
    }
}

@Composable
private fun NotificationItem(
    notification: InboxNotification,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
    notifIconBg: Color,
    notifCardBgUnread: Color,
    notifCardBgRead: Color,
    notifAccent: Color,
    strokeColor: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = Brand.Shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) notifCardBgRead else notifCardBgUnread,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Bell icon in circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(notifIconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (notification.isRead)
                        Icons.Default.Notifications
                    else
                        Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = notifAccent,
                )
            }

            Spacer(Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = notification.sender?.displayName ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                    )
                    Text(
                        text = formatNotifDate(notification.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                    )
                }
            }

            // Unread dot
            if (!notification.isRead) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(notifAccent)
                )
            }
        }
    }
}

@Composable
private fun NotificationDetailContent(
    notification: InboxNotification,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
    notifIconBg: Color,
    notifAccent: Color,
    onClose: () -> Unit,
) {
    val detailCardColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.04f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = secondaryText)
            }
        }

        // Bell icon
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(notifIconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = notifAccent,
            )
        }

        Spacer(Modifier.height(12.dp))

        // Sender badge
        notification.sender?.displayName?.let { sender ->
            Surface(
                shape = Brand.Shapes.full,
                color = notifAccent.copy(alpha = 0.2f),
            ) {
                Text(
                    text = sender,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = notifAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Date
        Text(
            text = formatNotifDate(notification.createdAt),
            color = secondaryText,
            fontSize = 13.sp,
        )

        Spacer(Modifier.height(20.dp))

        // Content card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = Brand.Shapes.large,
            colors = CardDefaults.cardColors(containerColor = detailCardColor),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Spacer(Modifier.height(4.dp))
                HorizontalDivider(color = notifAccent, thickness = 2.dp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color.White.copy(alpha = 0.8f) else primaryText,
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

private fun formatNotifDate(isoDate: String): String {
    return try {
        val dt = ZonedDateTime.parse(isoDate + (if ("Z" in isoDate || "+" in isoDate) "" else "Z"))
        val months = arrayOf(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )
        val d = dt.toLocalDateTime()
        "${d.dayOfMonth} ${months[d.monthValue - 1]} ${d.year} г. в %02d:%02d".format(d.hour, d.minute)
    } catch (_: Exception) {
        isoDate.take(16).replace("T", " ")
    }
}
