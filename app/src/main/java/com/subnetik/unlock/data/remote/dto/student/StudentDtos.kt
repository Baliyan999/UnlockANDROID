package com.subnetik.unlock.data.remote.dto.student

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.subnetik.unlock.data.remote.dto.admin.HomeworkUserShort

@Serializable
data class StudentTokenWallet(
    val balance: Int = 0,
    @SerialName("totalEarned") val totalEarned: Int = 0,
    @SerialName("totalSpent") val totalSpent: Int = 0,
)

@Serializable
data class StudentScheduleData(
    @SerialName("group_id") val groupId: Int = 0,
    @SerialName("group_name") val groupName: String = "",
    val teacher: HomeworkUserShort? = null,
    @SerialName("schedule_days") val scheduleDays: String? = null,
    @SerialName("schedule_time") val scheduleTime: String? = null,
    @SerialName("lesson_duration_minutes") val lessonDurationMinutes: Int = 0,
    @SerialName("meet_url") val meetUrl: String? = null,
    @SerialName("support_bookings") val supportBookings: List<StudentSupportBooking> = emptyList(),
)

@Serializable
data class StudentSupportBooking(
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
