package com.subnetik.unlock.data.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("referral_code") val referralCode: String? = null,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class VerifyCodeRequest(
    val email: String,
    val code: String,
)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
)

@Serializable
data class AuthLoginResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("requires_2fa") val requires2FA: Boolean? = null,
    val email: String? = null,
)

@Serializable
data class UserProfile(
    val id: Int,
    val email: String,
    @SerialName("display_name") val displayName: String? = null,
    val role: String? = null,
    @SerialName("avatar_url") val avatar: String? = null,
    @SerialName("terms_accepted_at") val termsAcceptedAt: String? = null,
    @SerialName("teacher_terms_accepted_at") val teacherTermsAcceptedAt: String? = null,
)

@Serializable
data class UserSelfUpdateRequest(
    @SerialName("display_name") val displayName: String? = null,
    val email: String? = null,
    @SerialName("current_password") val currentPassword: String? = null,
    @SerialName("new_password") val newPassword: String? = null,
)

@Serializable
data class StudentsCountResponse(val count: Int)

@Serializable
data class ReferralInfoResponse(
    @SerialName("referral_code") val referralCode: String = "",
    @SerialName("referral_link") val referralLink: String = "",
    @SerialName("referral_url") val referralUrl: String = "",
    @SerialName("invited_total") val invitedTotal: Int = 0,
    @SerialName("rewarded_total") val rewardedTotal: Int = 0,
    @SerialName("referred_count") val referredCount: Int = 0,
    @SerialName("total_reward") val totalReward: Int = 0,
    @SerialName("reward_for_referrer") val rewardForReferrer: Int = 60,
    @SerialName("reward_for_invited") val rewardForInvited: Int = 40,
) {
    val effectiveLink: String get() = referralLink.ifBlank { referralUrl }
    val effectiveInvited: Int get() = if (invitedTotal > 0) invitedTotal else referredCount
}

@Serializable
data class AvatarResponse(val url: String)
