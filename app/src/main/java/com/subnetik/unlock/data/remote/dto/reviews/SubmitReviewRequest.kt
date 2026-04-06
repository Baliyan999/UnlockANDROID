package com.subnetik.unlock.data.remote.dto.reviews

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubmitReviewRequest(
    val author: String,
    val text: String,
    val rating: Int,
    @SerialName("is_student") val isStudent: Boolean = false,
    @SerialName("image_url") val imageUrl: String? = null,
)
