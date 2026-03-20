package com.subnetik.unlock.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary_progress")
data class VocabularyProgressEntity(
    @PrimaryKey val key: String, // "{email}_{level}"
    val knownWordIdsJson: String, // JSON array of word IDs
    val reviewWordIdsJson: String, // JSON array of word IDs
    val lastStudiedAt: String? = null,
)
