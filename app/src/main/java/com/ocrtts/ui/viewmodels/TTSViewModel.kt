package com.ocrtts.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import com.ocrtts.base.AzureTextSynthesis
import com.ocrtts.base.OfflineTextSynthesis
import java.util.Locale

class TTSViewModel(
    application: Application,
    private var initialLanguage: String,
    private val initialSpeed: Float,

) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    val offlineTTS = OfflineTextSynthesis(context)
    var azureTTS: AzureTextSynthesis? = null
    var isOnline: Boolean = false

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

//    init {
//        isOnline = checkNetworkAvailability(context)
//        if (isOnline) {
//            azureTTS = AzureTextSynthesis(initialLanguage)
//        }
//    }

    private fun checkNetworkAvailability(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

//    fun updateLanguage(language: String) {
//        if (isOnline) {
//            azureTTS?.updateVoice(language)
//        }else {
////            offlineTTS
//        }
//    }

    fun speak(text: String, speed: Float) {
        if (isOnline && azureTTS != null) {
            azureTTS?.stopSynthesis()
            azureTTS?.startPlaying(text, speed)
        } else {
            offlineTTS.speak(text)
        }
    }

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


// 原本想在这里设置checknetwork

//package com.ocrtts.ui.viewmodels
//
//import android.app.AlertDialog
//import android.app.Application
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.Network
//import android.net.NetworkCapabilities
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.platform.LocalContext
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import androidx.navigation.NavController
//import com.ocrtts.base.AzureTextSynthesis
//import com.ocrtts.base.OfflineTextSynthesis
//import com.ocrtts.ui.screens.Screens
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//class TTSViewModel(
//    application: Application,
//    private val initialLanguage: String,
//    private val initialSpeed: Float
//
//) : AndroidViewModel(application) {
//    private val context = getApplication<Application>().applicationContext
//    val offlineTTS = OfflineTextSynthesis(context)
//    var azureTTS: AzureTextSynthesis? = null
//
//    private val _isOnline = MutableStateFlow(checkNetworkAvailability(context))
//    val isOnline: StateFlow<Boolean> = _isOnline
//
//    //    init {
////        isOnline = checkNetworkAvailability(context)
////        if (isOnline) {
////            azureTTS = AzureTextSynthesis("zh-HK-HiuMaanNeural")
////            azureTTS = AzureTextSynthesis(initialLanguage)
////        }
////    }
//    init {
//        if (_isOnline.value) {
//            azureTTS = AzureTextSynthesis(initialLanguage)
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
//    fun updateLanguage(language: String) {
//        if (isOnline.value) {
//            azureTTS?.updateVoice(language)
//        }else {
//
//            // Handle offline TTS language update if necessary
//
//        }
//    }
//
//    fun speak(text: String, speed: Float) {
//        if (_isOnline.value && azureTTS != null) {
//            azureTTS?.stopSynthesis()
//            azureTTS?.startPlaying(text, speed)
//        } else {
//            offlineTTS.speak(text)
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
//
//    fun setOnlineStatus(isOnline: Boolean) {
//        viewModelScope.launch {
//            _isOnline.emit(isOnline)
//        }
//    }
//}
//
//@Composable
//fun NetworkMonitor(navController: NavController, ttsViewModel: TTSViewModel, onOnlineDetected: () -> Unit) {
//    val context = LocalContext.current
//    val isOnline by ttsViewModel.isOnline.collectAsState()
//
//    DisposableEffect(Unit) {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkCallback = object : ConnectivityManager.NetworkCallback() {
//
//            override fun onAvailable(network: android.net.Network) {
//                ttsViewModel.setOnlineStatus(true)
//                onOnlineDetected()
//            }
//
//            override fun onLost(network: android.net.Network) {
//                ttsViewModel.setOnlineStatus(false)
//            }
//        }
//
//        connectivityManager.registerDefaultNetworkCallback(networkCallback)
//
//        onDispose {
//            connectivityManager.unregisterNetworkCallback(networkCallback)
//        }
//    }
//
//    if (isOnline) {
//
//        // Display alert box and provide options to switch TTS modes
//        AlertDialog (
//            onDissmissRequest = {},
//            title = { Text("Network connection") },
//            text = { Text("Network connection detected. Please manually switch to online Text to Speech") },
//            confirmButton = {
//                Button(onClick = {
//                    navController.navigate(Screens.SettingScreen.route)
//                }) {
//                    Text("OK")
//                }
//            },
//            dismissButton = {
//                Button(onClick = {
//                    //...
//                }) {
//                    Text("No")
//                }
//            }
//        )
//
//    }
//}