package com.subnetik.unlock.domain.repository

import com.subnetik.unlock.data.remote.dto.ai.*

interface AiRepository {
    suspend fun sendMessage(message: String, conversationId: Int?): AiChatResponse
    suspend fun getConversations(): List<AiConversationListItem>
    suspend fun getConversation(conversationId: Int): AiConversationDetail
    suspend fun deleteConversation(conversationId: Int)
}
