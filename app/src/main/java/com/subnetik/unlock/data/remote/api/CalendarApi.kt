package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.calendar.CalendarEventCreate
import com.subnetik.unlock.data.remote.dto.calendar.CalendarEventResponse
import com.subnetik.unlock.data.remote.dto.calendar.CalendarEventUpdate
import com.subnetik.unlock.data.remote.dto.common.ApiMessageResponse
import retrofit2.http.*

interface CalendarApi {
    @GET("calendar/events")
    suspend fun getEvents(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("event_type") eventType: String? = null,
    ): List<CalendarEventResponse>

    @POST("calendar/events")
    suspend fun createEvent(@Body request: CalendarEventCreate): CalendarEventResponse

    @PATCH("calendar/events/{id}")
    suspend fun updateEvent(
        @Path("id") id: Int,
        @Body request: CalendarEventUpdate,
    ): CalendarEventResponse

    @DELETE("calendar/events/{id}")
    suspend fun deleteEvent(@Path("id") id: Int): ApiMessageResponse

    @GET("calendar/holidays")
    suspend fun getHolidays(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
    ): List<CalendarEventResponse>
}
