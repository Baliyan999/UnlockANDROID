package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.reviews.PublicReviewDto
import com.subnetik.unlock.data.remote.dto.reviews.SubmitReviewRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReviewsApi {
    @GET("reviews/public")
    suspend fun getPublicReviews(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): List<PublicReviewDto>

    @POST("reviews/")
    suspend fun submitReview(@Body request: SubmitReviewRequest): PublicReviewDto
}
