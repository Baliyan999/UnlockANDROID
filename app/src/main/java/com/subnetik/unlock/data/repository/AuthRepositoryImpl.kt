package com.subnetik.unlock.data.repository

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.subnetik.unlock.BuildConfig
import com.subnetik.unlock.data.local.datastore.AuthDataStore
import com.subnetik.unlock.data.remote.api.AuthApi
import com.subnetik.unlock.data.remote.api.NotificationApi
import com.subnetik.unlock.data.remote.dto.auth.*
import com.subnetik.unlock.data.remote.dto.notification.DeviceTokenRequest
import com.subnetik.unlock.domain.model.AppUserRole
import com.subnetik.unlock.domain.model.Resource
import com.subnetik.unlock.domain.repository.AuthRepository
import com.subnetik.unlock.util.ErrorMapper
import com.subnetik.unlock.util.ErrorMapper.ErrorContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val authDataStore: AuthDataStore,
    private val notificationApi: NotificationApi,
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        referralCode: String?
    ): Resource<TokenResponse> {
        return try {
            val response = authApi.register(
                RegisterRequest(email, password, displayName, referralCode)
            )
            authDataStore.saveSession(
                token = response.accessToken,
                role = "user",
                email = email,
                displayName = displayName
            )
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(ErrorMapper.map(e, ErrorContext.REGISTER))
        }
    }

    override suspend fun login(email: String, password: String): Resource<AuthLoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.requires2FA != true && response.accessToken != null) {
                authDataStore.saveSession(
                    token = response.accessToken,
                    role = "user",
                    email = email,
                    displayName = null
                )
                // Fetch profile to get actual role
                try {
                    val profile = authApi.getProfile()
                    authDataStore.saveSession(
                        token = response.accessToken,
                        role = profile.role ?: "user",
                        email = profile.email,
                        displayName = profile.displayName
                    )
                    profile.avatar?.let { authDataStore.saveAvatar(it) }
                    // Sync terms acceptance from server
                    authDataStore.saveTermsAccepted(profile.termsAcceptedAt != null)
                    authDataStore.saveTeacherTermsAccepted(profile.teacherTermsAcceptedAt != null)
                } catch (_: Exception) { }
                // Register FCM token with server
                registerFcmToken()
            }
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(ErrorMapper.map(e, ErrorContext.LOGIN))
        }
    }

    override suspend fun verifyCode(email: String, code: String): Resource<TokenResponse> {
        return try {
            val response = authApi.verifyCode(VerifyCodeRequest(email, code))
            authDataStore.saveSession(
                token = response.accessToken,
                role = "user",
                email = email,
                displayName = null
            )
            try {
                val profile = authApi.getProfile()
                authDataStore.saveSession(
                    token = response.accessToken,
                    role = profile.role ?: "user",
                    email = profile.email,
                    displayName = profile.displayName
                )
                profile.avatar?.let { authDataStore.saveAvatar(it) }
            } catch (_: Exception) { }
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(ErrorMapper.map(e, ErrorContext.LOGIN))
        }
    }

    override suspend fun getProfile(): Resource<UserProfile> {
        return try {
            val profile = authApi.getProfile()
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(ErrorMapper.map(e, ErrorContext.PROFILE))
        }
    }

    override suspend fun updateProfile(
        displayName: String?,
        email: String?,
        currentPassword: String?,
        newPassword: String?
    ): Resource<UserProfile> {
        return try {
            val response = authApi.updateProfile(
                UserSelfUpdateRequest(displayName, email, currentPassword, newPassword)
            )
            displayName?.let { name ->
                authDataStore.saveSession(
                    token = null,
                    role = null,
                    email = email,
                    displayName = name
                )
            }
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(ErrorMapper.map(e, ErrorContext.PROFILE))
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            // Unregister FCM token before logout
            unregisterFcmToken()
            try { authApi.logout() } catch (_: Exception) { }
            authDataStore.clearAll()
            Resource.Success(Unit)
        } catch (e: Exception) {
            authDataStore.clearAll()
            Resource.Success(Unit)
        }
    }

    override suspend fun getReferralInfo(): Resource<ReferralInfoResponse> {
        return try {
            Resource.Success(authApi.getReferralInfo())
        } catch (e: Exception) {
            Resource.Error(ErrorMapper.map(e))
        }
    }

    override fun isLoggedIn(): Flow<Boolean> = authDataStore.isLoggedIn

    override fun getUserRole(): Flow<AppUserRole> = authDataStore.userRole.map { role ->
        AppUserRole.resolve(true, role)
    }

    override fun getUserEmail(): Flow<String?> = authDataStore.email

    override fun getDisplayName(): Flow<String?> = authDataStore.displayName

    override fun getAvatarUrl(): Flow<String?> = authDataStore.avatarUrl

    override suspend fun uploadAvatar(imageBytes: ByteArray, fileName: String): Resource<String> {
        return try {
            val mediaType = when {
                fileName.endsWith(".png", ignoreCase = true) -> "image/png"
                fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
                else -> "image/jpeg"
            }.toMediaType()
            val requestBody = imageBytes.toRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData("avatar", fileName, requestBody)
            val response = authApi.uploadAvatar(part)
            authDataStore.saveAvatar(response.url)
            Resource.Success(response.url)
        } catch (e: Exception) {
            Resource.Error(ErrorMapper.map(e, ErrorContext.PROFILE))
        }
    }

    override suspend fun deleteAvatar(): Resource<Unit> {
        return try {
            authApi.deleteAvatar()
            authDataStore.clearAvatar()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(ErrorMapper.map(e, ErrorContext.PROFILE))
        }
    }

    private suspend fun registerFcmToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            notificationApi.registerDeviceToken(
                DeviceTokenRequest(token = token, platform = "android")
            )
            if (BuildConfig.DEBUG) Log.d("AuthRepo", "FCM token registered with server")
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("AuthRepo", "Failed to register FCM token: ${e.message}")
        }
    }

    private suspend fun unregisterFcmToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            notificationApi.unregisterDeviceToken(
                DeviceTokenRequest(token = token, platform = "android")
            )
            if (BuildConfig.DEBUG) Log.d("AuthRepo", "FCM token unregistered from server")
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("AuthRepo", "Failed to unregister FCM token: ${e.message}")
        }
    }
}
