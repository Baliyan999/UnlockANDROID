package com.subnetik.unlock.presentation.screens.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.local.db.entity.VocabularyProgressEntity
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject

data class FlashcardUiState(
    val currentWord: VocabularyWord? = null,
    val currentIndex: Int = 0,
    val totalCount: Int = 0,
    val knownCount: Int = 0,
    val reviewCount: Int = 0,
    val isComplete: Boolean = false,
    val progress: Float = 0f,
    val isDarkTheme: Boolean? = null,
)

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
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
    private var knownIds = mutableListOf<String>()
    private var reviewIds = mutableListOf<String>()
    private var currentLevel = 1

    fun loadCards(level: Int) {
        currentLevel = level
        viewModelScope.launch {
            val words = vocabularyRepository.getWords(level).shuffled()
            allWords = words
            knownIds.clear()
            reviewIds.clear()

            if (words.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        currentWord = words.first(),
                        currentIndex = 0,
                        totalCount = words.size,
                        knownCount = 0,
                        reviewCount = 0,
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
        advance()
    }

    fun markReview() {
        val word = _uiState.value.currentWord ?: return
        reviewIds.add(word.id)
        advance()
    }

    private fun advance() {
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
            saveProgress()
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
                knownWordIdsJson = json.encodeToString(knownIds.toList()),
                reviewWordIdsJson = json.encodeToString(reviewIds.toList()),
                lastStudiedAt = Instant.now().toString(),
            )
            vocabularyRepository.saveProgress(entity)
        }
    }
}
