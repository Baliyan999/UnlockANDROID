package com.subnetik.unlock.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TestQuestion(
    val id: String,
    val level: Int,
    val prompt: String,
    val answers: List<TestAnswer>,
    val explanation: String? = null,
)

@Serializable
data class TestAnswer(
    val id: String,
    val text: String,
    @SerialName("isCorrect") val isCorrect: Boolean,
)
