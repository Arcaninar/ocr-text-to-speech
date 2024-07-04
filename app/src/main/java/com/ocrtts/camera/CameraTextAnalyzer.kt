package com.ocrtts.camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//TODO
//Without try-catch, the exception may not be properly caught if the recognizer has error in initialization
//Add Simple Lock
//Try to have simple check for the ocr result


//
//class TextRecognitionAnalyzer(
//    private val onDetectedTextUpdated: (Text, Int) -> Unit
//) : ImageAnalysis.Analyzer {
//
//    companion object {
//        const val THROTTLE_TIMEOUT_MS = 1_000L
//    }
//
//
//    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
//    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//
//    @OptIn(ExperimentalGetImage::class)
//    override fun analyze(imageProxy: ImageProxy) {
//        TimingUtility.measureExecutionTime("analyze"){
//            scope.launch {
//                val mediaImage: Image = imageProxy.image ?: run { imageProxy.close(); return@launch }
//                val inputImage: InputImage = InputImage.fromMediaImage(mediaImage, 90)
//
//                suspendCoroutine { continuation ->
//                    textRecognizer.process(inputImage)
//                        .addOnSuccessListener { visionText: Text ->
//                            onDetectedTextUpdated(visionText, imageProxy.imageInfo.rotationDegrees)
//                        }
//                        .addOnCompleteListener {
//                            continuation.resume(Unit)
//                        }
//                }
//
////            delay(THROTTLE_TIMEOUT_MS)
//            }.invokeOnCompletion { exception ->
//
//                exception?.printStackTrace()
//                imageProxy.close()
//            }
//        }
//
//    }
//}

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
                    val (hasText, _) = analyzeOffline(inputImage, true)
                    onTextRecognized(hasText)
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