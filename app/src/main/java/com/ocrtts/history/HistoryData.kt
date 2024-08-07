package com.ocrtts.history

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import android.util.Log

private val Context.dataStore by preferencesDataStore(name = "image_history")
class DataStoreManager(private val context: Context) {

    companion object {
        private val IMAGE_HISTORY_KEY = stringSetPreferencesKey("image_history")
    }

    val imageHistory: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[IMAGE_HISTORY_KEY] ?: emptySet()
        }

    suspend fun addImageToHistory(filePath: String) {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[IMAGE_HISTORY_KEY]?.toMutableList() ?: mutableListOf()
            currentHistory.add(filePath)
            preferences[IMAGE_HISTORY_KEY] = currentHistory.toSet()
        }
    }

    suspend fun removeImageFromHistory(filePath: String) {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[IMAGE_HISTORY_KEY] ?: emptySet()
            preferences[IMAGE_HISTORY_KEY] = currentHistory - filePath
        }
    }

    suspend fun updateImageHistory() {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[IMAGE_HISTORY_KEY] ?: emptySet()
            val filteredHistory = currentHistory.filter { filePath ->
                val fileExists = File(filePath).exists()
                if (!fileExists) {
                    Log.d("DataStoreManager", "File does not exist: $filePath")
                }
                fileExists
            }.toSet()
            preferences[IMAGE_HISTORY_KEY] = filteredHistory
        }
    }
}