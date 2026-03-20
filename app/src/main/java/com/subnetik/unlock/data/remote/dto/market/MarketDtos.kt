package com.subnetik.unlock.data.remote.dto.market

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarketItemResponse(
    val id: Int,
    val code: String,
    val name: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("token_price") val tokenPrice: Int,
    val category: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)

@Serializable
data class MarketPurchaseResponse(
    val id: Int,
    @SerialName("item_code") val itemCode: String,
    @SerialName("item_name") val itemName: String,
    @SerialName("item_image_url") val itemImageUrl: String? = null,
    @SerialName("token_price") val tokenPrice: Int,
    val quantity: Int,
    @SerialName("total_tokens") val totalTokens: Int,
    val status: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class MarketPurchaseRequest(
    @SerialName("item_code") val itemCode: String,
    val quantity: Int = 1,
)
