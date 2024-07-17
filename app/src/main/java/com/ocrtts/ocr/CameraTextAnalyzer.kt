package com.ocrtts.ocr

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.ocrtts.type.OCRText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "CameraTextRecognitionAnalyzer"

class CameraTextAnalyzer(private val onTextRecognized: (OCRText) -> Unit, private val coroutineScope: CoroutineScope) : ImageAnalysis.Analyzer {
    private var isLocked: Boolean = false
    override fun analyze(imageProxy: ImageProxy) {
        if (!isLocked) {
            isLocked=true
            coroutineScope.launch {
                recognizeText(imageProxy)
            }
        }
        else{
            imageProxy.close()
        }
    }
    @OptIn(ExperimentalGetImage::class)
    private suspend fun recognizeText(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            try {
                withContext(Dispatchers.Main) {
                    analyzeCameraOCR(image, image.imageInfo.rotationDegrees, onTextRecognized)
                    image.close()
                    isLocked = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in processing text recognition: ${e.message}")
                image.close() // Close imageProxy if an exception occurs before addOnCompleteListener
                isLocked = false // not sure
            }
        } else {
            Log.w(TAG, "No media image available for text recognition")
            image.close() // Close imageProxy if mediaImage is null
            isLocked = false // not sure
        }
    }
}
