package com.subnetik.unlock.data.repository

import com.subnetik.unlock.data.local.banks.TestBank
import com.subnetik.unlock.data.local.db.dao.TestProgressDao
import com.subnetik.unlock.data.local.db.entity.TestProgressEntity
import com.subnetik.unlock.domain.model.TestQuestion
import com.subnetik.unlock.domain.repository.TestRepository
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor(
    private val testBank: TestBank,
    private val testProgressDao: TestProgressDao,
) : TestRepository {

    override fun getQuestions(level: Int): List<TestQuestion> {
        return testBank.getQuestions(level)
    }

    override fun getRandomQuestions(level: Int, count: Int): List<TestQuestion> {
        val allQuestions = testBank.getQuestions(level)
        return allQuestions.shuffled().take(count).map { question ->
            question.copy(answers = question.answers.shuffled())
        }
    }

    override suspend fun saveProgress(entity: TestProgressEntity) {
        testProgressDao.insert(entity)
    }

    override suspend fun getProgress(email: String, levelId: String): TestProgressEntity? {
        return testProgressDao.getByKey("${email}_$levelId")
    }

    override suspend fun getAllProgress(email: String): List<TestProgressEntity> {
        return testProgressDao.getAll().filter { it.key.startsWith("${email}_") }
    }
}
