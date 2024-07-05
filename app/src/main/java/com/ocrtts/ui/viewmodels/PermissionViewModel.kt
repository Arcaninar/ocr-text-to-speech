import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ocrtts.data.DataStoreManager.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.State

private const val PREFS_KEY_DENY_COUNT = "deny_count"
const val MAX_DENY_COUNT = 2

class PermissionViewModel(context: Context) : ViewModel() {
//    private val _isPermissionGranted = MutableStateFlow(false)
//    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()
//    fun updatePermissionStatus(isGranted: Boolean) {
//        _isPermissionGranted.value = isGranted
//    }

    private val _cameraPermissionState = mutableStateOf<Boolean?>(null)
    val cameraPermissionState: State<Boolean?> = _cameraPermissionState

    fun updateCameraPermissionStatus(isGranted: Boolean) {
        _cameraPermissionState.value = isGranted
    }

    private val dataStore: DataStore<Preferences> = context.dataStore
    companion object {
        private val DENY_COUNT_KEY = intPreferencesKey(PREFS_KEY_DENY_COUNT)
        const val MAX_DENY_COUNT = 2
    }

    val denyCount: StateFlow<Int> = dataStore.data
        .map { preferences ->
            preferences[DENY_COUNT_KEY] ?: 0
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun incrementDenyCount() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val currentCount = preferences[DENY_COUNT_KEY] ?: 0
                preferences[DENY_COUNT_KEY] = currentCount + 1
            }
        }
    }

    fun resetDenyCount() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DENY_COUNT_KEY] = 0
            }
        }
    }

}

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PermissionViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}