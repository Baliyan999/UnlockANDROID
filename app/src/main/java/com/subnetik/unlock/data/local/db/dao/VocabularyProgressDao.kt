package com.subnetik.unlock.data.local.db.dao

import androidx.room.*
import com.subnetik.unlock.data.local.db.entity.VocabularyProgressEntity

@Dao
interface VocabularyProgressDao {
    @Query("SELECT * FROM vocabulary_progress")
    suspend fun getAll(): List<VocabularyProgressEntity>

    @Query("SELECT * FROM vocabulary_progress WHERE `key` = :key")
    suspend fun getByKey(key: String): VocabularyProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VocabularyProgressEntity)

    @Query("DELETE FROM vocabulary_progress")
    suspend fun deleteAll()
}
