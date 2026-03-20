package com.subnetik.unlock.presentation.screens.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.local.db.entity.TestProgressEntity
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
    val isDarkTheme: Boolean? = null,
    val showTrialButton: Boolean = false,
)

@HiltViewModel
class TestViewModel @Inject constructor(
    private val testRepository: TestRepository,
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
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
                _uiState.update { it.copy(showTrialButton = role == com.subnetik.unlock.domain.model.AppUserRole.USER || role == com.subnetik.unlock.domain.model.AppUserRole.GUEST) }
            }
        }
        loadAllProgress()
    }

    private fun loadAllProgress() {
        viewModelScope.launch {
            val email = authRepository.getUserEmail().first() ?: return@launch
            val progress = testRepository.getAllProgress(email)
            val map = mutableMapOf<Int, TestProgressEntity>()
            progress.forEach { entity ->
                val levelStr = entity.key.removePrefix("${email}_")
                val level = levelStr.toIntOrNull() ?: return@forEach
                map[level] = entity
            }
            _uiState.update { it.copy(levelProgress = map) }
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
        val isCorrect = question.answers.find { it.id == answerId }?.isCorrect == true
        _uiState.update {
            it.copy(
                selectedAnswerId = answerId,
                answerRevealed = true,
                score = if (isCorrect) it.score + 1 else it.score,
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

            val entity = TestProgressEntity(
                key = "${email}_$level",
                bestPercent = maxOf(percent, existing?.bestPercent ?: 0),
                bestScore = maxOf(state.score, existing?.bestScore ?: 0),
                totalQuestions = state.totalQuestions,
                attempts = (existing?.attempts ?: 0) + 1,
                passed = percent >= 70 || existing?.passed == true,
                lastAttemptAt = Instant.now().toString(),
            )
            testRepository.saveProgress(entity)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
