package com.subnetik.unlock.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val LANGUAGE_CODE = stringPreferencesKey("app_language_code")
        private val IS_DARK_THEME = booleanPreferencesKey("app_theme_is_dark")
        private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    val languageCode: Flow<String> = context.settingsDataStore.data.map { it[LANGUAGE_CODE] ?: "ru" }
    val isDarkTheme: Flow<Boolean?> = context.settingsDataStore.data.map { it[IS_DARK_THEME] ?: true }
    val hasSeenOnboarding: Flow<Boolean> = context.settingsDataStore.data.map { it[HAS_SEEN_ONBOARDING] ?: false }

    suspend fun setLanguage(code: String) {
        context.settingsDataStore.edit { it[LANGUAGE_CODE] = code }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.settingsDataStore.edit { it[IS_DARK_THEME] = isDark }
    }

    suspend fun setOnboardingSeen() {
        context.settingsDataStore.edit { it[HAS_SEEN_ONBOARDING] = true }
    }
}
