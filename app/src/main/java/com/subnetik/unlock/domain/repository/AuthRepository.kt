package com.subnetik.unlock.domain.repository

import com.subnetik.unlock.data.remote.dto.auth.AuthLoginResponse
import com.subnetik.unlock.data.remote.dto.auth.ReferralInfoResponse
import com.subnetik.unlock.data.remote.dto.auth.TokenResponse
import com.subnetik.unlock.data.remote.dto.auth.UserProfile
import com.subnetik.unlock.domain.model.AppUserRole
import com.subnetik.unlock.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(email: String, password: String, displayName: String, referralCode: String?): Resource<TokenResponse>
    suspend fun login(email: String, password: String): Resource<AuthLoginResponse>
    suspend fun verifyCode(email: String, code: String): Resource<TokenResponse>
    suspend fun getProfile(): Resource<UserProfile>
    suspend fun updateProfile(displayName: String?, email: String?, currentPassword: String?, newPassword: String?): Resource<UserProfile>
    suspend fun logout(): Resource<Unit>
    suspend fun getReferralInfo(): Resource<ReferralInfoResponse>
    fun isLoggedIn(): Flow<Boolean>
    fun getUserRole(): Flow<AppUserRole>
    fun getUserEmail(): Flow<String?>
    fun getDisplayName(): Flow<String?>
    fun getAvatarUrl(): Flow<String?>
    suspend fun uploadAvatar(imageBytes: ByteArray, fileName: String): Resource<String>
    suspend fun deleteAvatar(): Resource<Unit>
}
