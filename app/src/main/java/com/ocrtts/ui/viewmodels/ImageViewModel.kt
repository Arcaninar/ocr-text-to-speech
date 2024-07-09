package com.ocrtts.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.Text
import com.ocrtts.type.OCRText
import java.util.Collections.rotate

class ImageViewModel : ViewModel() {
    var ocrTextList: List<OCRText> by mutableStateOf(listOf())
        private set

    var ocrTextSelected: OCRText by mutableStateOf(OCRText())
        private set

    var longTouchCounter by mutableIntStateOf(0)
        private set

    var isFinishedAnalysing by mutableStateOf(false)
        private set

    var containText by mutableStateOf(false)
        private set

    fun updateTextRectList(list: List<OCRText>) { ocrTextList = list }

    fun updateTextRectSelected(value: OCRText) { ocrTextSelected = value }

    fun incrementLongTouch() { longTouchCounter += 1 }

    fun setRecognizedText(text: Text) {
        rotate(text.textBlocks, 90)
    }

    fun finishedAnalyzing() {
        isFinishedAnalysing = true
    }

    fun imageContainsText() {
        containText = true
    }
}