package com.subnetik.unlock.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_progress")
data class TestProgressEntity(
    @PrimaryKey val key: String, // "{email}_{levelId}"
    val bestPercent: Int,
    val bestScore: Int,
    val totalQuestions: Int,
    val attempts: Int,
    val passed: Boolean,
    val lastAttemptAt: String? = null,
    val bestAttemptDetailsJson: String? = null,
)
