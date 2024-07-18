package com.ocrtts.history

import android.content.Context
import android.util.Log
import androidx.compose.ui.unit.IntSize
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ocrtts.history.DataStoreManager.Companion.HEIGHT_KEY
import com.ocrtts.history.DataStoreManager.Companion.WIDTH_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.suspendCoroutine

private val Context.dataStore by preferencesDataStore(name = "image_history")
//private val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    companion object {
        private val IMAGE_HISTORY_KEY = stringSetPreferencesKey("image_history")
        private const val MAX_HISTORY_SIZE = 10

        // setting variable
        val LANG_MODEL_KEY = stringPreferencesKey("langModel")
        val SPEED_RATE_KEY = floatPreferencesKey("speedRate")
        val MODEL_TYPE_KEY = stringPreferencesKey("modelType")

        // screen size
        val WIDTH_KEY = intPreferencesKey("screenWidth")
        val HEIGHT_KEY = intPreferencesKey("screenHeight")
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
            if (currentHistory.size > MAX_HISTORY_SIZE) {
                // Remove the oldest entries to maintain the limit
//                for (i in 0..<currentHistory.size - MAX_HISTORY_SIZE) {
//                    removeImageFromHistory(currentHistory[i])
//                }
                currentHistory.subList(0, currentHistory.size - MAX_HISTORY_SIZE).clear()
            }
            preferences[IMAGE_HISTORY_KEY] = currentHistory.toSet()
        }
    }

    private suspend fun removeImageFromHistory(filePath: String) {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[IMAGE_HISTORY_KEY] ?: emptySet()
            preferences[IMAGE_HISTORY_KEY] = currentHistory - filePath
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