package com.subnetik.unlock.domain.repository

import com.subnetik.unlock.data.local.db.entity.TestProgressEntity
import com.subnetik.unlock.domain.model.Resource
import com.subnetik.unlock.domain.model.TestQuestion

interface TestRepository {
    fun getQuestions(level: Int): List<TestQuestion>
    fun getRandomQuestions(level: Int, count: Int = 10): List<TestQuestion>
    suspend fun saveProgress(entity: TestProgressEntity)
    suspend fun getProgress(email: String, levelId: String): TestProgressEntity?
    suspend fun getAllProgress(email: String): List<TestProgressEntity>
}
