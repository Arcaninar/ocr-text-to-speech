package com.ocrtts.camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.ocrtts.ui.viewmodels.RecognizedTextBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//TODO
//Without try-catch, the exception may not be properly catched if the recognizer has error in initialization
//Add Simple Lock
//Try to have simple check for the ocr result

const val TAG = "TextRecognitionAnalyzer"

class TextAnalyzer(private val onTextRecognized: (List<RecognizedTextBlock>,Int,Int,Int) -> Unit,
                   private val coroutineScope: CoroutineScope) :
    ImageAnalysis.Analyzer {
    private var isLocked: Boolean = false
    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun analyze(imageProxy: ImageProxy) {
        if (!isLocked) {
            isLocked=true
            coroutineScope.launch {
                recognizeText(imageProxy, imageProxy.width, imageProxy.height, imageProxy.imageInfo.rotationDegrees)
            }
        }
        else{
            imageProxy.close()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private suspend fun recognizeText(image: ImageProxy,width: Int, height: Int,rotation: Int) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

            try {
                withContext(Dispatchers.IO) {
                    recognizer.process(inputImage)
                        .addOnSuccessListener { visionText ->
//                            if(visionText.text.isNotBlank()){
//                                onTextRecognized(visionText.text)
//                            }
                            processRecognizedText(visionText, width, height, rotation)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error processing image for text recognition: ${e.message}")
                        }
                        .addOnCompleteListener {
                            image.close() // Ensure to close the ImageProxy here
                            isLocked = false
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in processing text recognition: ${e.message}")
                image.close() // Close imageProxy if an exception occurs before addOnCompleteListener
            }
        } else {
            Log.w(TAG, "No media image available for text recognition")
            image.close() // Close imageProxy if mediaImage is null
        }
    }

    private fun processRecognizedText(visionText: Text, imageWidth: Int, imageHeight: Int, rotation: Int) {
        val recognizedTextBlocks = mutableListOf<RecognizedTextBlock>()

        for (block in visionText.textBlocks) {
            if(block.text.isBlank()|| block.cornerPoints.isNullOrEmpty()){
                continue
            }
            Log.i(TAG," :"+block.cornerPoints?.toList().toString())
            recognizedTextBlocks.add(RecognizedTextBlock(block.text,  block.cornerPoints?.toList() ?: emptyList()))
        }
        if (recognizedTextBlocks.isNotEmpty()) {
            onTextRecognized(recognizedTextBlocks, imageWidth, imageHeight, rotation)
        }
    }

}



