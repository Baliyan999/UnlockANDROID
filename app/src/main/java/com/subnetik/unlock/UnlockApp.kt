package com.subnetik.unlock

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.subnetik.unlock.service.NotificationHelper
import com.subnetik.unlock.service.UnlockFCMService
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class UnlockApp : Application(), SingletonImageLoader.Factory {

    @Inject lateinit var okHttpClient: OkHttpClient
    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        notificationHelper.createBadgeChannel()
    }

    override fun newImageLoader(context: android.content.Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
            }
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                UnlockFCMService.CHANNEL_ID,
                UnlockFCMService.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Unlock Language Studio notifications"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
