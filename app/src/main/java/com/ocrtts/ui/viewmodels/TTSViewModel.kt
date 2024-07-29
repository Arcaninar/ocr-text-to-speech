package com.ocrtts.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ocrtts.base.AzureTextSynthesis
import com.ocrtts.base.OfflineTextSynthesis
import com.ocrtts.history.SettingDataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.apache.http.TruncatedChunkException
import java.util.Locale

class TTSViewModel(
    application: Application,
    private var initialLanguage: String,
    private val initialSpeed: Float,
    private val dataStoreManager: SettingDataStoreManager

    ) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    val offlineTTS = OfflineTextSynthesis(context)
    var azureTTS: AzureTextSynthesis? = null
    var isOnline: Boolean = false

    var showDialog = mutableStateOf(false)//init
//    val model: MutableState<String> = mutableStateOf("")

    init {
        isOnline = checkNetworkAvailability(context)
        setupTTS()
    }

    private fun setupTTS() {
        if (isOnline) {
            azureTTS = AzureTextSynthesis(initialLanguage)
        } else {
            offlineTTS.setLanguage(Locale.forLanguageTag(initialLanguage))
        }
    }

    fun updateLanguage(newLanguage: String) {
        initialLanguage = newLanguage
        if (isOnline) {
            azureTTS?.updateVoice(newLanguage)
        } else {
            offlineTTS.setLanguage(Locale.forLanguageTag(newLanguage))
        }
    }

    private fun checkNetworkAvailability(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    suspend fun speak(text: String, modelTy: String, speed: Float) {
        if (isOnline) {
            if (modelTy == "onlineTTS") {
                azureTTS?.stopSynthesis()
                azureTTS?.startPlaying(text, speed)
            } else {
//                azureTTS = null
                azureTTS?.stopSynthesis()
                offlineTTS.speak(text)
            }
        } else {
            if (modelTy != "offlineTTS") {
                showDialog.value = true
                viewModelScope.launch {
                    dataStoreManager.updateModelType("offlineTTS")
                    Log.i("update", "auto-change to offlineTTS")
                }
                offlineTTS.speak(text)
            } else {
                offlineTTS.speak(text)
            }
// else still need to optimization
            //check setting
            //Setting UI ->offline
            //Alert box
            //OfflineSpeak

        }
    }

//    suspend fun speek(settingViewModel: SettingViewModel) {
//        model.value = settingViewModel.modelType.first()
//    }
//    fun speak(text: String, speed: Float) {
//        if (isOnline && azureTTS != null) {
//            azureTTS!!.stopSynthesis()
//            azureTTS!!.startPlaying(text, speed)
//        } else {
//            offlineTTS.speak(text)
//        }
//    }

    fun stopAllTTS() {
        azureTTS?.stopSynthesis()
        offlineTTS.shutdown()
    }

    override fun onCleared() {
        super.onCleared()
        azureTTS?.destroy()
        offlineTTS.shutdown()
    }
}

//import android.app.Application
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//import androidx.lifecycle.AndroidViewModel
//import com.ocrtts.base.AzureTextSynthesis
//import com.ocrtts.base.OfflineTextSynthesis
//import java.util.Locale
//
//import androidx.lifecycle.viewModelScope
//import com.ocrtts.history.SettingDataStoreManager
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//
//class TTSViewModel(
//    application: Application,
////    private var initialLanguage: String,
////    private val initialSpeed: Float,
//    private val dataStoreManager: SettingDataStoreManager
//
//) : AndroidViewModel(application) {
//    private val context = getApplication<Application>().applicationContext
//    private val offlineTTS = OfflineTextSynthesis(context)
//    private var azureTTS: AzureTextSynthesis? = null
//    private var isOnline: Boolean = false
//
//    init {
////        isOnline = checkNetworkAvailability(context)
//        observeSettings()
//        setupTTS()
//    }
//
//    private fun observeSettings() {
//        viewModelScope.launch {
//            dataStoreManager.modelTypeFlow.collectLatest { modelType ->
//                isOnline = checkNetworkAvailability(context)
//                val langModel = dataStoreManager.langModelFlow.first()
//                if (isOnline) {
//                    if (modelType == "onlineTTS") {
//                        azureTTS = AzureTextSynthesis(langModel)
//                    } else {
//                        azureTTS = null
//                        offlineTTS.setLanguage(Locale.forLanguageTag(langModel))
//                    }
//                } els e {
//                    azureTTS = null
//                    offlineTTS.setLanguage(Locale.forLanguageTag(langModel))
//                }
//            }
//        }
//    }
//
//
////    private fun setupTTS() {
////        if (isOnline) {
////            azureTTS = AzureTextSynthesis(initialLanguage)
////        } else {
////            offlineTTS.setLanguage(Locale.forLanguageTag(initialLanguage))
////        }
////    }
//
//    private fun setupTTS() {
//        // Initial setup based on the current network state and settings
//        isOnline = checkNetworkAvailability(context)
//        viewModelScope.launch {
//            val modelType = dataStoreManager.modelTypeFlow.first()
//            val langModel = dataStoreManager.langModelFlow.first()
//            if (isOnline && modelType == "onlineTTS") {
//                azureTTS = AzureTextSynthesis(langModel)
//            } else {
//                azureTTS = null
//                offlineTTS.setLanguage(Locale.forLanguageTag(langModel))
//            }
//        }
//    }
//
//
////    fun updateLanguage(newLanguage: String) {
////        initialLanguage = newLanguage
////        if (isOnline) {
////            azureTTS?.updateVoice(newLanguage)
////        } else {
////            offlineTTS.setLanguage(Locale.forLanguageTag(newLanguage))
////        }
////    }
//
//    fun updateLanguage(newLanguage: String) {
//        viewModelScope.launch {
//            if (isOnline) {
//                val modelType = dataStoreManager.modelTypeFlow.first()
//                if (modelType == "onlineTTS") {
//                    azureTTS?.updateVoice(newLanguage)
//                }
//            }
//            offlineTTS.setLanguage(Locale.forLanguageTag(newLanguage))
//        }
//    }
//
//    private fun checkNetworkAvailability(context: Context): Boolean {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork ?: return false
//        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
//        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
//    }
//
////    fun speak(text: String, speed: Float) {
////        if (isOnline && azureTTS != null) {
////            azureTTS!!.stopSynthesis()
////            azureTTS!!.startPlaying(text, speed)
////        } else {
////            offlineTTS.speak(text)
////        }
////    }
//
//    fun speak(text: String) {
//        viewModelScope.launch {
//            val speed = dataStoreManager.speedRateFlow.first()
//            val modelType = dataStoreManager.modelTypeFlow.first()
//            if (isOnline && modelType == "onlineTTS" && azureTTS != null) {
//                azureTTS!!.stopSynthesis()
//                azureTTS!!.startPlaying(text, speed)
//            } else {
//                offlineTTS.speak(text)
//            }
//        }
//    }
//
//    fun stopAllTTS() {
//        azureTTS?.stopSynthesis()
//        offlineTTS.shutdown()
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        azureTTS?.destroy()
//        offlineTTS.shutdown()
//    }
//}