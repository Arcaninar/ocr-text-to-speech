package com.ocrtts.ui.camera

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CameraScreenViewModel : ViewModel() {
    var textRectList: MutableState<List<TextRect>> = mutableStateOf(listOf())

    var textRectSelected: MutableState<TextRect?> = mutableStateOf(null)
        private set

    var previousHasText: MutableState<Boolean> = mutableStateOf(false)
        private set

    var longTouchCounter: MutableState<Int> = mutableStateOf(0)
        private set

    fun setTextRectList(list: List<TextRect>) { textRectList.value = list }

    fun setTextRectSelected(value: TextRect?) { textRectSelected.value = value }

    fun setPreviousHasText(value: Boolean) { previousHasText.value = value }

    fun incrementLongTouch() { longTouchCounter.value += 1 }
}