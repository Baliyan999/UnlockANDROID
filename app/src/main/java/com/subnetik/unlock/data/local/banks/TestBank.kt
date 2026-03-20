package com.subnetik.unlock.data.local.banks

import android.content.Context
import com.subnetik.unlock.domain.model.TestQuestion
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class TestBank @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    private val cache = mutableMapOf<Int, List<TestQuestion>>()

    fun getQuestions(level: Int): List<TestQuestion> {
        return cache.getOrPut(level) {
            try {
                val fileName = "tests/hsk${level}_questions.json"
                val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
                json.decodeFromString<List<TestQuestion>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
