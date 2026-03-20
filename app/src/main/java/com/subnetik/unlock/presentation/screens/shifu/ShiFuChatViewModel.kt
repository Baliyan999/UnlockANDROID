package com.subnetik.unlock.presentation.screens.shifu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.remote.dto.ai.AiConversationListItem
import com.subnetik.unlock.domain.repository.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ShiFuMood(val drawableSuffix: String) {
    NORMAL(""),
    HAPPY("_happy"),
    SAD("_sad"),
    THINKING("_thinking"),
    WAVE("_wave");

    companion object {
        fun from(tag: String): ShiFuMood = when (tag.lowercase()) {
            "happy" -> HAPPY
            "sad" -> SAD
            "thinking" -> THINKING
            "wave" -> WAVE
            else -> NORMAL
        }
    }
}

data class ChatMessage(
    val id: Int,
    val role: String,
    val content: String,
    val mood: ShiFuMood = ShiFuMood.NORMAL,
    val createdAt: String = "",
)

data class ShiFuUiState(
    val messages: List<ChatMessage> = emptyList(),
    val conversations: List<AiConversationListItem> = emptyList(),
    val currentConversationId: Int? = null,
    val currentTitle: String = "Помощник Ши Фу",
    val isSending: Boolean = false,
    val isLoadingConversations: Boolean = false,
    val isLoadingMessages: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ShiFuChatViewModel @Inject constructor(
    private val aiRepository: AiRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShiFuUiState())
    val uiState: StateFlow<ShiFuUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _uiState.value.isSending) return

        val userMsg = ChatMessage(
            id = -(_uiState.value.messages.size + 1),
            role = "user",
            content = trimmed,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                isSending = true,
                error = null,
            )
        }

        viewModelScope.launch {
            try {
                val response = aiRepository.sendMessage(
                    message = trimmed,
                    conversationId = _uiState.value.currentConversationId,
                )
                val (cleaned, mood) = extractMoodAndClean(response.message.content)
                val assistantMsg = ChatMessage(
                    id = response.message.id,
                    role = response.message.role,
                    content = cleaned,
                    mood = mood,
                    createdAt = response.message.createdAt,
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + assistantMsg,
                        currentConversationId = response.conversationId,
                        isSending = false,
                    )
                }
                updateTitle()
                loadConversations()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Ошибка: ${e.localizedMessage ?: "Неизвестная ошибка"}",
                        isSending = false,
                    )
                }
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingConversations = true) }
            try {
                val list = aiRepository.getConversations()
                _uiState.update {
                    it.copy(conversations = list, isLoadingConversations = false)
                }
                updateTitle()
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingConversations = false) }
            }
        }
    }

    fun loadConversation(conversationId: Int) {
        _uiState.update {
            it.copy(
                currentConversationId = conversationId,
                messages = emptyList(),
                isLoadingMessages = true,
                error = null,
            )
        }
        viewModelScope.launch {
            try {
                val detail = aiRepository.getConversation(conversationId)
                val mapped = detail.messages.map { msg ->
                    if (msg.role == "assistant") {
                        val (cleaned, mood) = extractMoodAndClean(msg.content)
                        ChatMessage(msg.id, msg.role, cleaned, mood, msg.createdAt)
                    } else {
                        ChatMessage(msg.id, msg.role, msg.content, createdAt = msg.createdAt)
                    }
                }
                _uiState.update {
                    it.copy(messages = mapped, isLoadingMessages = false)
                }
                updateTitle()
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Не удалось загрузить диалог",
                        isLoadingMessages = false,
                    )
                }
            }
        }
    }

    fun startNewConversation() {
        _uiState.update {
            it.copy(
                currentConversationId = null,
                messages = emptyList(),
                error = null,
                currentTitle = "Помощник Ши Фу",
            )
        }
    }

    fun deleteConversation(conversationId: Int) {
        if (_uiState.value.currentConversationId == conversationId) {
            startNewConversation()
        }
        _uiState.update {
            it.copy(conversations = it.conversations.filter { c -> c.id != conversationId })
        }
        viewModelScope.launch {
            try {
                aiRepository.deleteConversation(conversationId)
            } catch (_: Exception) {
                // silent
            }
        }
    }

    private fun updateTitle() {
        val state = _uiState.value
        val id = state.currentConversationId
        val conv = if (id != null) state.conversations.firstOrNull { it.id == id } else null
        val title = conv?.title?.takeIf { it.isNotEmpty() } ?: "Помощник Ши Фу"
        _uiState.update { it.copy(currentTitle = title) }
    }

    private fun extractMoodAndClean(text: String): Pair<String, ShiFuMood> {
        val regex = Regex("\\[MOOD:(\\w+)]")
        val match = regex.find(text)
        val mood = match?.groupValues?.getOrNull(1)?.let { ShiFuMood.from(it) } ?: ShiFuMood.NORMAL
        val cleaned = regex.replace(text, "").trim()
        return cleaned to mood
    }
}
