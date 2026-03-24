package com.subnetik.unlock.data.remote.dto.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentPaymentInfoResponse(
    @SerialName("student_id") val studentId: Int = 0,
    @SerialName("student_name") val studentName: String = "",
    @SerialName("group_name") val groupName: String = "",
    @SerialName("hsk_level") val hskLevel: Int = 1,
    @SerialName("monthly_price") val monthlyPrice: Int = 0,
    @SerialName("lessons_per_month") val lessonsPerMonth: Int = 12,
    @SerialName("price_per_lesson") val pricePerLesson: Int = 0,
    @SerialName("schedule_days") val scheduleDays: String? = null,
    @SerialName("schedule_time") val scheduleTime: String? = null,
    @SerialName("current_month_paid") val currentMonthPaid: Boolean = false,
    @SerialName("current_month_amount") val currentMonthAmount: Int? = null,
    @SerialName("expected_price") val expectedPrice: Int = 0,
    @SerialName("total_paid_current_month") val totalPaidCurrentMonth: Int = 0,
    @SerialName("remaining_balance") val remainingBalance: Int = 0,
    @SerialName("is_first_month") val isFirstMonth: Boolean = false,
    @SerialName("lessons_this_month") val lessonsThisMonth: Int = 12,
    val payments: List<PaymentEntryDto> = emptyList(),
    @SerialName("pending_receipts") val pendingReceipts: List<ReceiptDto> = emptyList(),
    @SerialName("original_price") val originalPrice: Int? = null,
    @SerialName("discount_percent") val discountPercent: Int? = null,
    @SerialName("discount_amount") val discountAmount: Int? = null,
    @SerialName("discount_descriptions") val discountDescriptions: List<String>? = null,
    @SerialName("upcoming_discount_descriptions") val upcomingDiscountDescriptions: List<String>? = null,
    @SerialName("teacher_name") val teacherName: String? = null,
)

@Serializable
data class PaymentEntryDto(
    val month: Int? = null,
    val year: Int? = null,
    val date: String? = null,
    @SerialName("paymentMethod") val paymentMethod: String? = null,
    val amount: Int? = null,
)

@Serializable
data class ReceiptDto(
    val id: Int,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("extracted_amount") val extractedAmount: Int? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    val month: Int = 0,
    val year: Int = 0,
    val status: String = "",
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class ReceiptUploadResponse(
    val id: Int,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("extracted_amount") val extractedAmount: Int? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    val month: Int = 0,
    val year: Int = 0,
    val status: String = "",
    @SerialName("created_at") val createdAt: String = "",
)
