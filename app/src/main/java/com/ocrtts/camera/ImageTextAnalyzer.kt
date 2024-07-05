package com.ocrtts.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.geometry.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.ocrtts.type.OCRText
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "ImageTextRecognitionAnalyzer"


suspend fun analyzeOCR(image: Bitmap): MutableList<OCRText> {
    // do online analysis if internet is online, otherwise offline
    val textRectList = analyzeOCROffline(InputImage.fromBitmap(image, 0), false)
    for (textRect in textRectList) {
        Log.i(TAG + "test", textRect.text + " | " + textRect.language)
        Log.i(TAG + "test", textRect.rect.top.toString() + " | " + textRect.rect.bottom.toString() + " | " + textRect.rect.left.toString() + " | " + textRect.rect.right.toString())
    }
    return textRectList
}

suspend fun analyzeOCROnline(image: Bitmap) {

}


suspend fun analyzeOCROffline(
    image: InputImage,
    onlyDetect: Boolean
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
                        ocrTextList.addAll(convertToOCRText(visionText.textBlocks,  "English"))
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
    language: String
): List<OCRText> {
    val updatedOCRTexts: MutableList<OCRText> = mutableListOf()

    for (text in textBlocks) {
        if (text.boundingBox != null) {
            val textBlock = text.boundingBox!!
            Log.i(TAG + "test", text.text)
            Log.i(TAG + "test", textBlock.top.toString() + " | " + textBlock.bottom.toString() + " | " + textBlock.left.toString() + " | " + textBlock.right.toString())
            updatedOCRTexts.add(
                OCRText(
                    text.text, Rect(
                        // TODO: find a way to align the box with the text
                        top = (textBlock.bottom.toFloat() - (textBlock.bottom.toFloat() * textBlock.height() / 1000f)) * 0.675f,
                        bottom = textBlock.bottom.toFloat() * 0.675f,
                        left = (textBlock.right.toFloat() - (textBlock.right.toFloat() * textBlock.width() / 1200f)) * 0.475f,
                        right = textBlock.right.toFloat() * 0.5175f,
                    ), language
                )
            )
//
//            val imageWidth = _image!!.width
//            val imageHeight = _image!!.height
//            val viewWidth = _size.width
//            val viewHeight = _size.height
//
//            var scaledWidth: Int
//            var scaledHeight: Int
//            if (imageWidth*viewHeight <= imageHeight*viewWidth) {
//
//                //rescaled width and height of image within ImageView
//                scaledWidth = (imageWidth*viewHeight)/imageHeight;
//                scaledHeight = viewHeight;
//            }
//            else {
//                //rescaled width and height of image within ImageView
//                scaledWidth = viewWidth;
//                scaledHeight = (imageHeight*viewWidth)/imageWidth;
//            }
//
//            updatedOCRTexts.add(
//                OCRText(
//                    text.text, Rect(
//                        top = textBlock.top / getSystem().displayMetrics.density * 1.5f,
//                        bottom = textBlock.bottom / getSystem().displayMetrics.density * 1.5f,
//                        left = textBlock.left / getSystem().displayMetrics.density * 1.5f,
//                        right = textBlock.right / getSystem().displayMetrics.density * 1.5f,
//                    ), language
//                )
//            )
        }
    }

    return updatedOCRTexts
}