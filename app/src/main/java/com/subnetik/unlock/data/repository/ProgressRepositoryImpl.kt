package com.subnetik.unlock.data.repository

import com.subnetik.unlock.data.local.datastore.AuthDataStore
import com.subnetik.unlock.data.local.db.dao.TestProgressDao
import com.subnetik.unlock.data.local.db.dao.VocabularyProgressDao
import com.subnetik.unlock.data.remote.api.ProgressApi
import com.subnetik.unlock.data.remote.dto.progress.ProgressSyncRequest
import com.subnetik.unlock.data.remote.dto.progress.TestProgressSyncItem
import com.subnetik.unlock.data.remote.dto.progress.VocabProgressSyncItem
import com.subnetik.unlock.domain.model.Resource
import com.subnetik.unlock.domain.repository.ProgressRepository
import com.subnetik.unlock.util.ErrorMapper
import com.subnetik.unlock.util.NetworkMonitor
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val progressApi: ProgressApi,
    private val testProgressDao: TestProgressDao,
    private val vocabularyProgressDao: VocabularyProgressDao,
    private val authDataStore: AuthDataStore,
    private val networkMonitor: NetworkMonitor,
    private val json: Json,
) : ProgressRepository {

    override suspend fun syncProgress(): Resource<Unit> {
        if (!networkMonitor.isOnline()) return Resource.Error("Нет подключения к интернету")

        val email = authDataStore.email.first() ?: return Resource.Error("Сессия истекла. Войдите снова")

        return try {
            val testProgress = testProgressDao.getAll()
                .filter { it.key.startsWith("${email}_") }
                .map { entity ->
                    TestProgressSyncItem(
                        levelId = entity.key.removePrefix("${email}_"),
                        bestPercent = entity.bestPercent,
                        bestScore = entity.bestScore,
                        totalQuestions = entity.totalQuestions,
                        attempts = entity.attempts,
                        passed = entity.passed,
                        lastAttemptAt = entity.lastAttemptAt,
                        bestAttemptDetails = null
                    )
                }

            val vocabProgress = vocabularyProgressDao.getAll()
                .filter { it.key.startsWith("${email}_") }
                .map { entity ->
                    val knownIds = try {
                        json.decodeFromString<List<String>>(entity.knownWordIdsJson)
                    } catch (_: Exception) { emptyList() }
                    val reviewIds = try {
                        json.decodeFromString<List<String>>(entity.reviewWordIdsJson)
                    } catch (_: Exception) { emptyList() }

                    VocabProgressSyncItem(
                        level = entity.key.removePrefix("${email}_").toIntOrNull() ?: 1,
                        totalWords = knownIds.size + reviewIds.size,
                        knownCount = knownIds.size,
                        reviewCount = reviewIds.size,
                        knownWordIds = knownIds,
                        reviewWordIds = reviewIds,
                        lastStudiedAt = entity.lastStudiedAt
                    )
                }

            progressApi.syncProgress(
                ProgressSyncRequest(tests = testProgress, vocabulary = vocabProgress)
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(ErrorMapper.map(e))
        }
    }
}
