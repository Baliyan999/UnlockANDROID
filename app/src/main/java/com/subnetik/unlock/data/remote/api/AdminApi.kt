package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.admin.*
import com.subnetik.unlock.data.remote.dto.common.ApiMessageResponse
import retrofit2.http.*

interface AdminApi {
    // ─── Groups ──────────────────────────────────────────
    @GET("admin/groups")
    suspend fun getGroups(): List<AdminGroup>

    @POST("admin/groups")
    suspend fun createGroup(@Body request: AdminGroupCreateRequest): AdminGroup

    @PUT("admin/groups/{id}")
    suspend fun updateGroup(@Path("id") id: Int, @Body request: AdminGroupCreateRequest): AdminGroup

    @DELETE("admin/groups/{id}")
    suspend fun deleteGroup(@Path("id") id: Int): ApiMessageResponse

    @GET("admin/groups/{groupId}/students")
    suspend fun getGroupStudents(@Path("groupId") groupId: Int): List<AdminStudent>

    @POST("admin/groups/{groupId}/students")
    suspend fun createStudent(@Path("groupId") groupId: Int, @Body request: AdminStudentCreateRequest): AdminStudent

    @PUT("admin/groups/{groupId}/students/{studentId}")
    suspend fun updateStudent(@Path("groupId") groupId: Int, @Path("studentId") studentId: Int, @Body request: AdminStudentUpdateRequest): AdminStudent

    @DELETE("admin/groups/{groupId}/students/{studentId}")
    suspend fun deleteStudent(@Path("groupId") groupId: Int, @Path("studentId") studentId: Int): ApiMessageResponse

    // ─── Leads ───────────────────────────────────────────
    @GET("leads/admin")
    suspend fun getLeads(): List<AdminLead>

    @GET("leads/admin-stats")
    suspend fun getLeadStats(): AdminLeadStats

    @PATCH("leads/admin/{id}")
    suspend fun updateLead(@Path("id") id: Int, @Body request: AdminLeadUpdateRequest): AdminLead

    @DELETE("leads/admin/{id}")
    suspend fun deleteLead(@Path("id") id: Int): ApiMessageResponse

    @HTTP(method = "DELETE", path = "leads/admin/{id}/hard", hasBody = false)
    suspend fun hardDeleteLead(@Path("id") id: Int): ApiMessageResponse

    // ─── Users ───────────────────────────────────────────
    @GET("admin/users")
    suspend fun getUsers(): List<AdminUser>

    @PATCH("admin/users/{id}/role")
    suspend fun updateUserRole(@Path("id") id: Int, @Body request: AdminUserUpdateRequest): ApiMessageResponse

    @PATCH("admin/users/{id}/teacher")
    suspend fun assignTeacher(@Path("id") id: Int, @Body request: AssignTeacherRequest): ApiMessageResponse

    @GET("admin/users/teachers")
    suspend fun getTeachers(): List<AdminTeacherOption>

