package com.subnetik.unlock.domain.repository

import com.subnetik.unlock.data.remote.dto.notification.InboxNotification
import com.subnetik.unlock.domain.model.Resource

interface NotificationRepository {
    suspend fun getNotifications(limit: Int = 50, offset: Int = 0): Resource<List<InboxNotification>>
    suspend fun getUnreadCount(): Resource<Int>
    suspend fun markRead(id: Int): Resource<Unit>
    suspend fun markAllRead(): Resource<Unit>
    suspend fun registerDeviceToken(token: String)
    suspend fun unregisterDeviceToken(token: String)
}
