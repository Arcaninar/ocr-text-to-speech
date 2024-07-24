package com.ocrtts.history

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingDataStoreManager(private val context: Context) {

    companion object {
        val LANG_MODEL_KEY = stringPreferencesKey("langModel")
        val SPEED_RATE_KEY = floatPreferencesKey("speedRate")
        val MODEL_TYPE_KEY = stringPreferencesKey("modelType")
    }

    val langModelFlow: Flow<String> = context.settingsDataStore.data
        .map { preferences ->
            preferences[LANG_MODEL_KEY] ?: "en-US"
        }

    val speedRateFlow: Flow<Float> = context.settingsDataStore.data
        .map { preferences ->
            preferences[SPEED_RATE_KEY] ?: 1.0f
        }

    val modelTypeFlow: Flow<String> = context.settingsDataStore.data
        .map { preferences ->
            preferences[MODEL_TYPE_KEY] ?: "offlineTTS"
        }

    suspend fun updateLangModel(newLangModel: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANG_MODEL_KEY] = newLangModel
        }
    }

    suspend fun updateSpeedRate(newSpeedRate: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[SPEED_RATE_KEY] = newSpeedRate
        }
    }

    suspend fun updateModelType(newModelType: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[MODEL_TYPE_KEY] = newModelType
        }
    }
}
