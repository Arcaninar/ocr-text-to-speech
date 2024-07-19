package com.ocrtts.history

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import android.util.Log

private val Context.dataStore by preferencesDataStore(name = "image_history")

class DataStoreManager(private val context: Context) {

    companion object {
        private val IMAGE_HISTORY_KEY = stringSetPreferencesKey("image_history")

        // Setting variables
        val LANG_MODEL_KEY = stringPreferencesKey("langModel")
        val SPEED_RATE_KEY = floatPreferencesKey("speedRate")
        val MODEL_TYPE_KEY = stringPreferencesKey("modelType")
    }

    val imageHistory: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[IMAGE_HISTORY_KEY] ?: emptySet()
        }

    val langModelFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANG_MODEL_KEY] ?: ""
        }

    val speedRateFlow: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[SPEED_RATE_KEY] ?: 1.0f
        }

    val modelTypeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[MODEL_TYPE_KEY] ?: "offlineTTS"
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

    suspend fun updateLangModel(newLangModel: String) {
        context.dataStore.edit { preferences ->
            preferences[LANG_MODEL_KEY] = newLangModel
        }
    }

    suspend fun updateSpeedRate(newSpeedRate: Float) {
        context.dataStore.edit { preferences ->
            preferences[SPEED_RATE_KEY] = newSpeedRate
        }
    }

    suspend fun updateModelType(newModelType: String) {
        context.dataStore.edit { preferences ->
            preferences[MODEL_TYPE_KEY] = newModelType
        }
    }
}