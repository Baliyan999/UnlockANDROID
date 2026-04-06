package com.subnetik.unlock.presentation.screens.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.BuildConfig
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
import javax.inject.Inject

data class VocabularyUiState(
    val words: List<VocabularyWord> = emptyList(),
    val filteredWords: List<VocabularyWord> = emptyList(),
    val totalWords: Int = 0,
    val knownCount: Int = 0,
    val reviewCount: Int = 0,
    val knownWordIds: Set<String> = emptySet(),
    val reviewWordIds: Set<String> = emptySet(),
    val isDarkTheme: Boolean? = true,
    val levelProgress: Map<Int, Int> = emptyMap(), // level -> knownCount
)

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
    private val progressApi: ProgressApi,
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
                try { json.decodeFromString<List<String>>(it.knownWordIdsJson).filter { id -> id.startsWith("h") }.toMutableSet() } catch (_: Exception) { mutableSetOf() }
            } ?: mutableSetOf()
            reviewIds = progress?.let {
                try { json.decodeFromString<List<String>>(it.reviewWordIdsJson).filter { id -> id.startsWith("h") }.toMutableSet() } catch (_: Exception) { mutableSetOf() }
            } ?: mutableSetOf()

            // FETCH first — don't send stale local data that could corrupt server
            try {
                val serverResponse = progressApi.getMyProgress()
                val serverLevel = serverResponse.vocabulary.find { it.level == level }
                val serverKnown = serverLevel?.knownWordIds?.filter { it.startsWith("h") }?.toSet() ?: emptySet()
                val serverReview = serverLevel?.reviewWordIds?.filter { it.startsWith("h") }?.toSet() ?: emptySet()
                if (serverKnown.isNotEmpty() || serverReview.isNotEmpty()) {
                    // Server is truth. Only add words server doesn't know about.
                    val serverAllWords = serverKnown + serverReview
                    val localOnlyKnown = knownIds.filter { it !in serverAllWords }.toSet()
                    val localOnlyReview = reviewIds.filter { it !in serverAllWords }.toSet()
                    knownIds = (serverKnown + localOnlyKnown).toMutableSet()
                    reviewIds = (serverReview + localOnlyReview).toMutableSet()
                    saveProgress(level)
                    // Only sync back if we had local-only words
                    if (localOnlyKnown.isNotEmpty() || localOnlyReview.isNotEmpty()) {
                        syncLevelToServerSync(level)
                    }
                    if (BuildConfig.DEBUG) android.util.Log.d("VocabVM", "loadWords synced: known=${knownIds.size} review=${reviewIds.size}")
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) android.util.Log.w("VocabVM", "Server sync failed: ${e.message}")
            }

            _uiState.update {
                it.copy(
                    words = words,
                    filteredWords = words,
                    totalWords = words.size,
                    knownCount = knownIds.size,
                    reviewCount = reviewIds.size,
                    knownWordIds = knownIds.toSet(),
                    reviewWordIds = reviewIds.toSet(),
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
        syncLevelToServer(level)
    }

    fun markReview(wordId: String, level: Int) {
        knownIds.remove(wordId)
        reviewIds.add(wordId)
        updateCounts()
        saveProgress(level)
        syncLevelToServer(level)
    }

    private fun updateCounts() {
        _uiState.update {
            it.copy(
                knownCount = knownIds.size,
                reviewCount = reviewIds.size,
                knownWordIds = knownIds.toSet(),
                reviewWordIds = reviewIds.toSet(),
            )
        }
    }

    fun loadAllLevelProgress() {
        viewModelScope.launch {
            val email = authRepository.getUserEmail().first() ?: ""
            val progressMap = mutableMapOf<Int, Int>()
            val localKnownMap = mutableMapOf<Int, MutableSet<String>>()
            val localReviewMap = mutableMapOf<Int, MutableSet<String>>()

            for (level in 1..6) {
                val progress = vocabularyRepository.getProgress(email, level)
                val knownSet = progress?.let {
                    try { json.decodeFromString<List<String>>(it.knownWordIdsJson).filter { id -> id.startsWith("h") }.toMutableSet() } catch (_: Exception) { mutableSetOf() }
                } ?: mutableSetOf()
                val reviewSet = progress?.let {
                    try { json.decodeFromString<List<String>>(it.reviewWordIdsJson).filter { id -> id.startsWith("h") }.toMutableSet() } catch (_: Exception) { mutableSetOf() }
                } ?: mutableSetOf()
                localKnownMap[level] = knownSet
                localReviewMap[level] = reviewSet
                progressMap[level] = knownSet.size
            }

            // FETCH first — don't corrupt server with stale local data
            var hasLocalOnly = false
            try {
                val serverResponse = progressApi.getMyProgress()
                for (item in serverResponse.vocabulary) {
                    val serverKnown = (item.knownWordIds ?: emptyList()).filter { it.startsWith("h") }.toSet()
                    val serverReview = (item.reviewWordIds ?: emptyList()).filter { it.startsWith("h") }.toSet()
                    if (serverKnown.isEmpty() && serverReview.isEmpty()) continue

                    val localKnown = localKnownMap[item.level] ?: mutableSetOf()
                    val localReview = localReviewMap[item.level] ?: mutableSetOf()

                    // Server is truth. Only add words server doesn't know about.
                    val serverAllWords = serverKnown + serverReview
                    val localOnlyKnown = localKnown.filter { it !in serverAllWords }.toSet()
                    val localOnlyReview = localReview.filter { it !in serverAllWords }.toSet()
                    if (localOnlyKnown.isNotEmpty() || localOnlyReview.isNotEmpty()) hasLocalOnly = true

                    val mergedKnown = serverKnown + localOnlyKnown
                    val mergedReview = serverReview + localOnlyReview

                    val entity = VocabularyProgressEntity(
                        key = "${email}_${item.level}",
                        knownWordIdsJson = json.encodeToString(
                            kotlinx.serialization.builtins.ListSerializer(kotlinx.serialization.serializer<String>()),
                            mergedKnown.toList()
                        ),
                        reviewWordIdsJson = json.encodeToString(
                            kotlinx.serialization.builtins.ListSerializer(kotlinx.serialization.serializer<String>()),
                            mergedReview.toList()
                        ),
                    )
                    vocabularyRepository.saveProgress(entity)
                    localKnownMap[item.level] = mergedKnown.toMutableSet()
                    localReviewMap[item.level] = mergedReview.toMutableSet()
                    progressMap[item.level] = mergedKnown.size
                    if (BuildConfig.DEBUG) android.util.Log.d("VocabVM", "Merged level ${item.level}: known=${mergedKnown.size} review=${mergedReview.size}")
                }
                // Only sync back if we had local-only words to push
                if (hasLocalOnly) {
                    syncLocalToServer(email, localKnownMap, localReviewMap)
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) android.util.Log.w("VocabVM", "Server progress load failed: ${e.message}")
            }

            _uiState.update { it.copy(levelProgress = progressMap) }
        }
    }

    private suspend fun syncLocalToServer(
        email: String,
        knownMap: Map<Int, Set<String>>,
        reviewMap: Map<Int, Set<String>>,
    ) {
        try {
            val vocabItems = (1..6).mapNotNull { level ->
                val known = knownMap[level] ?: return@mapNotNull null
                val review = reviewMap[level] ?: emptySet()
                if (known.isEmpty() && review.isEmpty()) return@mapNotNull null
                com.subnetik.unlock.data.remote.dto.progress.VocabProgressSyncItem(
                    level = level,
                    totalWords = vocabularyRepository.getWords(level).size,
                    knownCount = known.size,
                    reviewCount = review.size,
                    knownWordIds = known.toList(),
                    reviewWordIds = review.toList(),
                )
            }
            if (vocabItems.isNotEmpty()) {
                progressApi.syncProgress(
                    com.subnetik.unlock.data.remote.dto.progress.ProgressSyncRequest(
                        tests = emptyList(),
                        vocabulary = vocabItems,
                    )
                )
                if (BuildConfig.DEBUG) android.util.Log.d("VocabVM", "Synced ${vocabItems.size} levels to server")
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.w("VocabVM", "Sync to server failed: ${e.message}")
        }
    }

    private suspend fun syncLevelToServerSync(level: Int) {
        try {
            val totalWords = vocabularyRepository.getWords(level).size
            val vocabItem = com.subnetik.unlock.data.remote.dto.progress.VocabProgressSyncItem(
                level = level,
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

    private fun syncLevelToServer(level: Int) {
        viewModelScope.launch {
            try {
                val totalWords = vocabularyRepository.getWords(level).size
                val vocabItem = com.subnetik.unlock.data.remote.dto.progress.VocabProgressSyncItem(
                    level = level,
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
                if (BuildConfig.DEBUG) android.util.Log.d("VocabVM", "Synced level $level: ${knownIds.size} known")
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) android.util.Log.w("VocabVM", "Sync level $level failed: ${e.message}")
            }
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
