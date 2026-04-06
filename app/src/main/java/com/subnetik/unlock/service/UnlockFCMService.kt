package com.subnetik.unlock.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.subnetik.unlock.BuildConfig
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.subnetik.unlock.MainActivity
import com.subnetik.unlock.R
import com.subnetik.unlock.data.local.datastore.AuthDataStore
import com.subnetik.unlock.data.remote.api.NotificationApi
import com.subnetik.unlock.data.remote.dto.notification.DeviceTokenRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UnlockFCMService : FirebaseMessagingService() {

    @Inject lateinit var notificationApi: NotificationApi
    @Inject lateinit var authDataStore: AuthDataStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (BuildConfig.DEBUG) Log.d(TAG, "FCM token refreshed")
        serviceScope.launch {
            registerTokenWithServer(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (BuildConfig.DEBUG) Log.d(TAG, "FCM message received from: ${message.from}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""
        val notificationId = message.data["notification_id"]?.toIntOrNull()
            ?: System.currentTimeMillis().toInt()
        val badge = message.data["badge"]?.toIntOrNull() ?: 0

        showNotification(title, body, notificationId, badge)
    }

    private fun showNotification(title: String, body: String, notificationId: Int, badge: Int) {
        ensureNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_id", notificationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setNumber(badge)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)

        // Update badge count via ShortcutBadger (best-effort, not all launchers support it)
        try {
            if (badge > 0) {
                // Uses the standard Android API for badging where supported
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager.getNotificationChannel(CHANNEL_ID)?.let {
                        it.setShowBadge(true)
                    }
                }
            }
        } catch (_: Exception) { }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Unlock Language Studio notifications"
                    enableVibration(true)
                    setShowBadge(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private suspend fun registerTokenWithServer(token: String) {
        val isLoggedIn = authDataStore.isLoggedIn.firstOrNull() ?: false
        if (!isLoggedIn) {
            if (BuildConfig.DEBUG) Log.d(TAG, "User not logged in, skipping FCM token registration")
            return
        }
        try {
            notificationApi.registerDeviceToken(
                DeviceTokenRequest(token = token, platform = "android")
            )
            if (BuildConfig.DEBUG) Log.d(TAG, "FCM token registered with server")
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Failed to register FCM token: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "UnlockFCM"
        const val CHANNEL_ID = "unlock_notifications"
        const val CHANNEL_NAME = "Unlock Notifications"
    }
}
