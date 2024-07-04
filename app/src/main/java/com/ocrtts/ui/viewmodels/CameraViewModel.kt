package com.ocrtts.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

const val TAG="MainViewModel"
class CameraViewModel : ViewModel() {
//    private val _recognizedText = MutableStateFlow<String>("")
//    val recognizedText = _recognizedText.asStateFlow()

    var recognizedText by mutableStateOf(false)
        private set

    fun updateRecognizedText(value: Boolean) {
//        _recognizedText.value = text
//        Log.i(TAG,text)
        recognizedText = value
    }
}