package com.ocrtts.ui.viewmodels
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


private const val PREFS_NAME = "camera_prefs"
private const val PREFS_KEY_DENY_COUNT = "deny_count"
const val MAX_DENY_COUNT = 2

class PermissionViewModel(context: Context) : ViewModel() {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    var denyCount by mutableIntStateOf(sharedPreferences.getInt(PREFS_KEY_DENY_COUNT, 0))
        private set
    fun incrementDenyCount() {
        denyCount++
        viewModelScope.launch {
            sharedPreferences.edit().putInt(PREFS_KEY_DENY_COUNT, denyCount).apply()
        }
    }
    fun resetDenyCount() {
        denyCount = 0
        viewModelScope.launch {
            sharedPreferences.edit().putInt(PREFS_KEY_DENY_COUNT, denyCount).apply()
        }
    }
    fun setToMaxDenyCount() {
        denyCount = MAX_DENY_COUNT
        viewModelScope.launch {
            sharedPreferences.edit().putInt(PREFS_KEY_DENY_COUNT, denyCount).apply()
        }
    }
}

class PermissionViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PermissionViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}