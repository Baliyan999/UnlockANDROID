package com.subnetik.unlock.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encrypted JSON cache backed by EncryptedSharedPreferences.
 * Saves API responses so the app can show stale data when offline,
 * while keeping cached content encrypted at rest.
 */
object OfflineCache {
    private const val PREFS_NAME = "offline_cache_encrypted"

    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun save(context: Context, key: String, json: String) {
        prefs(context).edit().putString(key, json).apply()
    }

    fun load(context: Context, key: String): String? =
        prefs(context).getString(key, null)

    fun remove(context: Context, key: String) {
        prefs(context).edit().remove(key).apply()
    }

    // Well-known cache keys
    object Key {
        const val BLOG_POSTS = "blog_posts"
    }
}
