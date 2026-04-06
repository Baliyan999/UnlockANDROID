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

    @POST("blog/")
    suspend fun createPost(@Body request: CreateBlogPostRequest): BlogPostDto

    @PUT("blog/{postId}")
    suspend fun updatePost(@Path("postId") postId: Int, @Body request: UpdateBlogPostRequest): BlogPostDto

    @DELETE("blog/{postId}")
    suspend fun deletePost(@Path("postId") postId: Int): Any

    @PATCH("blog/{postId}/status")
    suspend fun updateStatus(@Path("postId") postId: Int, @Body request: Map<String, String>): Any
}
