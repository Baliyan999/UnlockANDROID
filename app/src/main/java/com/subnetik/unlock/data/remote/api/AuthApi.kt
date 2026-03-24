package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.auth.*
import com.subnetik.unlock.data.remote.dto.common.ApiMessageResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): TokenResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthLoginResponse

    @POST("auth/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): TokenResponse

    @GET("auth/me")
    suspend fun getProfile(): UserProfile

    @PATCH("auth/me")
    suspend fun updateProfile(@Body request: UserSelfUpdateRequest): UserProfile

    @POST("auth/logout")
    suspend fun logout(): ApiMessageResponse

    @GET("auth/me/students-count")
    suspend fun getStudentsCount(): StudentsCountResponse

    @GET("auth/me/referral")
    suspend fun getReferralInfo(): ReferralInfoResponse

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): AvatarResponse

    @DELETE("users/me/avatar")
    suspend fun deleteAvatar()

    @POST("auth/me/accept-terms")
    suspend fun acceptTerms(): ApiMessageResponse

    @POST("auth/me/accept-teacher-terms")
    suspend fun acceptTeacherTerms(): ApiMessageResponse
}
