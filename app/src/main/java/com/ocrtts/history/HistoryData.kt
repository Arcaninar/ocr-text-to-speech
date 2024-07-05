package com.ocrtts.history

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "image_history")

class DataStoreManager(private val context: Context) {

    companion object {
        private val IMAGE_HISTORY_KEY = stringSetPreferencesKey("image_history")
        private const val MAX_HISTORY_SIZE = 10
    }

    val imageHistory: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[IMAGE_HISTORY_KEY] ?: emptySet()
        }

    suspend fun addImageToHistory(filePath: String) {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[IMAGE_HISTORY_KEY]?.toMutableList() ?: mutableListOf()
            currentHistory.add(filePath)
            if (currentHistory.size > MAX_HISTORY_SIZE) {
                // Remove the oldest entries to maintain the limit
                currentHistory.subList(0, currentHistory.size - MAX_HISTORY_SIZE).clear()
            }
            preferences[IMAGE_HISTORY_KEY] = currentHistory.toSet()
        }
    }

    suspend fun removeImageFromHistory(filePath: String) {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[IMAGE_HISTORY_KEY] ?: emptySet()
            preferences[IMAGE_HISTORY_KEY] = currentHistory - filePath
        }
    }
}