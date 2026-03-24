package com.subnetik.unlock.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
        private val IS_LOGGED_IN = booleanPreferencesKey("auth_is_logged_in")
        private val USER_ROLE = stringPreferencesKey("auth_user_role")
        private val EMAIL = stringPreferencesKey("auth_email")
        private val DISPLAY_NAME = stringPreferencesKey("auth_display_name")
        private val REMEMBER_ME = booleanPreferencesKey("auth_remember_me")
        private val LAST_ACTIVE = longPreferencesKey("auth_last_active")
        private val AVATAR_URL = stringPreferencesKey("profile_avatar")
        private val TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        private val TEACHER_TERMS_ACCEPTED = booleanPreferencesKey("teacher_terms_accepted")
    }

    val accessToken: Flow<String?> = context.authDataStore.data.map { it[ACCESS_TOKEN] }
    val isLoggedIn: Flow<Boolean> = context.authDataStore.data.map { it[IS_LOGGED_IN] ?: false }
    val userRole: Flow<String?> = context.authDataStore.data.map { it[USER_ROLE] }
    val email: Flow<String?> = context.authDataStore.data.map { it[EMAIL] }
    val displayName: Flow<String?> = context.authDataStore.data.map { it[DISPLAY_NAME] }
    val rememberMe: Flow<Boolean> = context.authDataStore.data.map { it[REMEMBER_ME] ?: false }
    val avatarUrl: Flow<String?> = context.authDataStore.data.map { it[AVATAR_URL] }
    val termsAccepted: Flow<Boolean> = context.authDataStore.data.map { it[TERMS_ACCEPTED] ?: false }
    val teacherTermsAccepted: Flow<Boolean> = context.authDataStore.data.map { it[TEACHER_TERMS_ACCEPTED] ?: false }

    suspend fun saveSession(token: String?, role: String?, email: String?, displayName: String?) {
        context.authDataStore.edit { prefs ->
            token?.let { prefs[ACCESS_TOKEN] = it }
            role?.let { prefs[USER_ROLE] = it }
            email?.let { prefs[EMAIL] = it }
            displayName?.let { prefs[DISPLAY_NAME] = it }
            prefs[IS_LOGGED_IN] = true
            prefs[LAST_ACTIVE] = System.currentTimeMillis()
        }
    }

    suspend fun saveAvatar(url: String) {
        context.authDataStore.edit { it[AVATAR_URL] = url }
    }

    suspend fun clearAvatar() {
        context.authDataStore.edit { it.remove(AVATAR_URL) }
    }

    suspend fun setRememberMe(value: Boolean) {
        context.authDataStore.edit { it[REMEMBER_ME] = value }
    }

    suspend fun updateLastActive() {
        context.authDataStore.edit { it[LAST_ACTIVE] = System.currentTimeMillis() }
    }

    suspend fun saveTermsAccepted(accepted: Boolean) {
        context.authDataStore.edit { it[TERMS_ACCEPTED] = accepted }
    }

    suspend fun saveTeacherTermsAccepted(accepted: Boolean) {
        context.authDataStore.edit { it[TEACHER_TERMS_ACCEPTED] = accepted }
    }

    suspend fun clearAll() {
        context.authDataStore.edit { it.clear() }
    }
}
