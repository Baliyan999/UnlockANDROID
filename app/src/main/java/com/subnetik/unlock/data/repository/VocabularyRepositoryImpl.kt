package com.subnetik.unlock.data.repository

import com.subnetik.unlock.data.local.banks.VocabularyBank
import com.subnetik.unlock.data.local.db.dao.VocabularyProgressDao
import com.subnetik.unlock.data.local.db.entity.VocabularyProgressEntity
import com.subnetik.unlock.domain.model.VocabularyWord
import com.subnetik.unlock.domain.repository.VocabularyRepository
import javax.inject.Inject

class VocabularyRepositoryImpl @Inject constructor(
    private val vocabularyBank: VocabularyBank,
    private val vocabularyProgressDao: VocabularyProgressDao,
) : VocabularyRepository {

    override fun getWords(level: Int): List<VocabularyWord> {
        return vocabularyBank.getWords(level)
    }

    override fun searchWords(level: Int, query: String): List<VocabularyWord> {
        val words = vocabularyBank.getWords(level)
        if (query.isBlank()) return words
        val q = query.lowercase()
        return words.filter {
            it.character.contains(q) ||
            it.pinyin.lowercase().contains(q) ||
            it.translation.lowercase().contains(q)
        }
    }

    override suspend fun getProgress(email: String, level: Int): VocabularyProgressEntity? {
        return vocabularyProgressDao.getByKey("${email}_$level")
    }

    override suspend fun saveProgress(entity: VocabularyProgressEntity) {
        vocabularyProgressDao.insert(entity)
    }

    override suspend fun getAllProgress(email: String): List<VocabularyProgressEntity> {
        return vocabularyProgressDao.getAll().filter { it.key.startsWith("${email}_") }
    }
}
