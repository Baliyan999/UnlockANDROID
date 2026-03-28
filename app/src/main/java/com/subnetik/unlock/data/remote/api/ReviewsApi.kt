package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.reviews.PublicReviewDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ReviewsApi {
    @GET("reviews/public")
    suspend fun getPublicReviews(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): List<PublicReviewDto>
}
