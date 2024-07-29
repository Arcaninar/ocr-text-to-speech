package com.ocrtts.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ocrtts.history.SettingDataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingViewModel(private val dataStoreManager: SettingDataStoreManager) : ViewModel() {

    private val _langModel = MutableStateFlow("en-GB-SoniaNeural")
    val langModel: StateFlow<String> = _langModel

    private val _speedRate = MutableStateFlow(1.0f)
    val speedRate: StateFlow<Float> = _speedRate

    private val _modelType = MutableStateFlow("offlineTTS")
    val modelType: StateFlow<String> = _modelType

    fun setmodeltype(){}

    init {
        viewModelScope.launch {
            dataStoreManager.langModelFlow.collectLatest {
                _langModel.value = it
                Log.i("set", "langModel: ${_langModel.value}")
            }
        }
        viewModelScope.launch {
            dataStoreManager.speedRateFlow.collectLatest {
                _speedRate.value = it
//                speedSetting = it
                Log.i("set", "speedRate: ${_speedRate.value}")
            }
        }
        viewModelScope.launch {
            dataStoreManager.modelTypeFlow.collectLatest {
                _modelType.value = it
                Log.i("set", "modeltype: ${_modelType.value}")
            }
        }
    }

    fun updateLangModel(newLangModel: String) {
        _langModel.value = newLangModel
        viewModelScope.launch {
            dataStoreManager.updateLangModel(newLangModel)
            Log.i("update", newLangModel)
        }
    }

    fun updateSpeedRate(newSpeedRate: Float) {
        _speedRate.value = newSpeedRate
        viewModelScope.launch {
            dataStoreManager.updateSpeedRate(newSpeedRate)
            Log.i("update", newSpeedRate.toString())
        }
    }

    fun updateModelType(newModelType: String) {
        _modelType.value = newModelType

    }
}
