package com.subnetik.unlock.data.remote.dto.calendar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalendarEventCreate(
    val date: String,
    @SerialName("end_date") val endDate: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("event_type") val eventType: String = "event",
    val color: String? = "blue",
    val visibility: String = "all",
    @SerialName("target_group_id") val targetGroupId: Int? = null,
    @SerialName("target_user_id") val targetUserId: Int? = null,
    @SerialName("cancels_lessons") val cancelsLessons: Boolean = false,
    @SerialName("no_compensation") val noCompensation: Boolean = true,
)

@Serializable
data class CalendarEventUpdate(
    val date: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("event_type") val eventType: String? = null,
    val color: String? = null,
    val visibility: String? = null,
    @SerialName("target_group_id") val targetGroupId: Int? = null,
    @SerialName("target_user_id") val targetUserId: Int? = null,
    @SerialName("cancels_lessons") val cancelsLessons: Boolean? = null,
    @SerialName("no_compensation") val noCompensation: Boolean? = null,
)

@Serializable
data class CalendarEventResponse(
    val id: Int,
    val date: String,
    @SerialName("end_date") val endDate: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("event_type") val eventType: String = "event",
    val color: String? = "blue",
    val visibility: String = "all",
    @SerialName("target_group_id") val targetGroupId: Int? = null,
    @SerialName("target_group_name") val targetGroupName: String? = null,
    @SerialName("target_user_id") val targetUserId: Int? = null,
    @SerialName("target_user_name") val targetUserName: String? = null,
    @SerialName("cancels_lessons") val cancelsLessons: Boolean = false,
    @SerialName("no_compensation") val noCompensation: Boolean = true,
    @SerialName("created_by_id") val createdById: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
