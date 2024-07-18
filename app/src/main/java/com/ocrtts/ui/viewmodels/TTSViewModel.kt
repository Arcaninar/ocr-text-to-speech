package com.ocrtts.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import com.ocrtts.base.AzureTextSynthesis
import com.ocrtts.base.OfflineTextSynthesis

class TTSViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    val offlineTTS = OfflineTextSynthesis(context)
    var azureTTS: AzureTextSynthesis? = null
    var isOnline: Boolean = false

    init {
        isOnline = checkNetworkAvailability(context)
        if (isOnline) {
            azureTTS = AzureTextSynthesis("en-GB-SoniaNeural")
        }
    }

    private fun checkNetworkAvailability(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    fun speak(text: String, speed: Float) {
        if (isOnline && azureTTS != null) {
            azureTTS?.startPlaying(text, speed)
        } else {
            offlineTTS.speak(text)
        }
    }

    override fun onCleared() {
        super.onCleared()
        azureTTS?.destroy()
        offlineTTS.shutdown()
    }
}
