package com.ocrtts.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ocrtts.type.OCRText

class ImageViewModel : ViewModel() {
    var ocrTextList = mutableStateListOf<OCRText>()
        private set

    var ocrTextSelected: OCRText by mutableStateOf(OCRText())
        private set

    var isFinishedAnalysing by mutableStateOf(false)
        private set

    var longTouchCounter by mutableIntStateOf(0)
        private set

    fun onTextRecognized(text: OCRText) {
        if (text.text.isNotBlank()) {
            ocrTextList.add(text)
        }

        if (!isFinishedAnalysing) {
            isFinishedAnalysing = true
        }

        Log.i("ViewModel", ocrTextList.size.toString())
    }

    fun resetFinishedAnalysing() { isFinishedAnalysing = false }

    fun updateTextRectSelected(value: OCRText) { ocrTextSelected = value }

    fun incrementLongTouch() { longTouchCounter += 1 }
}