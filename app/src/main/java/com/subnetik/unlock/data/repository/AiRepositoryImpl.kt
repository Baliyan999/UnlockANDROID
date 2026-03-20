package com.subnetik.unlock.data.repository

import com.subnetik.unlock.data.remote.api.AiApi
import com.subnetik.unlock.data.remote.dto.ai.*
import com.subnetik.unlock.domain.repository.AiRepository
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val aiApi: AiApi,
) : AiRepository {

    override suspend fun sendMessage(message: String, conversationId: Int?): AiChatResponse =
        aiApi.sendMessage(AiChatRequest(message = message, conversationId = conversationId))

    override suspend fun getConversations(): List<AiConversationListItem> =
        aiApi.getConversations()

    override suspend fun getConversation(conversationId: Int): AiConversationDetail =
        aiApi.getConversation(conversationId)

    override suspend fun deleteConversation(conversationId: Int) {
        aiApi.deleteConversation(conversationId)
    }
}
