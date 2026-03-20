package com.subnetik.unlock.data.remote.dto.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiChatRequest(
    val message: String,
    @SerialName("conversation_id") val conversationId: Int? = null,
)

@Serializable
data class AiChatResponse(
    @SerialName("conversation_id") val conversationId: Int,
    val message: AiMessageResponse,
)

@Serializable
data class AiMessageResponse(
    val id: Int,
    val role: String,
    val content: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class AiConversationListItem(
    val id: Int,
    val title: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("message_count") val messageCount: Int,
)

@Serializable
data class AiConversationDetail(
    val id: Int,
    val title: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val messages: List<AiMessageResponse>,
)
