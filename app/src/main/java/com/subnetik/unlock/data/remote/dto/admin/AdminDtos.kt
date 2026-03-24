package com.subnetik.unlock.data.remote.dto.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Groups ──────────────────────────────────────────────────

@Serializable
data class AdminGroup(
    val id: Int,
    val name: String,
    @SerialName("hsk_level") val hskLevel: Int = 0,
    val teacher: String? = null,
    val classroom: String? = null,
    @SerialName("schedule_time") val scheduleTime: String? = null,
    @SerialName("schedule_days") val scheduleDays: String? = null,
    @SerialName("students_count") val studentsCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
)

// ─── Leads ───────────────────────────────────────────────────

@Serializable
data class AdminLead(
    val id: Int,
    val name: String,
    val email: String = "",
    val phone: String? = null,
    val message: String? = null,
    @SerialName("language_level") val languageLevel: String? = null,
    @SerialName("preferred_time") val preferredTime: String? = null,
    val format: String? = null,
    val promocode: String? = null,
    @SerialName("promocode_discount_info") val promocodeDiscountInfo: String? = null,
    @SerialName("final_price") val finalPrice: String? = null,
    val source: String = "",
    val status: String = "",
    @SerialName("admin_note") val adminNote: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class AdminLeadStats(
    val total: Int = 0,
    val pending: Int = 0,
    val processed: Int = 0,
    val deleted: Int = 0,
)

@Serializable
data class AdminLeadUpdateRequest(
    val status: String? = null,
    @SerialName("admin_note") val adminNote: String? = null,
)

// ─── Users ───────────────────────────────────────────────────

@Serializable
data class AdminUser(
    val id: Int,
    val email: String,
    @SerialName("display_name") val displayName: String = "",
    val role: String = "",
    @SerialName("avatar_url") val avatar: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_login_at") val lastLoginAt: String? = null,
    @SerialName("teacher_id") val teacherId: Int? = null,
    @SerialName("teacher_name") val teacherName: String? = null,
)

// ─── Promocodes ──────────────────────────────────────────────

@Serializable
data class AdminPromocodeStats(
    val total: Int = 0,
    val active: Int = 0,
    val inactive: Int = 0,
    val deleted: Int = 0,
)

@Serializable
data class AdminPromocode(
    val id: Int,
    val code: String = "",
    @SerialName("discount_percent") val discountPercent: Int? = null,
    @SerialName("discount_amount") val discountAmount: Int? = null,
    @SerialName("usage_limit") val usageLimit: Int? = null,
    @SerialName("usage_count") val usageCount: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    val status: String = "",
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // Universal coupon fields
    @SerialName("coupon_type") val couponType: String? = null,
    @SerialName("token_amount") val tokenAmount: Int? = null,
    @SerialName("free_lessons_count") val freeLessonsCount: Int? = null,
    @SerialName("market_item_code") val marketItemCode: String? = null,
    val contexts: List<String>? = null,
    @SerialName("per_user_limit") val perUserLimit: Int? = null,
    @SerialName("min_purchase") val minPurchase: Int? = null,
    @SerialName("target_role") val targetRole: String? = null,
    @SerialName("target_group_id") val targetGroupId: Int? = null,
    val description: String? = null,
)

@Serializable
data class AdminPromocodeUpdateRequest(
    @SerialName("is_active") val isActive: Boolean? = null,
    val status: String? = null,
    // Universal coupon fields
    @SerialName("coupon_type") val couponType: String? = null,
    @SerialName("discount_percent") val discountPercent: Int? = null,
    @SerialName("discount_amount") val discountAmount: Int? = null,
    @SerialName("token_amount") val tokenAmount: Int? = null,
    @SerialName("free_lessons_count") val freeLessonsCount: Int? = null,
    @SerialName("market_item_code") val marketItemCode: String? = null,
    val contexts: List<String>? = null,
    @SerialName("per_user_limit") val perUserLimit: Int? = null,
    @SerialName("min_purchase") val minPurchase: Int? = null,
    @SerialName("target_role") val targetRole: String? = null,
    val description: String? = null,
    @SerialName("usage_limit") val usageLimit: Int? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
)

@Serializable
data class CouponValidateRequest(
    val code: String,
    val context: String = "wallet",
    @SerialName("purchase_amount") val purchaseAmount: Int? = null,
)

@Serializable
data class CouponValidateResponse(
    val valid: Boolean,
    val code: String = "",
    @SerialName("coupon_type") val couponType: String = "",
    @SerialName("bonus_description") val bonusDescription: String = "",
    @SerialName("discount_percent") val discountPercent: Int? = null,
    @SerialName("discount_amount") val discountAmount: Int? = null,
    @SerialName("token_amount") val tokenAmount: Int? = null,
    @SerialName("free_lessons_count") val freeLessonsCount: Int? = null,
    @SerialName("market_item_code") val marketItemCode: String? = null,
    val message: String? = null,
)

@Serializable
data class CouponRedeemRequest(
    val code: String,
    val context: String = "wallet",
    @SerialName("purchase_amount") val purchaseAmount: Int? = null,
)

@Serializable
data class CouponRedeemResponse(
    val success: Boolean,
    val message: String = "",
    @SerialName("bonus_applied") val bonusApplied: String? = null,
    @SerialName("tokens_added") val tokensAdded: Int? = null,
    @SerialName("new_balance") val newBalance: Int? = null,
)

@Serializable
data class PromocodeRedemptionsWrapper(
    val redemptions: List<PromocodeRedemptionResponse> = emptyList(),
    val total: Int = 0,
)

@Serializable
data class PromocodeRedemptionResponse(
    val id: Int,
    val code: String = "",
    @SerialName("coupon_type") val couponType: String? = null,
    val description: String? = null,
    @SerialName("bonus_description") val bonusDescription: String? = null,
    @SerialName("discount_month") val discountMonth: Int? = null,
    @SerialName("discount_year") val discountYear: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("redeemed_at") val redeemedAt: String? = null,
    @SerialName("is_pending") val isPending: Boolean = false,
    @SerialName("promocode_id") val promocodeId: Int? = null,
) {
    val effectiveDescription: String get() = bonusDescription ?: description ?: ""
    val effectiveDate: String? get() = createdAt ?: redeemedAt
}

// ─── Support ─────────────────────────────────────────────────

@Serializable
data class AdminSupportBooking(
    val id: Int,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("student_name") val studentName: String = "",
    @SerialName("student_login") val studentLogin: String = "",
    @SerialName("support_teacher") val supportTeacher: String = "",
    @SerialName("session_datetime") val sessionDatetime: String = "",
    val comment: String = "",
    val status: String = "",
    @SerialName("admin_note") val adminNote: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class AdminSupportUpdateRequest(
    val status: String? = null,
    @SerialName("admin_note") val adminNote: String? = null,
)

// ─── Reviews ─────────────────────────────────────────────────

@Serializable
data class AdminReview(
    val id: Int,
    val author: String = "",
    val text: String = "",
    val rating: Int = 0,
    @SerialName("is_student") val isStudent: Boolean = false,
    @SerialName("image_url") val imageUrl: String? = null,
    val status: String = "",
    @SerialName("admin_note") val adminNote: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class AdminReviewUpdateRequest(
    val status: String? = null,
    @SerialName("admin_note") val adminNote: String? = null,
)

// ─── Blog ────────────────────────────────────────────────────

@Serializable
data class AdminBlogPost(
    val id: Int,
    val title: String = "",
    val excerpt: String = "",
    val content: String = "",
    val slug: String = "",
    val language: String = "",
    val status: String = "",
    val cover: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class AdminBlogUpdateRequest(
    val status: String? = null,
)

// ─── Tokens ──────────────────────────────────────────────────

@Serializable
data class AdminTokenStudent(
    val id: Int,
    @SerialName("display_name") val displayName: String = "",
    val email: String = "",
    val balance: Int = 0,
    @SerialName("total_earned") val totalEarned: Int = 0,
    @SerialName("total_spent") val totalSpent: Int = 0,
)

@Serializable
data class AdminTokenTransaction(
    val id: Int,
    @SerialName("user_id") val userId: Int = 0,
    @SerialName("user_name") val userName: String = "",
    val amount: Int = 0,
    val type: String = "",
    val reason: String = "",
    val description: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

// ─── Market ──────────────────────────────────────────────────

@Serializable
data class AdminMarketItem(
    val id: Int,
    val code: String = "",
    val name: String = "",
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("token_price") val tokenPrice: Int = 0,
    val category: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)

// ─── Lessons ─────────────────────────────────────────────────

@Serializable
data class AdminLesson(
    val id: Int,
    val title: String = "",
    val description: String? = null,
    @SerialName("group_id") val groupId: Int = 0,
    @SerialName("group_name") val groupName: String = "",
    @SerialName("starts_at") val startsAt: String = "",
    @SerialName("ends_at") val endsAt: String? = null,
    val status: String = "",
    @SerialName("meet_url") val meetUrl: String? = null,
    @SerialName("recording_url") val recordingUrl: String? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String = "",
)

// ─── Homework ────────────────────────────────────────────────

@Serializable
data class AdminHomeworkGroup(
    val id: Int,
    val name: String = "",
    @SerialName("hsk_level") val hskLevel: Int = 0,
    @SerialName("assignments_count") val assignmentsCount: Int = 0,
)

@Serializable
data class AdminHomeworkAssignment(
    val id: Int,
    val title: String = "",
    val description: String? = null,
    @SerialName("group_id") val groupId: Int = 0,
    @SerialName("group_name") val groupName: String = "",
    @SerialName("due_date") val dueDate: String? = null,
    val status: String = "",
    @SerialName("is_completed") val isCompleted: Boolean? = null,
    @SerialName("submissions_count") val submissionsCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class HomeworkUserShort(
    val id: Int,
    @SerialName("displayName") val displayName: String = "",
    val email: String = "",
    val role: String = "",
)

@Serializable
data class HomeworkStudentRatingEntry(
    val rank: Int = 0,
    val student: HomeworkUserShort? = null,
    @SerialName("averageGrade") val averageGrade: Double? = null,
    @SerialName("totalScore") val totalScore: Int = 0,
    @SerialName("gradedAssignments") val gradedAssignments: Int = 0,
)

@Serializable
data class HomeworkStudentGroupOverview(
    val id: Int,
    val name: String = "",
    val teacher: HomeworkUserShort? = null,
    val classmates: List<HomeworkUserShort> = emptyList(),
    val rating: List<HomeworkStudentRatingEntry> = emptyList(),
)

// ─── Receipts ────────────────────────────────────────────────

@Serializable
data class AdminReceipt(
    val id: Int,
    @SerialName("student_id") val studentId: Int = 0,
    @SerialName("student_name") val studentName: String = "",
    @SerialName("group_name") val groupName: String? = null,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("extracted_amount") val extractedAmount: Int? = null,
    @SerialName("final_amount") val finalAmount: Int? = null,
    val status: String = "",
    @SerialName("payment_method") val paymentMethod: String? = null,
    val month: Int = 0,
    val year: Int = 0,
    @SerialName("admin_note") val adminNote: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("reviewed_at") val reviewedAt: String? = null,
    @SerialName("reviewed_by_name") val reviewedByName: String? = null,
)

@Serializable
data class AdminReceiptUpdateRequest(
    val status: String? = null,
    @SerialName("admin_note") val adminNote: String? = null,
    @SerialName("final_amount") val finalAmount: Int? = null,
)

// ─── Students (in groups) ────────────────────────────────────

@Serializable
data class PaymentEntry(
    val month: Int? = null,
    val year: Int? = null,
    val date: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    val amount: Int? = null,
)

@Serializable
data class AttendanceEntry(
    val date: String,
    val status: String, // "present" or "absent"
)

@Serializable
data class AdminStudent(
    val id: Int,
    @SerialName("group_id") val groupId: Int = 0,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    val age: Int? = null,
    val phone: String? = null,
    @SerialName("guardian_first_name") val guardianFirstName: String? = null,
    @SerialName("guardian_last_name") val guardianLastName: String? = null,
    @SerialName("guardian_phone") val guardianPhone: String? = null,
    val payments: List<PaymentEntry>? = null,
    val attendance: List<AttendanceEntry>? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val attendanceCount: Int get() = attendance?.count { it.status == "present" } ?: 0
    val totalLessons: Int get() = attendance?.size ?: 0
    val attendancePercent: Int get() = if (totalLessons > 0) (attendanceCount * 100 / totalLessons) else 0
    val lastPayment: PaymentEntry? get() = payments?.sortedByDescending { (it.year ?: 0) * 100 + (it.month ?: 0) }?.firstOrNull()
    val isPaid: Boolean get() {
        val cal = java.util.Calendar.getInstance()
        val curMonth = cal.get(java.util.Calendar.MONTH) + 1
        val curYear = cal.get(java.util.Calendar.YEAR)
        return payments?.any { it.month == curMonth && it.year == curYear } == true
    }
    val paidAmount: Int? get() {
        val cal = java.util.Calendar.getInstance()
        val curMonth = cal.get(java.util.Calendar.MONTH) + 1
        val curYear = cal.get(java.util.Calendar.YEAR)
        return payments?.find { it.month == curMonth && it.year == curYear }?.amount
    }
}

@Serializable
data class AdminStudentCreateRequest(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String = "",
    val age: Int? = null,
    val phone: String? = null,
    @SerialName("guardian_first_name") val guardianFirstName: String? = null,
    @SerialName("guardian_last_name") val guardianLastName: String? = null,
    @SerialName("guardian_phone") val guardianPhone: String? = null,
    val payments: List<PaymentEntry>? = null,
    val attendance: List<AttendanceEntry>? = null,
)

@Serializable
data class AdminStudentUpdateRequest(
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val age: Int? = null,
    val phone: String? = null,
    @SerialName("guardian_first_name") val guardianFirstName: String? = null,
    @SerialName("guardian_last_name") val guardianLastName: String? = null,
    @SerialName("guardian_phone") val guardianPhone: String? = null,
    val payments: List<PaymentEntry>? = null,
    val attendance: List<AttendanceEntry>? = null,
)

// ─── Create Requests ─────────────────────────────────────────

@Serializable
data class AdminGroupCreateRequest(
    val name: String,
    @SerialName("hsk_level") val hskLevel: Int,
    val teacher: String? = null,
    val classroom: String? = null,
    @SerialName("schedule_time") val scheduleTime: String? = null,
    @SerialName("schedule_days") val scheduleDays: String? = null,
)

@Serializable
data class AdminPromocodeCreateRequest(
    val code: String,
    @SerialName("discount_percent") val discountPercent: Int? = null,
    @SerialName("discount_amount") val discountAmount: Int? = null,
    @SerialName("usage_limit") val usageLimit: Int? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    // Universal coupon fields
    @SerialName("coupon_type") val couponType: String? = null,
    @SerialName("token_amount") val tokenAmount: Int? = null,
    @SerialName("free_lessons_count") val freeLessonsCount: Int? = null,
    @SerialName("market_item_code") val marketItemCode: String? = null,
    val contexts: List<String>? = null,
    @SerialName("per_user_limit") val perUserLimit: Int? = null,
    @SerialName("min_purchase") val minPurchase: Int? = null,
    @SerialName("target_role") val targetRole: String? = null,
    val description: String? = null,
)

@Serializable
data class AdminBlogCreateRequest(
    val title: String,
    val excerpt: String = "",
    val content: String = "",
    val language: String = "ru",
    val status: String = "draft",
    val cover: String? = null,
)

@Serializable
data class AdminTokenAdjustRequest(
    @SerialName("user_id") val userId: Int,
    val amount: Int,
    val reason: String,
)

@Serializable
data class AdminMarketItemCreateRequest(
    val name: String,
    val description: String? = null,
    @SerialName("token_price") val tokenPrice: Int,
    val category: String? = null,
)

@Serializable
data class AdminMarketItemUpdateRequest(
    val name: String? = null,
    @SerialName("token_price") val tokenPrice: Int? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
)

@Serializable
data class AdminLessonCreateRequest(
    val title: String,
    @SerialName("group_id") val groupId: Int,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("meet_url") val meetUrl: String? = null,
)

@Serializable
data class AdminHomeworkCreateRequest(
    val title: String,
    val description: String? = null,
    @SerialName("group_id") val groupId: Int,
    @SerialName("due_date") val dueDate: String? = null,
)

@Serializable
data class AdminNotificationRequest(
    val title: String,
    val body: String,
    val target: String = "all",
    @SerialName("target_id") val targetId: Int? = null,
)

// ─── Upload ─────────────────────────────────────────────────

@Serializable
data class AdminUploadResponse(
    val success: Boolean = false,
    val url: String? = null,
    val filename: String? = null,
    val size: String? = null,
    val message: String? = null,
)

// ─── Notification Dispatch ──────────────────────────────────

@Serializable
data class AdminNotificationDispatchOptions(
    @SerialName("targetTypes") val targetTypes: List<String> = emptyList(),
    val roles: List<String> = emptyList(),
    val users: List<AdminNotificationTargetUser> = emptyList(),
    val groups: List<AdminNotificationTargetGroup> = emptyList(),
    @SerialName("pushConfigured") val pushConfigured: Boolean = false,
)

@Serializable
data class AdminNotificationTargetUser(
    val id: Int,
    @SerialName("displayName") val displayName: String = "",
    val email: String = "",
    val role: String = "",
)

@Serializable
data class AdminNotificationTargetGroup(
    val id: Int,
    val name: String = "",
)

@Serializable
data class AdminNotificationDispatchRequest(
    val title: String,
    val message: String,
    @SerialName("targetType") val targetType: String = "all",
    @SerialName("targetRole") val targetRole: String? = null,
    @SerialName("targetGroupId") val targetGroupId: Int? = null,
    @SerialName("targetUserId") val targetUserId: Int? = null,
    @SerialName("expiresInDays") val expiresInDays: Int? = null,
)

@Serializable
data class AdminNotificationDispatchResponse(
    @SerialName("notificationId") val notificationId: Int = 0,
    @SerialName("recipientsCount") val recipientsCount: Int = 0,
    @SerialName("pushSent") val pushSent: Int = 0,
    @SerialName("pushFailed") val pushFailed: Int = 0,
    @SerialName("pushConfigured") val pushConfigured: Int = 0,
)

@Serializable
data class AdminUserUpdateRequest(
    val role: String? = null,
    @SerialName("display_name") val displayName: String? = null,
)

// ─── Blog Status ────────────────────────────────────────────

@Serializable
data class AdminBlogStatusRequest(
    val status: String,
)

// ─── Receipt Action ─────────────────────────────────────────

@Serializable
data class AdminReceiptActionRequest(
    @SerialName("final_amount") val finalAmount: Int? = null,
    @SerialName("admin_note") val adminNote: String? = null,
)

// ─── Token Transaction Create ───────────────────────────────

@Serializable
data class AdminTokenTransactionCreateRequest(
    @SerialName("student_id") val studentId: Int,
    val amount: Int,
    val category: String,
    val description: String? = null,
)

// ─── Review Stats ───────────────────────────────────────────

@Serializable
data class AdminReviewStats(
    val total: Int = 0,
    val approved: Int = 0,
    val pending: Int = 0,
    val rejected: Int = 0,
    val deleted: Int = 0,
)
