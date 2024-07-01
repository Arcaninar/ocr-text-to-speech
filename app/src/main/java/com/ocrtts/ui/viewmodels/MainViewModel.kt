package com.ocrtts.ui.viewmodels

import android.media.Image
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ocrtts.ui.screens.TextRect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

//TODO
//Still reading

const val TAG="MainViewModel"
class MainViewModel : ViewModel() {
    var textRectList: MutableState<List<TextRect>> = mutableStateOf(listOf())

    var textRectSelected: MutableState<TextRect?> = mutableStateOf(null)
        private set

    var previousHasText: MutableState<Boolean> = mutableStateOf(false)
        private set

    var longTouchCounter: MutableState<Int> = mutableStateOf(0)
        private set

    var imageSelected: MutableState<Image?> = mutableStateOf(null)

    fun setTextRectList(list: List<TextRect>) { textRectList.value = list }

    fun setTextRectSelected(value: TextRect?) { textRectSelected.value = value }

    fun setPreviousHasText(value: Boolean) { previousHasText.value = value }

    fun incrementLongTouch() { longTouchCounter.value += 1 }

    fun setImageSelected(image: Image?) { imageSelected.value = image }

    private val _recognizedText = MutableStateFlow<String>("")
    val recognizedText = _recognizedText.asStateFlow()

    fun updateRecognizedText(text: String) {
        _recognizedText.value = text
        Log.i(TAG,text)
    }
}