package com.ocrtts.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ocrtts.history.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {

    private val _langModel = MutableStateFlow("")
    val langModel: StateFlow<String> = _langModel

    private val _speedRate = MutableStateFlow(1.0f)
    val speedRate: StateFlow<Float> = _speedRate

    private val _modelType = MutableStateFlow("offlineTTS")
    val modelType: StateFlow<String> = _modelType

    init {
        viewModelScope.launch {
            dataStoreManager.langModelFlow.collectLatest {
                _langModel.value = it
                Log.i("set", "langModel: $_langModel")
            }
        }
        viewModelScope.launch {
            dataStoreManager.speedRateFlow.collectLatest {
                _speedRate.value = it
                Log.i("set", "speedRate: $_speedRate")
            }
        }
        viewModelScope.launch {
            dataStoreManager.modelTypeFlow.collectLatest {
                _modelType.value = it
                Log.i("set", "modeltype: $_modelType")
            }
        }
    }

    fun updateLangModel(newLangModel: String) {
        viewModelScope.launch {
            dataStoreManager.updateLangModel(newLangModel)
        }
    }

    fun updateSpeedRate(newSpeedRate: Float) {
        viewModelScope.launch {
            dataStoreManager.updateSpeedRate(newSpeedRate)
        }
    }

    fun updateModelType(newModelType: String) {
        viewModelScope.launch {
            dataStoreManager.updateModelType(newModelType)
        }
    }
}
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.ocrtts.history.DataStoreManager
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.collect
//import kotlinx.coroutines.launch
//
//class SettingViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
//
//    private val _langModel = MutableStateFlow("")
//    val langModel: StateFlow<String> = _langModel
//
//    private val _speedRate = MutableStateFlow(1.0f)
//    val speedRate: StateFlow<Float> = _speedRate
//
//    private val _modelType = MutableStateFlow("offlineTTS")
//    val modelType: StateFlow<String> = _modelType
//
//    init {
//        viewModelScope.launch {
//            dataStoreManager.langModelFlow.collect {
//                _langModel.value = it
//            }
//        }
//        viewModelScope.launch {
//            dataStoreManager.speedRateFlow.collect {
//                _speedRate.value = it
//            }
//        }
//        viewModelScope.launch {
//            dataStoreManager.modelTypeFlow.collect {
//                _modelType.value = it
//            }
//        }
//    }
//
//    fun updateLangModel(newLangModel: String) {
//        viewModelScope.launch {
//            dataStoreManager.updateLangModel(newLangModel)
//        }
//    }
//
//    fun updateSpeedRate(newSpeedRate: Float) {
//        viewModelScope.launch {
//            dataStoreManager.updateSpeedRate(newSpeedRate)
//        }
//    }
//
//    fun updateModelType(newModelType: String) {
//        viewModelScope.launch {
//            dataStoreManager.updateModelType(newModelType)
//        }
//    }
//}
