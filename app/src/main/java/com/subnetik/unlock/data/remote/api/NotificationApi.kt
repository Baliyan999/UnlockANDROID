package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.notification.*
import retrofit2.http.*

interface NotificationApi {
    @GET("notifications/my")
    suspend fun getNotifications(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): List<InboxNotification>

    @GET("notifications/my/unread-count")
    suspend fun getUnreadCount(): UnreadCountResponse

    @POST("notifications/my/{id}/read")
    suspend fun markRead(@Path("id") id: Int)

    @POST("notifications/my/read-all")
    suspend fun markAllRead()

    @POST("notifications/device-tokens")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest)

    @HTTP(method = "DELETE", path = "notifications/device-tokens", hasBody = true)
    suspend fun unregisterDeviceToken(@Body request: DeviceTokenRequest)
}
