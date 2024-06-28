package com.ocrtts.ui

import android.media.Image
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ocrtts.ui.camera.TextRect

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
}