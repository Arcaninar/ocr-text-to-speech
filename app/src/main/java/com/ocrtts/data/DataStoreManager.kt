package com.ocrtts.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val PREFS_NAME = "settings"

object DataStoreManager {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)

//    fun getDataStore(context: Context): DataStore<Preferences> {
//        return context.dataStore
//    }
}