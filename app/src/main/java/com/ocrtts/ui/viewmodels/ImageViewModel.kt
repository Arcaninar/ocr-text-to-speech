package com.ocrtts.ui.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ocrtts.history.DataStoreManager
import com.ocrtts.type.OCRText
import com.ocrtts.utils.saveBitmapToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "ImageViewModel"
class ImageViewModel : ViewModel() {
    var ocrTextList = mutableStateListOf<OCRText>()
        private set

    var ocrTextSelected: OCRText by mutableStateOf(OCRText())
        private set

    var isFinishedAnalysing by mutableStateOf(false)
        private set

    var longTouchCounter by mutableIntStateOf(0)
        private set

    private var hasSavedImage by mutableStateOf(false)

    fun onTextRecognized(text: OCRText, reset: Boolean) {
        isFinishedAnalysing = true
        if (reset) {
            ocrTextList = mutableStateListOf()
        }

        if (text.text.isNotBlank()) {
            Log.i(TAG + "RecognizedText", text.text)
            ocrTextList.add(text)
        }
    }

    fun resetFinishedAnalysing() { isFinishedAnalysing = false }

    fun updateTextRectSelected(value: OCRText) { ocrTextSelected = value }

    fun incrementLongTouch() { longTouchCounter += 1 }

    fun saveImageToFile(isFromHistory: Boolean, image: Bitmap, outputDirectory: File, dataStoreManager: DataStoreManager) {
        if (!hasSavedImage && !isFromHistory) {
            val photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")
            saveBitmapToFile(photoFile, image)

            CoroutineScope(Dispatchers.IO).launch {
                dataStoreManager.addImageToHistory(photoFile.absolutePath)
            }

            hasSavedImage = true
        }
    }
}