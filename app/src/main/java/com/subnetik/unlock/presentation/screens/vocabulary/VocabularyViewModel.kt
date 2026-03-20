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
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class VocabularyUiState(
    val words: List<VocabularyWord> = emptyList(),
    val filteredWords: List<VocabularyWord> = emptyList(),
    val totalWords: Int = 0,
    val knownCount: Int = 0,
    val reviewCount: Int = 0,
    val isDarkTheme: Boolean? = null,
)

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
    private val json: Json,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularyUiState())

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
    }
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    private var knownIds = mutableSetOf<String>()
    private var reviewIds = mutableSetOf<String>()

    fun loadWords(level: Int) {
        viewModelScope.launch {
            val words = vocabularyRepository.getWords(level)
            val email = authRepository.getUserEmail().first() ?: ""
            val progress = vocabularyRepository.getProgress(email, level)
            knownIds = progress?.let {
                try { json.decodeFromString<List<String>>(it.knownWordIdsJson).toMutableSet() } catch (_: Exception) { mutableSetOf() }
            } ?: mutableSetOf()

            _uiState.update {
                it.copy(
                    words = words,
                    filteredWords = words,
                    totalWords = words.size,
                    knownCount = knownIds.size,
                    reviewCount = words.size - knownIds.size,
                )
            }
        }
    }

    fun search(level: Int, query: String) {
        viewModelScope.launch {
            val filtered = vocabularyRepository.searchWords(level, query)
            _uiState.update { it.copy(filteredWords = filtered) }
        }
    }

    fun markKnown(wordId: String, level: Int) {
        knownIds.add(wordId)
        reviewIds.remove(wordId)
        updateCounts()
        saveProgress(level)
    }

    fun markReview(wordId: String, level: Int) {
        knownIds.remove(wordId)
        reviewIds.add(wordId)
        updateCounts()
        saveProgress(level)
    }

    private fun updateCounts() {
        _uiState.update {
            it.copy(
                knownCount = knownIds.size,
                reviewCount = it.totalWords - knownIds.size,
            )
        }
    }

    private fun saveProgress(level: Int) {
        viewModelScope.launch {
            val email = authRepository.getUserEmail().first() ?: ""
            val entity = VocabularyProgressEntity(
                key = "${email}_${level}",
                knownWordIdsJson = json.encodeToString(kotlinx.serialization.builtins.ListSerializer(kotlinx.serialization.serializer<String>()), knownIds.toList()),
                reviewWordIdsJson = json.encodeToString(kotlinx.serialization.builtins.ListSerializer(kotlinx.serialization.serializer<String>()), reviewIds.toList()),
            )
            vocabularyRepository.saveProgress(entity)
        }
    }
}
