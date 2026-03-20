package com.subnetik.unlock.data.remote.dto.notification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeworkUserShort(
    val id: Int,
    @SerialName("display_name") val displayName: String? = null,
    val email: String? = null,
    val avatar: String? = null,
)

@Serializable
data class InboxNotification(
    val id: Int,
    val recipient: HomeworkUserShort? = null,
    val title: String,
    val message: String,
    val type: String? = null,
    @SerialName("action_url") val actionUrl: String? = null,
    val data: String? = null,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class UnreadCountResponse(val count: Int)

@Serializable
data class DeviceTokenRequest(
    val token: String,
    val platform: String = "android",
)
