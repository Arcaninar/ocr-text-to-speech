package com.ocrtts.ui.viewmodels

import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ocrtts.history.DataStoreManager
import com.ocrtts.type.OCRText
import com.ocrtts.utils.saveBitmapToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "ImageViewModel"
class ImageViewModel(application: Application, private val settingViewModel: SettingViewModel) : AndroidViewModel(application) {
    var ocrTextList = mutableStateListOf<OCRText>()
        private set

    var ocrTextSelected: OCRText by mutableStateOf(OCRText())
        private set

    var isFinishedAnalysing by mutableStateOf(false)
        private set

    var longTouchCounter by mutableIntStateOf(0)
        private set

    private var hasSavedImage by mutableStateOf(false)

    private val _isOnline = MutableStateFlow(checkNetworkAvailability(application.applicationContext))
    val isOnline: StateFlow<Boolean> = _isOnline
    //change1
//    val isNetworkAvailable = MutableStateFlow(false)
    var showDialog = mutableStateOf(false)


    val modelType: StateFlow<String> = settingViewModel.modelType.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = "offlineTTS"
    )

    init {
        startNetworkCallback(application.applicationContext)

        viewModelScope.launch {
            _isOnline.collect { isOnline ->
                modelType.collect { model ->
                    if (isOnline && model == "offlineTTS") {
                        showDialog.value = true
                    } else {
                        showDialog.value = false
                    }
                }
            }
        }
    }

    private fun checkNetworkAvailability(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private fun startNetworkCallback(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                viewModelScope.launch {
                    _isOnline.emit(true)
                }
            }

            override fun onLost(network: android.net.Network) {
                viewModelScope.launch {
                    _isOnline.emit(false)
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    fun onTextRecognized(text: OCRText, reset: Boolean) {
        if (reset && !isFinishedAnalysing) {
            ocrTextList = mutableStateListOf()
        }
        isFinishedAnalysing = true


        if (text.text.isNotBlank()) {
            Log.i(TAG, text.text)
            ocrTextList.add(text)
        }
    }

    fun resetFinishedAnalysing() {
        isFinishedAnalysing = false
        ocrTextList = mutableStateListOf()
    }

    fun updateTextRectSelected(value: OCRText) { ocrTextSelected = value }

    fun incrementLongTouch() { longTouchCounter += 1 }

    fun saveImageToFile(isFromHistory: Boolean, image: Bitmap, orientation: Int, viewSize: IntSize, outputDirectory: File, dataStoreManager: DataStoreManager) {
        if (!hasSavedImage && !isFromHistory) {
            val orientationChar = when (orientation) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> 'L'
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> 'R'
                else -> 'P'
            }
            val size = "${viewSize.width}x${viewSize.height}"
            val photoFile = File(outputDirectory, "${orientationChar}_${size}_${System.currentTimeMillis()}.jpg")
            saveBitmapToFile(photoFile, image)

            CoroutineScope(Dispatchers.IO).launch {
                dataStoreManager.addImageToHistory(photoFile.absolutePath)
            }

            hasSavedImage = true
        }
    }
}