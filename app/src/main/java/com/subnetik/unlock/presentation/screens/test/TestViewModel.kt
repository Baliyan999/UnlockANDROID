package com.subnetik.unlock.presentation.screens.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.BuildConfig
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.local.db.entity.TestProgressEntity
import com.subnetik.unlock.data.remote.api.ProgressApi
import com.subnetik.unlock.data.remote.dto.progress.ProgressSyncRequest
import com.subnetik.unlock.data.remote.dto.progress.TestAttemptDetail
import com.subnetik.unlock.data.remote.dto.progress.TestProgressSyncItem
import com.subnetik.unlock.domain.model.TestQuestion
import com.subnetik.unlock.domain.repository.AuthRepository
import com.subnetik.unlock.domain.repository.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class TestUiState(
    val questions: List<TestQuestion> = emptyList(),
    val currentQuestion: TestQuestion? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val selectedAnswerId: String? = null,
    val answerRevealed: Boolean = false,
    val score: Int = 0,
    val testComplete: Boolean = false,
    val remainingSeconds: Int = 600,
    val levelProgress: Map<Int, TestProgressEntity> = emptyMap(),
    val isDarkTheme: Boolean? = true,
    val showTrialButton: Boolean = false,
    val isLoggedIn: Boolean = false,
    val answerDetails: List<TestAttemptDetail> = emptyList(),
)

