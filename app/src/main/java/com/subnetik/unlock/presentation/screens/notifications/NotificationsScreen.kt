package com.subnetik.unlock.presentation.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.subnetik.unlock.data.remote.dto.notification.InboxNotification
import com.subnetik.unlock.presentation.components.EmptyState
import com.subnetik.unlock.presentation.theme.Brand
import com.subnetik.unlock.presentation.theme.BrandBlue
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val NotifIconBg = Color(0xFF2A3A5C)
private val NotifCardBgUnread = Color(0xFF1E2D4A)
private val NotifCardBgRead = Color(0xFF162030)
private val NotifBlue = Color(0xFF4A90E2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNotification by remember { mutableStateOf<InboxNotification?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Уведомления", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.notifications.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.Notifications,
                    title = "Нет уведомлений",
                    subtitle = "Здесь будут ваши уведомления",
                    modifier = Modifier.padding(padding),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
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
                                        tint = NotifBlue,
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Прочитать все", color = NotifBlue, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
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

    // Detail sheet
    selectedNotification?.let { notif ->
        ModalBottomSheet(
            onDismissRequest = { selectedNotification = null },
            containerColor = Color(0xFF1A2540),
        ) {
            NotificationDetailContent(
                notification = notif,
                onClose = { selectedNotification = null },
            )
        }
    }
}

@Composable
private fun NotificationItem(
    notification: InboxNotification,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = Brand.Shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) NotifCardBgRead else NotifCardBgUnread,
        ),
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
                    .background(NotifIconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (notification.isRead)
                        Icons.Default.Notifications
                    else
                        Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = NotifBlue,
                )
            }

            Spacer(Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
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
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                    )
                    Text(
                        text = formatNotifDate(notification.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
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
                        .background(NotifBlue)
                )
            }
        }
    }
}

@Composable
private fun NotificationDetailContent(
    notification: InboxNotification,
    onClose: () -> Unit,
) {
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
                Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White.copy(alpha = 0.6f))
            }
        }

        // Bell icon
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(NotifIconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = NotifBlue,
            )
        }

        Spacer(Modifier.height(12.dp))

        // Sender badge
        notification.sender?.displayName?.let { sender ->
            Surface(
                shape = Brand.Shapes.full,
                color = NotifBlue.copy(alpha = 0.2f),
            ) {
                Text(
                    text = sender,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = NotifBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Date
        Text(
            text = formatNotifDate(notification.createdAt),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp,
        )

        Spacer(Modifier.height(20.dp))

        // Content card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = Brand.Shapes.large,
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Divider(color = NotifBlue, thickness = 2.dp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
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
