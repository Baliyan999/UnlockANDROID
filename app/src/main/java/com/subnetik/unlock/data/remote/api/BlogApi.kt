package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.blog.*
import retrofit2.http.*

interface BlogApi {

    @GET("blog/")
    suspend fun getPosts(): List<BlogPostDto>

    @GET("blog/{id}")
    suspend fun getPost(@Path("id") id: Int): BlogPostDto

    @POST("blog/{id}/like")
    suspend fun toggleLike(@Path("id") id: Int): LikeResponse

    @GET("blog/{id}/like-status")
    suspend fun getLikeStatus(@Path("id") id: Int): LikeStatusResponse

    @POST("blog/{id}/view")
    suspend fun incrementView(@Path("id") id: Int): ViewResponse
}
