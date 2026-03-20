package com.subnetik.unlock.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.subnetik.unlock.data.local.db.dao.TestProgressDao
import com.subnetik.unlock.data.local.db.dao.VocabularyProgressDao
import com.subnetik.unlock.data.local.db.entity.TestProgressEntity
import com.subnetik.unlock.data.local.db.entity.VocabularyProgressEntity

@Database(
    entities = [TestProgressEntity::class, VocabularyProgressEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class UnlockDatabase : RoomDatabase() {
    abstract fun testProgressDao(): TestProgressDao
    abstract fun vocabularyProgressDao(): VocabularyProgressDao
}
