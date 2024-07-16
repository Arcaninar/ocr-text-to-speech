package com.ocrtts.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ocrtts.type.OCRText

class ImageViewModel : ViewModel() {
    var ocrTextList: List<OCRText>? by mutableStateOf(null)
        private set

    var ocrTextSelected: OCRText by mutableStateOf(OCRText())
        private set

    var longTouchCounter by mutableIntStateOf(0)
        private set

    fun onTextRecognized(textList: List<OCRText>) {
        ocrTextList = textList.ifEmpty {
            listOf()
        }
    }

    fun updateTextRectSelected(value: OCRText) { ocrTextSelected = value }

    fun incrementLongTouch() { longTouchCounter += 1 }
}