package com.ocrtts.camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "CameraTextRecognitionAnalyzer"

class CameraTextAnalyzer(private val onTextRecognized: (Boolean) -> Unit, private val coroutineScope: CoroutineScope) : ImageAnalysis.Analyzer {
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
            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
//            val englishRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//            val chineseRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

            try {
                withContext(Dispatchers.IO) {
                    val textRect = analyzeOCROffline(inputImage, true)
                    onTextRecognized(textRect.isNotEmpty())
                    image.close()
                    isLocked = false
//                    suspendCoroutine { continuation ->
//                        englishRecognizer.process(inputImage)
//                            .addOnSuccessListener { visionText ->
//                                if (visionText.text.isNotBlank()) {
//                                    hasText = true
//                                    onTextRecognized(true)
//                                }
//                            }
//                            .addOnFailureListener { e ->
//                                Log.e(TAG, "Error processing image for text recognition: ${e.message}")
//                            }
//                            .addOnCompleteListener {
//                                if (hasText) {
//                                    image.close() // Ensure to close the ImageProxy here
//                                    isLocked = false
//                                    continuation.resume(Unit)
//                                }
//                            }
//                    }
//
//                    if (!hasText) {
//                        suspendCoroutine { continuation ->
//                            chineseRecognizer.process(inputImage)
//                                .addOnSuccessListener { visionText ->
//                                    onTextRecognized(visionText.text.isNotBlank())
//                                }
//                                .addOnFailureListener { e ->
//                                    Log.e(TAG, "Error processing image for text recognition: ${e.message}")
//                                }
//                                .addOnCompleteListener {
//                                    image.close() // Ensure to close the ImageProxy here
//                                    isLocked = false
//                                    continuation.resume(Unit)
//                                }
//                        }
//                    }

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
