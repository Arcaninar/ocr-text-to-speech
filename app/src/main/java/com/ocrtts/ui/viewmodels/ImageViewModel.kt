package com.ocrtts.ui.viewmodels

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
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
        if (reset && !isFinishedAnalysing) {
            ocrTextList = mutableStateListOf()
        }
        isFinishedAnalysing = true


        if (text.text.isNotBlank()) {
            Log.i(TAG, text.text)
            ocrTextList.add(text)
        }
    }

    fun resetFinishedAnalysing() {
        isFinishedAnalysing = false
        ocrTextList = mutableStateListOf()
    }

    fun updateTextRectSelected(value: OCRText) { ocrTextSelected = value }

    fun incrementLongTouch() { longTouchCounter += 1 }

    fun saveImageToFile(isFromHistory: Boolean, image: Bitmap, orientation: Int, viewSize: IntSize, outputDirectory: File, dataStoreManager: DataStoreManager) {
        if (!hasSavedImage && !isFromHistory) {
            val orientationChar = when (orientation) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> 'L'
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> 'R'
                else -> 'P'
            }
            val size = "${viewSize.width}x${viewSize.height}"
            val photoFile = File(outputDirectory, "${orientationChar}_${size}_${System.currentTimeMillis()}.jpg")
            saveBitmapToFile(photoFile, image)

            CoroutineScope(Dispatchers.IO).launch {
                dataStoreManager.addImageToHistory(photoFile.absolutePath)
            }

            hasSavedImage = true
        }
    }
}