package com.subnetik.unlock.data.local.banks

import android.content.Context
import com.subnetik.unlock.domain.model.VocabularyWord
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class VocabularyBank @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    private val cache = mutableMapOf<Int, List<VocabularyWord>>()

    fun getWords(level: Int): List<VocabularyWord> {
        return cache.getOrPut(level) {
            try {
                val fileName = "vocabulary/hsk${level}_words.json"
                val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
                json.decodeFromString<List<VocabularyWord>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
