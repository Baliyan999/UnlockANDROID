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
    @SerialName("likes") val likesCount: Int,
)

@Serializable
data class LikeStatusResponse(
    val liked: Boolean,
)

@Serializable
data class ViewResponse(
    @SerialName("views_count") val viewsCount: Int,
)

@Serializable
data class CreateBlogPostRequest(
    val title: String,
    val excerpt: String? = null,
    val content: String,
    val slug: String,
    val language: String = "ru",
    val status: String = "draft",
    @SerialName("image_url") val imageUrl: String? = null,
)

@Serializable
data class UpdateBlogPostRequest(
    val title: String? = null,
    val excerpt: String? = null,
    val content: String? = null,
    val slug: String? = null,
    val language: String? = null,
    val status: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
)
