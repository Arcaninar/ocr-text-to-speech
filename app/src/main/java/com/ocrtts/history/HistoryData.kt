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

private val Context.dataStore by preferencesDataStore(name = "image_history")
//private val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    companion object {
        private val IMAGE_HISTORY_KEY = stringSetPreferencesKey("image_history")
       

        //setting variable
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