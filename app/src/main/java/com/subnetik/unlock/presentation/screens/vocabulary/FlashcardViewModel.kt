package com.subnetik.unlock.presentation.screens.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.local.db.entity.VocabularyProgressEntity
import com.subnetik.unlock.data.remote.api.ProgressApi
import com.subnetik.unlock.domain.model.VocabularyWord
import com.subnetik.unlock.domain.repository.AuthRepository
import com.subnetik.unlock.domain.repository.VocabularyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject

data class FlashcardUiState(
    val currentWord: VocabularyWord? = null,
    val currentIndex: Int = 0,
    val totalCount: Int = 0,
    val knownCount: Int = 0,
    val reviewCount: Int = 0,
    val previouslyKnown: Int = 0,
    val isComplete: Boolean = false,
    val progress: Float = 0f,
    val isDarkTheme: Boolean? = true,
)

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
    private val progressApi: ProgressApi,
    private val json: Json,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardUiState())

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
    }
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    private var allWords = listOf<VocabularyWord>()
    private var knownIds = mutableSetOf<String>()
    private var reviewIds = mutableSetOf<String>()
    private var currentLevel = 1

    fun loadCards(level: Int) {
        currentLevel = level
        viewModelScope.launch {
            val words = vocabularyRepository.getWords(level)
            val email = authRepository.getUserEmail().first() ?: ""

            // Load existing progress
            val progress = vocabularyRepository.getProgress(email, level)
            val existingKnown = progress?.let {
                try { json.decodeFromString<List<String>>(it.knownWordIdsJson).filter { id -> id.startsWith("h") }.toMutableSet() } catch (_: Exception) { mutableSetOf() }
            } ?: mutableSetOf()
            val existingReview = progress?.let {
                try { json.decodeFromString<List<String>>(it.reviewWordIdsJson).filter { id -> id.startsWith("h") }.toMutableSet() } catch (_: Exception) { mutableSetOf() }
            } ?: mutableSetOf()

            knownIds = existingKnown.toMutableSet()
            reviewIds = existingReview.toMutableSet()

            // Sort: unknown first, then known
            val unknown = words.filter { it.id !in knownIds }.shuffled()
            val known = words.filter { it.id in knownIds }.shuffled()
            allWords = unknown + known

            if (allWords.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        currentWord = allWords.first(),
                        currentIndex = 0,
                        totalCount = allWords.size,
                        knownCount = knownIds.size,
                        reviewCount = reviewIds.size,
                        previouslyKnown = knownIds.size,
                        isComplete = false,
                        progress = 0f,
                    )
                }
            }
        }
    }

    fun markKnown() {
        val word = _uiState.value.currentWord ?: return
        knownIds.add(word.id)
        reviewIds.remove(word.id)
        saveAndAdvance()
    }

    fun markReview() {
        val word = _uiState.value.currentWord ?: return
        reviewIds.add(word.id)
        knownIds.remove(word.id)
        saveAndAdvance()
    }

    private fun saveAndAdvance() {
        // Save after every action
        saveProgress()
        syncToServer()

        val nextIndex = _uiState.value.currentIndex + 1
        if (nextIndex >= allWords.size) {
            _uiState.update {
                it.copy(
                    isComplete = true,
                    knownCount = knownIds.size,
                    reviewCount = reviewIds.size,
                    progress = 1f,
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    currentWord = allWords[nextIndex],
                    currentIndex = nextIndex,
                    knownCount = knownIds.size,
                    reviewCount = reviewIds.size,
                    progress = nextIndex.toFloat() / allWords.size,
                )
            }
        }
    }

    private fun saveProgress() {
        viewModelScope.launch {
            val email = authRepository.getUserEmail().first() ?: return@launch
            val entity = VocabularyProgressEntity(
                key = "${email}_$currentLevel",
                knownWordIdsJson = json.encodeToString(
                    kotlinx.serialization.builtins.ListSerializer(kotlinx.serialization.serializer<String>()),
                    knownIds.toList()
                ),
                reviewWordIdsJson = json.encodeToString(
                    kotlinx.serialization.builtins.ListSerializer(kotlinx.serialization.serializer<String>()),
                    reviewIds.toList()
                ),
                lastStudiedAt = Instant.now().toString(),
            )
            vocabularyRepository.saveProgress(entity)
        }
    }

    private fun syncToServer() {
        viewModelScope.launch {
            try {
                val totalWords = vocabularyRepository.getWords(currentLevel).size
                val vocabItem = com.subnetik.unlock.data.remote.dto.progress.VocabProgressSyncItem(
                    level = currentLevel,
                    totalWords = totalWords,
                    knownCount = knownIds.size,
                    reviewCount = reviewIds.size,
                    knownWordIds = knownIds.toList(),
                    reviewWordIds = reviewIds.toList(),
                )
                progressApi.syncProgress(
                    com.subnetik.unlock.data.remote.dto.progress.ProgressSyncRequest(
                        tests = emptyList(),
                        vocabulary = listOf(vocabItem),
                    )
                )
            } catch (_: Exception) { }
        }
    }
}
