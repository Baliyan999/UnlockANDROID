package com.subnetik.unlock.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VocabularyWord(
    val id: String,
    val level: Int = 0,
    val character: String,
    val pinyin: String,
    val translation: String,
)
