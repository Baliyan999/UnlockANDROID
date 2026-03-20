package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.ai.*
import com.subnetik.unlock.data.remote.dto.common.ApiMessageResponse
import retrofit2.http.*

interface AiApi {
    @POST("ai/chat")
    suspend fun sendMessage(@Body request: AiChatRequest): AiChatResponse

    @GET("ai/conversations")
    suspend fun getConversations(): List<AiConversationListItem>

    @GET("ai/conversations/{id}")
    suspend fun getConversation(@Path("id") conversationId: Int): AiConversationDetail

    @DELETE("ai/conversations/{id}")
    suspend fun deleteConversation(@Path("id") conversationId: Int): ApiMessageResponse
}
