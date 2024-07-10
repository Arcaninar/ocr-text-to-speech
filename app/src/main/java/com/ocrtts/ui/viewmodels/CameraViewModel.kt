package com.ocrtts.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.ocrtts.type.OCRText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

const val TAG="MainViewModel"
class CameraViewModel : ViewModel() {
    private val _isRecognizedText = MutableStateFlow(false)
    val isRecognizedText = _isRecognizedText.asStateFlow()

    private val _hasTextBefore = MutableStateFlow(false)
    val hasTextBefore = _hasTextBefore.asStateFlow()

    fun updateRecognizedText(textList: List<OCRText>) {
        _isRecognizedText.value = textList[0].text.isNotBlank()
    }

    fun updateHasText(value: Boolean) {
        _hasTextBefore.value = value
    }
}