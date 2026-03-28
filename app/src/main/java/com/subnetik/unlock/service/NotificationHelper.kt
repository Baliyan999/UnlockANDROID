package com.subnetik.unlock.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.subnetik.unlock.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the app icon badge count by posting/cancelling a silent summary notification.
 *
 * On Android 8+ the launcher shows a badge (dot or number) when there are active
 * notifications in a channel that has [NotificationChannel.canShowBadge] == true.
 * This helper posts a minimal, silent group-summary notification whose sole purpose
 * is to drive that badge count.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Call once at app startup (e.g. from [com.subnetik.unlock.UnlockApp.onCreate])
     * to ensure the badge notification channel exists.
     */
    fun createBadgeChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(BADGE_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    BADGE_CHANNEL_ID,
                    BADGE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN,   // silent, no heads-up
                ).apply {
                    description = "Used to display unread count on the app icon"
                    setShowBadge(true)
                    enableVibration(false)
                    setSound(null, null)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Update the app-icon badge count.
     *
     * @param count  The number of unread items.
     *               If > 0 a silent summary notification is posted so the launcher shows
     *               the badge.  If == 0 the notification is cancelled, removing the badge.
     */
    fun updateBadge(count: Int) {
        if (count > 0) {
            val notification = NotificationCompat.Builder(context, BADGE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setNumber(count)
                .setGroup(BADGE_GROUP_KEY)
                .setGroupSummary(true)
                .setSilent(true)                         // no sound / vibration
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(false)
                .setAutoCancel(false)
                .build()
            notificationManager.notify(BADGE_NOTIFICATION_ID, notification)
        } else {
            notificationManager.cancel(BADGE_NOTIFICATION_ID)
        }
    }

    companion object {
        const val BADGE_CHANNEL_ID = "unlock_badge"
        const val BADGE_CHANNEL_NAME = "Badge Count"
        private const val BADGE_GROUP_KEY = "unlock_inbox"
        private const val BADGE_NOTIFICATION_ID = 9999
    }
}
