package com.subnetik.unlock.data.local.db.dao

import androidx.room.*
import com.subnetik.unlock.data.local.db.entity.TestProgressEntity

@Dao
interface TestProgressDao {
    @Query("SELECT * FROM test_progress")
    suspend fun getAll(): List<TestProgressEntity>

    @Query("SELECT * FROM test_progress WHERE `key` = :key")
    suspend fun getByKey(key: String): TestProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TestProgressEntity)

    @Query("DELETE FROM test_progress")
    suspend fun deleteAll()
}
