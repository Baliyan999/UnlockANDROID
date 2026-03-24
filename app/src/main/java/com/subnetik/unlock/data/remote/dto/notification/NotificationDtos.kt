package com.subnetik.unlock.data.remote.dto.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationSender(
    val id: Int,
    @SerialName("display_name") val displayName: String? = null,
    val email: String? = null,
    val role: String? = null,
)

@Serializable
data class InboxNotification(
    val id: Int,
    @SerialName("notification_id") val notificationId: Int? = null,
    val title: String,
    val message: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("read_at") val readAt: String? = null,
    val sender: NotificationSender? = null,
)

@Serializable
data class NotificationsListResponse(
    @SerialName("unread_count") val unreadCount: Int = 0,
    val items: List<InboxNotification> = emptyList(),
)

@Serializable
data class UnreadCountResponse(
    @SerialName("unread_count") val unreadCount: Int = 0,
)

@Serializable
data class DeviceTokenRequest(
    val token: String,
    val platform: String = "android",
)
