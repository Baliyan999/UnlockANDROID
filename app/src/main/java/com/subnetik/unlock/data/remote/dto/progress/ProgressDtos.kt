package com.subnetik.unlock.data.remote.dto.progress

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProgressSyncRequest(
    val tests: List<TestProgressSyncItem>,
    val vocabulary: List<VocabProgressSyncItem>,
)

@Serializable
data class TestProgressSyncItem(
    @SerialName("level_id") val levelId: String,
    @SerialName("best_percent") val bestPercent: Int,
    @SerialName("best_score") val bestScore: Int,
    @SerialName("total_questions") val totalQuestions: Int,
    val attempts: Int,
    val passed: Boolean,
    @SerialName("last_attempt_at") val lastAttemptAt: String? = null,
    @SerialName("best_attempt_details") val bestAttemptDetails: List<TestAttemptDetail>? = null,
)

@Serializable
data class TestAttemptDetail(
    @SerialName("question_id") val questionId: String,
    @SerialName("selected_answer") val selectedAnswer: String,
    val correct: Boolean,
)

@Serializable
data class VocabProgressSyncItem(
    val level: Int,
    @SerialName("total_words") val totalWords: Int,
    @SerialName("known_count") val knownCount: Int,
    @SerialName("review_count") val reviewCount: Int,
    @SerialName("last_studied_at") val lastStudiedAt: String? = null,
)

@Serializable
data class StudentFullProgressResponse(
    @SerialName("user_id") val userId: Int,
    val tests: List<TestProgressSyncItem>,
    val vocabulary: List<VocabProgressSyncItem>,
)