@HiltViewModel
class TestViewModel @Inject constructor(
    private val testRepository: TestRepository,
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
    private val progressApi: ProgressApi,
    private val json: kotlinx.serialization.json.Json,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestUiState())
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        viewModelScope.launch {
            authRepository.getUserRole().collect { role ->
                _uiState.update {
                    it.copy(
                        showTrialButton = role == com.subnetik.unlock.domain.model.AppUserRole.USER || role == com.subnetik.unlock.domain.model.AppUserRole.GUEST,
                    )
                }
            }
        }
        viewModelScope.launch {
            authRepository.isLoggedIn().collect { loggedIn ->
                _uiState.update { it.copy(isLoggedIn = loggedIn) }
            }
        }
        loadAllProgress()
    }

    private fun loadAllProgress() {
        viewModelScope.launch {
            val email = authRepository.getUserEmail().first() ?: return@launch

            // Load local first for instant UI
            val localProgress = testRepository.getAllProgress(email)
            val map = mutableMapOf<Int, TestProgressEntity>()
            localProgress.forEach { entity ->
                val levelStr = entity.key.removePrefix("${email}_")
                val level = levelStr.toIntOrNull() ?: return@forEach
                map[level] = entity
            }
            _uiState.update { it.copy(levelProgress = map) }

            // Then fetch from server and merge (server is truth)
            try {
                val serverResponse = progressApi.getMyProgress()
                var hasLocalOnly = false

                for (item in serverResponse.tests) {
                    // Server uses "hsk1", "hsk2" format; also handle plain "1", "2"
                    val level = item.levelId.removePrefix("hsk").toIntOrNull() ?: continue
                    val local = map[level]

                    // Merge: take best from server and local
                    val mergedBestPercent = maxOf(item.bestPercent, local?.bestPercent ?: 0)
                    val mergedBestScore = maxOf(item.bestScore, local?.bestScore ?: 0)
                    val mergedAttempts = maxOf(item.attempts, local?.attempts ?: 0)
                    val mergedPassed = item.passed || local?.passed == true

                    if (local != null && (local.bestPercent > item.bestPercent || local.attempts > item.attempts)) {
                        hasLocalOnly = true
                    }

                    // Merge bestAttemptDetails: use server's if server has better score, otherwise keep local
                    val mergedDetailsJson = if (item.bestPercent >= (local?.bestPercent ?: 0)) {
                        item.bestAttemptDetails?.let { details ->
                            try { json.encodeToString(kotlinx.serialization.builtins.ListSerializer(TestAttemptDetail.serializer()), details) } catch (_: Exception) { null }
                        } ?: local?.bestAttemptDetailsJson
                    } else {
                        local?.bestAttemptDetailsJson
                    }

                    val entity = TestProgressEntity(
                        key = "${email}_$level",
                        bestPercent = mergedBestPercent,
                        bestScore = mergedBestScore,
                        totalQuestions = item.totalQuestions,
                        attempts = mergedAttempts,
                        passed = mergedPassed,
                        lastAttemptAt = item.lastAttemptAt ?: local?.lastAttemptAt,
                        bestAttemptDetailsJson = mergedDetailsJson,
                    )
                    testRepository.saveProgress(entity)
                    map[level] = entity
                }

                _uiState.update { it.copy(levelProgress = map) }

                // Sync back if local had better data
                if (hasLocalOnly) {
                    syncAllToServer(email, map)
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) android.util.Log.w("TestVM", "Server fetch failed: ${e.message}")
            }
        }
    }

    fun startTest(level: Int) {
        val questions = testRepository.getRandomQuestions(level, 10)
        if (questions.isEmpty()) {
            _uiState.update { it.copy(testComplete = true, score = 0, totalQuestions = 0) }
            return
        }
        _uiState.update {
            it.copy(
                questions = questions,
                currentQuestion = questions.first(),
                currentQuestionIndex = 0,
                totalQuestions = questions.size,
                selectedAnswerId = null,
                answerRevealed = false,
                score = 0,
                testComplete = false,
                remainingSeconds = 600,
                answerDetails = emptyList(),
            )
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && !_uiState.value.testComplete) {
                delay(1000)
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            if (_uiState.value.remainingSeconds <= 0) {
                finishTest()
            }
        }
    }

    fun selectAnswer(answerId: String) {
        if (_uiState.value.answerRevealed) return
        val question = _uiState.value.currentQuestion ?: return
        val selectedAnswer = question.answers.find { it.id == answerId }
        val isCorrect = selectedAnswer?.isCorrect == true
        val correctAnswer = question.answers.find { it.isCorrect }

        val detail = TestAttemptDetail(
            questionId = question.id,
            prompt = question.prompt,
            selectedAnswer = selectedAnswer?.text,
            correctAnswer = correctAnswer?.text,
            isCorrect = isCorrect,
        )

        _uiState.update {
            it.copy(
                selectedAnswerId = answerId,
                answerRevealed = true,
                score = if (isCorrect) it.score + 1 else it.score,
                answerDetails = it.answerDetails + detail,
            )
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        val nextIndex = state.currentQuestionIndex + 1
        if (nextIndex >= state.questions.size) {
            finishTest()
        } else {
            _uiState.update {
                it.copy(
                    currentQuestion = state.questions[nextIndex],
                    currentQuestionIndex = nextIndex,
                    selectedAnswerId = null,
                    answerRevealed = false,
                )
            }
        }
    }

    private fun finishTest() {
        timerJob?.cancel()
        val state = _uiState.value
        _uiState.update { it.copy(testComplete = true) }
        saveProgress(state)
    }

    private fun saveProgress(state: TestUiState) {
        viewModelScope.launch {
            val email = authRepository.getUserEmail().first() ?: return@launch
            val level = state.currentQuestion?.level ?: return@launch
            val percent = if (state.totalQuestions > 0) (state.score * 100) / state.totalQuestions else 0
            val existing = testRepository.getProgress(email, "$level")

            // Determine if this attempt is a new best — if so, update bestAttemptDetails
            val isBetterScore = percent > (existing?.bestPercent ?: 0) ||
                    (percent == (existing?.bestPercent ?: 0) && state.score > (existing?.bestScore ?: 0))

            val detailsJson = if (isBetterScore && state.answerDetails.isNotEmpty()) {
                json.encodeToString(kotlinx.serialization.builtins.ListSerializer(TestAttemptDetail.serializer()), state.answerDetails)
            } else {
                existing?.bestAttemptDetailsJson
            }

            val entity = TestProgressEntity(
                key = "${email}_$level",
                bestPercent = maxOf(percent, existing?.bestPercent ?: 0),
                bestScore = maxOf(state.score, existing?.bestScore ?: 0),
                totalQuestions = state.totalQuestions,
                attempts = (existing?.attempts ?: 0) + 1,
                passed = percent >= 70 || existing?.passed == true,
                lastAttemptAt = Instant.now().toString(),
                bestAttemptDetailsJson = detailsJson,
            )
            testRepository.saveProgress(entity)

            // Update UI
            val map = _uiState.value.levelProgress.toMutableMap()
            map[level] = entity
            _uiState.update { it.copy(levelProgress = map) }

            // Sync to server
            syncAllToServer(email, map)
        }
    }

    private suspend fun syncAllToServer(email: String, map: Map<Int, TestProgressEntity>) {
        try {
            val testItems = map.map { (level, entity) ->
                val details = entity.bestAttemptDetailsJson?.let { raw ->
                    try {
                        json.decodeFromString<List<TestAttemptDetail>>(raw)
                    } catch (_: Exception) { null }
                }
                TestProgressSyncItem(
                    levelId = "hsk$level",
                    bestPercent = entity.bestPercent,
                    bestScore = entity.bestScore,
                    totalQuestions = entity.totalQuestions,
                    attempts = entity.attempts,
                    passed = entity.passed,
                    lastAttemptAt = entity.lastAttemptAt,
                    bestAttemptDetails = details,
                )
            }
            progressApi.syncProgress(ProgressSyncRequest(tests = testItems, vocabulary = emptyList()))
            if (BuildConfig.DEBUG) android.util.Log.d("TestVM", "Test progress synced to server: ${testItems.size} levels")
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.w("TestVM", "Test sync failed: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
