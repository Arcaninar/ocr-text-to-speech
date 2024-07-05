package com.ocrtts.base

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.ocrtts.R

@Composable
fun ShowNetworkDialog(showDialog: MutableState<Boolean>, onDismiss: () -> Unit) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = stringResource(id = R.string.network_error)) },
            text = { Text(text = stringResource(id = R.string.network_error_content)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        onDismiss()
                    }
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            }
        )
    }
}

fun isNetworkConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}
//
//
//
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//import android.speech.tts.TextToSpeech
//import android.util.Log
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.MutableState
//import androidx.compose.ui.res.stringResource
//import com.innospire.smarttrafficfund.R
//import com.innospire.smarttrafficfund.objectdetection.views.CameraViewManager.tts
//import java.util.Locale
//
//@Composable
//fun ShowNetworkDialog(showDialog: MutableState<Boolean>, onDismiss: () -> Unit) {
//    if (showDialog.value) {
//        AlertDialog(
//            onDismissRequest = { showDialog.value = false },
//            title = { Text(text = stringResource(id = R.string.network_error)) },
//            text = { Text(text = stringResource(id = R.string.network_error_content)) },
//            confirmButton = {
//                Button(
//                    onClick = {
//                        showDialog.value = false
//                        onDismiss()
//                    }
//                ) {
//                    Text(stringResource(id = R.string.confirm))
//                }
//            }
//        )
//    }
//}
//
//fun getTTSInstance(context: Context): TTS {
//    return if (isNetworkConnected(context)) {
//        AzureTTS(context)
//    } else {
//        AndroidTTS(context)
//    }
//}
//
//fun isNetworkConnected(context: Context): Boolean {
//    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//    val activeNetwork = connectivityManager.activeNetwork ?: return false
//    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
//    return when {
//        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
//        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
//        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
//        else -> false
//    }
//}
//
//interface TTS {
//    fun speak(text: String)
//}
//
//class GoogleTTS(context: Context) : TTS, TextToSpeech.OnInitListener {
//    private var tts: TextToSpeech? = null
//
//    init {
//        tts = TextToSpeech(context, this)
//    }
//
//    override fun onInit(status: Int) {
//        if (status == TextToSpeech.SUCCESS) {
//            val result = tts?.setLanguage(Locale.US)
//            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Log.e("TTS", "Language not supported")
//            }
//        } else {
//            Log.e("TTS", "Initialization failed")
//        }
//    }
//
//    override fun speak(text: String) {
//        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
//    }
//}
//
//class AndroidTTS(context: Context) : TTS, TextToSpeech.OnInitListener {
//    private var tts: TextToSpeech? = null
//
//    init {
//        tts = TextToSpeech(context, this)
//    }
//
//    override fun onInit(status: Int) {
//        if (status == TextToSpeech.SUCCESS) {
//            val result = tts?.setLanguage(Locale.US)
//            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Log.e("TTS", "Language not supported")
//            }
//        } else {
//            Log.e("TTS", "Initialization failed")
//        }
//    }
//
//    override fun speak(text: String) {
//        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
//    }
//}
