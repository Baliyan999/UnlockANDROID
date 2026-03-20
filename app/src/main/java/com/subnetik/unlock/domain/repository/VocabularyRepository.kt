package com.subnetik.unlock.domain.repository

import com.subnetik.unlock.data.local.db.entity.VocabularyProgressEntity
import com.subnetik.unlock.domain.model.VocabularyWord

interface VocabularyRepository {
    fun getWords(level: Int): List<VocabularyWord>
    fun searchWords(level: Int, query: String): List<VocabularyWord>
    suspend fun getProgress(email: String, level: Int): VocabularyProgressEntity?
    suspend fun saveProgress(entity: VocabularyProgressEntity)
    suspend fun getAllProgress(email: String): List<VocabularyProgressEntity>
}