    @DELETE("admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): ApiMessageResponse

    @POST("admin/users/{id}/reset-password")
    suspend fun resetPassword(@Path("id") id: Int): PasswordResetResponse

    // ─── Support ─────────────────────────────────────────
    @GET("support/bookings/admin")
    suspend fun getSupportBookings(): List<AdminSupportBooking>

    @PATCH("support/bookings/admin/{id}")
    suspend fun updateSupportBooking(@Path("id") id: Int, @Body request: AdminSupportUpdateRequest): AdminSupportBooking

    // ─── Reviews ─────────────────────────────────────────
    @GET("reviews/admin")
    suspend fun getReviews(): List<AdminReview>

    @GET("reviews/admin-stats")
    suspend fun getReviewStats(): AdminReviewStats

    @PATCH("reviews/admin/{id}/approve")
    suspend fun approveReview(@Path("id") id: Int): AdminReview

    @PATCH("reviews/admin/{id}/reject")
    suspend fun rejectReview(@Path("id") id: Int): AdminReview

    @PATCH("reviews/admin/{id}")
    suspend fun updateReviewNote(@Path("id") id: Int, @Body request: AdminReviewUpdateRequest): AdminReview

    @DELETE("reviews/admin/{id}")
    suspend fun deleteReview(@Path("id") id: Int): ApiMessageResponse

    @HTTP(method = "DELETE", path = "reviews/admin/{id}/permanent", hasBody = false)
    suspend fun hardDeleteReview(@Path("id") id: Int): ApiMessageResponse

    // ─── Promocodes ──────────────────────────────────────
    @GET("promocodes/admin-stats")
    suspend fun getPromocodeStats(): AdminPromocodeStats

    @GET("promocodes/")
    suspend fun getPromocodes(@Query("status_filter") statusFilter: String = "all"): List<AdminPromocode>

    @POST("promocodes/")
    suspend fun createPromocode(@Body request: AdminPromocodeCreateRequest): AdminPromocode

    @PATCH("promocodes/{id}")
    suspend fun updatePromocode(@Path("id") id: Int, @Body request: AdminPromocodeUpdateRequest): AdminPromocode

    @PATCH("promocodes/{id}/activate")
    suspend fun activatePromocode(@Path("id") id: Int): AdminPromocode

    @PATCH("promocodes/{id}/deactivate")
    suspend fun deactivatePromocode(@Path("id") id: Int): AdminPromocode

    @DELETE("promocodes/{id}")
    suspend fun deletePromocode(@Path("id") id: Int): ApiMessageResponse

    @HTTP(method = "DELETE", path = "promocodes/{id}/hard", hasBody = false)
    suspend fun hardDeletePromocode(@Path("id") id: Int): ApiMessageResponse

    // ─── Blog ────────────────────────────────────────────
    @GET("blog/")
    suspend fun getBlogPosts(): List<AdminBlogPost>

    @POST("blog/")
    suspend fun createBlogPost(@Body request: AdminBlogCreateRequest): AdminBlogPost

    @PUT("blog/{id}")
    suspend fun updateBlogPost(@Path("id") id: Int, @Body request: AdminBlogUpdateRequest): AdminBlogPost

    @PATCH("blog/{id}/status")
    suspend fun updateBlogPostStatus(@Path("id") id: Int, @Body request: AdminBlogStatusRequest): ApiMessageResponse

    @DELETE("blog/{id}")
    suspend fun deleteBlogPost(@Path("id") id: Int): ApiMessageResponse

    // ─── Upload ──────────────────────────────────────────
    @Multipart
    @POST("upload/blog-image")
    suspend fun uploadBlogImage(@Part file: okhttp3.MultipartBody.Part): AdminUploadResponse

    // ─── Tokens ──────────────────────────────────────────
    @GET("admin/tokens/students")
    suspend fun getTokenStudents(): List<AdminTokenStudent>

    @GET("admin/tokens/transactions")
    suspend fun getTokenTransactions(): List<AdminTokenTransaction>

    @POST("admin/tokens/transactions")
    suspend fun createTokenTransaction(@Body request: AdminTokenTransactionCreateRequest): AdminTokenTransaction

    // ─── Market ──────────────────────────────────────────
    @GET("admin/tokens/market-items")
    suspend fun getMarketItems(): List<AdminMarketItem>

    @POST("admin/tokens/market-items")
    suspend fun createMarketItem(@Body request: AdminMarketItemCreateRequest): AdminMarketItem

    @PATCH("admin/tokens/market-items/{id}")
    suspend fun updateMarketItem(@Path("id") id: Int, @Body request: AdminMarketItemUpdateRequest): AdminMarketItem

    @DELETE("admin/tokens/market-items/{id}")
    suspend fun deleteMarketItem(@Path("id") id: Int): ApiMessageResponse

    // ─── Lessons ─────────────────────────────────────────
    @GET("lessons")
    suspend fun getLessons(): List<AdminLesson>

    @POST("lessons")
    suspend fun createLesson(@Body request: AdminLessonCreateRequest): AdminLesson

    @PUT("lessons/{id}")
    suspend fun updateLesson(@Path("id") id: Int, @Body request: AdminLessonCreateRequest): AdminLesson

    @DELETE("lessons/{id}")
    suspend fun deleteLesson(@Path("id") id: Int): ApiMessageResponse

    // ─── Homework ────────────────────────────────────────
    @GET("homework/groups")
    suspend fun getHomeworkGroups(): List<AdminHomeworkGroup>

    @GET("homework/assignments")
    suspend fun getHomeworkAssignments(
        @Query("include_completed") includeCompleted: Boolean = false,
    ): List<AdminHomeworkAssignment>

    @POST("homework/assignments")
    suspend fun createHomeworkAssignment(@Body request: AdminHomeworkCreateRequest): AdminHomeworkAssignment

    @PATCH("homework/assignments/{id}/complete")
    suspend fun toggleHomeworkCompleted(@Path("id") id: Int): AdminHomeworkAssignment

    @DELETE("homework/assignments/{id}")
    suspend fun deleteHomeworkAssignment(@Path("id") id: Int): ApiMessageResponse

    @GET("homework/my/groups")
    suspend fun getHomeworkStudentGroups(): List<HomeworkStudentGroupOverview>

    @GET("homework/groups/{groupId}/rating")
    suspend fun getGroupRating(@Path("groupId") groupId: Int): TeacherGroupRatingResponse

    // ─── Receipts ────────────────────────────────────────
    @GET("admin/payments/receipts")
    suspend fun getReceipts(): List<AdminReceipt>

    @PUT("admin/payments/receipts/{id}/approve")
    suspend fun approveReceipt(@Path("id") id: Int, @Body request: AdminReceiptActionRequest = AdminReceiptActionRequest()): AdminReceipt

    @PUT("admin/payments/receipts/{id}/reject")
    suspend fun rejectReceipt(@Path("id") id: Int, @Body request: AdminReceiptActionRequest = AdminReceiptActionRequest()): AdminReceipt

    // ─── Performance Events ─────────────────────────────
    @POST("homework/groups/{groupId}/performance-events")
    suspend fun createPerformanceEvent(
        @Path("groupId") groupId: Int,
        @Body request: CreatePerformanceEventRequest,
    ): PerformanceEventResponse

    // ─── Notifications ───────────────────────────────────
    @GET("notifications/dispatch/options")
    suspend fun getNotificationDispatchOptions(): AdminNotificationDispatchOptions

    @POST("notifications/dispatch")
    suspend fun dispatchNotification(@Body request: AdminNotificationDispatchRequest): AdminNotificationDispatchResponse
}
