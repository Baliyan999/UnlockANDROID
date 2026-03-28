package com.subnetik.unlock.data.remote.dto.blog

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlogPostDto(
    val id: Int,
    val title: String,
    val excerpt: String,
    val content: String,
    val slug: String,
    val language: String = "ru",
    val status: String = "published",
    @SerialName("image_url") val imageUrl: String? = null,
    val views: Int = 0,
    val likes: Int = 0,
    val author: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class LikeResponse(
    val liked: Boolean,
    @SerialName("likes_count") val likesCount: Int,
)

@Serializable
data class LikeStatusResponse(
    val liked: Boolean,
)

@Serializable
data class ViewResponse(
    @SerialName("views_count") val viewsCount: Int,
)
