package com.subnetik.unlock.data.repository

import com.subnetik.unlock.data.remote.api.NotificationApi
import com.subnetik.unlock.data.remote.dto.notification.DeviceTokenRequest
import com.subnetik.unlock.data.remote.dto.notification.InboxNotification
import com.subnetik.unlock.domain.model.Resource
import com.subnetik.unlock.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi,
) : NotificationRepository {

    override suspend fun getNotifications(limit: Int, offset: Int): Resource<List<InboxNotification>> {
        return try {
            val response = notificationApi.getNotifications(limit, offset)
            Resource.Success(response.items)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load notifications")
        }
    }

    override suspend fun getUnreadCount(): Resource<Int> {
        return try {
            val result = notificationApi.getUnreadCount()
            Resource.Success(result.unreadCount)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get unread count")
        }
    }

    override suspend fun markRead(id: Int): Resource<Unit> {
        return try {
            notificationApi.markRead(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark as read")
        }
    }

    override suspend fun markAllRead(): Resource<Unit> {
        return try {
            notificationApi.markAllRead()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark all as read")
        }
    }

    override suspend fun registerDeviceToken(token: String) {
        try {
            notificationApi.registerDeviceToken(DeviceTokenRequest(token))
        } catch (_: Exception) { }
    }

    override suspend fun unregisterDeviceToken(token: String) {
        try {
            notificationApi.unregisterDeviceToken(DeviceTokenRequest(token))
        } catch (_: Exception) { }
    }
}
