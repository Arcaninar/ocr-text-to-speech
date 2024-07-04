package com.ocrtts.ui.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.Text

//data class imageObject(
//    image
//)

const val TAG="MainViewModel"
class CameraViewModel : ViewModel() {
//    private val _recognizedText = MutableStateFlow<String>("")
//    val recognizedText = _recognizedText.asStateFlow()

    var recognizedText by mutableStateOf(false)
        private set

//    var textRectList: MutableState<List<TextRect>> = mutableStateOf(listOf())
//        private set
//
//    var textRectSelected: MutableState<TextRect?> = mutableStateOf(null)
//        private set

//    var previousHasText: MutableState<Boolean> = mutableStateOf(false)
//        private set

//    var longTouchCounter: MutableState<Int> = mutableStateOf(0)
//        private set

//    var imageSelected: MutableState<Image?> = mutableStateOf(null)

//    fun setTextRectList(list: List<TextRect>) { textRectList.value = list }
//
//    fun setTextRectSelected(value: TextRect?) { textRectSelected.value = value }

//    fun setPreviousHasText(value: Boolean) { previousHasText.value = value }

//    fun incrementLongTouch() { longTouchCounter.value += 1 }

//    fun setImageSelected(image: Image?) { imageSelected.value = image }

    fun updateRecognizedText(value: Boolean) {
//        _recognizedText.value = text
//        Log.i(TAG,text)
        recognizedText = value
    }
}