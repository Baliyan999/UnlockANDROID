package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.admin.HomeworkStudentGroupOverview
import com.subnetik.unlock.data.remote.dto.student.BusySlotsResponse
import com.subnetik.unlock.data.remote.dto.student.HomeworkAssignmentStudent
import com.subnetik.unlock.data.remote.dto.student.HomeworkSubmissionStudent
import com.subnetik.unlock.data.remote.dto.student.StudentScheduleData
import com.subnetik.unlock.data.remote.dto.student.StudentSupportBooking
import com.subnetik.unlock.data.remote.dto.student.StudentTokenWallet
import com.subnetik.unlock.data.remote.dto.student.SupportBookingCreateRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface StudentApi {
    @GET("homework/my/wallet")
    suspend fun getWallet(): StudentTokenWallet

    @GET("homework/my/schedule")
    suspend fun getSchedule(): StudentScheduleData

    @GET("homework/my")
    suspend fun getMyAssignments(): List<HomeworkAssignmentStudent>

    @GET("homework/my/groups")
    suspend fun getMyGroups(): List<HomeworkStudentGroupOverview>

    @GET("support/bookings/my")
    suspend fun getMySupportBookings(): List<StudentSupportBooking>

    @Multipart
    @POST("homework/assignments/{assignmentId}/submit")
    suspend fun submitHomework(
        @Path("assignmentId") assignmentId: Int,
        @Part file: MultipartBody.Part,
    ): HomeworkSubmissionStudent

    @POST("support/bookings")
    suspend fun createSupportBooking(@Body request: SupportBookingCreateRequest): StudentSupportBooking

    @GET("support/bookings/busy-slots")
    suspend fun getBusySlots(
        @Query("teacher") teacher: String,
        @Query("date") date: String,
    ): BusySlotsResponse
}
