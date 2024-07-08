package com.ocrtts.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.ocrtts.type.OCRText
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "ImageTextRecognitionAnalyzer"


suspend fun analyzeOCR(image: Bitmap, viewSize: IntSize): MutableList<OCRText> {
    // do online analysis if internet is online, otherwise offline
    val scaleFactor = getScaleFactor(viewSize, IntSize(image.width, image.height))
    val textRectList = analyzeOCROffline(InputImage.fromBitmap(image, 0), false, scaleFactor)
    for (textRect in textRectList) {
        Log.i(TAG + "test", textRect.text + " | " + textRect.language)
        Log.i(TAG + "test", textRect.rect.top.toString() + " | " + textRect.rect.bottom.toString() + " | " + textRect.rect.left.toString() + " | " + textRect.rect.right.toString())
    }
    return textRectList
}

fun getScaleFactor(viewSize: IntSize, imageSize: IntSize): Pair<Float, Float> {
    val widthScale = viewSize.width.toFloat() / imageSize.width.toFloat()
    val heightScale = viewSize.height.toFloat() / imageSize.height.toFloat()
    return Pair(widthScale, heightScale)
}

suspend fun analyzeOCROnline(image: Bitmap) {

}


suspend fun analyzeOCROffline(
    image: InputImage,
    onlyDetect: Boolean,
    scaleFactor: Pair<Float, Float> = Pair(0f, 0f)
): MutableList<OCRText> {
    val englishRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val chineseRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    var isSuccess = false
    val ocrTextList = mutableListOf<OCRText>()

    suspendCoroutine { continuation ->
        englishRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotBlank()){
                    isSuccess = true
                    if (!onlyDetect) {
                        ocrTextList.addAll(convertToOCRText(visionText.textBlocks,  "English", scaleFactor))
                    }
                    else {
                        ocrTextList.add(OCRText())
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error processing image for text recognition: ${e.message}")
            }
            .addOnCompleteListener {
                continuation.resume(Unit)
            }
    }

    if (onlyDetect && isSuccess) {
        return ocrTextList
    }

    // commented out because the chineseRecognizer recognizes the english/latin letter as well (I thought it only recognizes chinese character), so it's like double scanning here
//    suspendCoroutine { continuation ->
//        chineseRecognizer.process(image)
//            .addOnSuccessListener { visionText ->
//                if (visionText.text.isNotBlank()){
//                    OCRTextList.addAll(convertToAnalyzedText(visionText.textBlocks, 55, "Chinese"))
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error processing image for text recognition: ${e.message}")
//            }
//            .addOnCompleteListener {
//                continuation.resume(Unit)
//            }
//    }
    return ocrTextList
}

fun convertToOCRText(
    textBlocks: List<Text.TextBlock>,
    language: String,
    scaleFactor: Pair<Float, Float>
): List<OCRText> {
    val updatedOCRTexts: MutableList<OCRText> = mutableListOf()

    for (text in textBlocks) {
        if (text.boundingBox != null) {
            val textBlock = text.boundingBox!!
            val widthScaleFactor = scaleFactor.first
            val heightScaleFactor = scaleFactor.second
            updatedOCRTexts.add(
                OCRText(
                    text.text, Rect(
                        top = textBlock.top.toFloat() * heightScaleFactor,
                        bottom = textBlock.bottom.toFloat() * heightScaleFactor,
                        left = textBlock.left.toFloat() * widthScaleFactor,
                        right = textBlock.right.toFloat() * widthScaleFactor,
                    ), language
                )
            )
        }
    }

    return updatedOCRTexts
}