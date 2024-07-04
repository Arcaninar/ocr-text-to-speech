package com.ocrtts.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.geometry.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.ocrtts.type.TextRect
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "ImageTextRecognitionAnalyzer"

suspend fun analyzeOffline(image: Bitmap, addTextRect: (List<TextRect>) -> Unit) {
    val englishRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val chineseRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    val inputImage = InputImage.fromBitmap(image, 0)
    var isSuccess = false

    suspendCoroutine { continuation ->
        englishRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotBlank()){
                    isSuccess = true
                    val textList = rotate(visionText.textBlocks, 90)
                    addTextRect(textList)
                }
            }
            .addOnFailureListener { e ->
                isSuccess = false
                Log.e(TAG, "Error processing image for text recognition: ${e.message}")
            }
            .addOnCompleteListener {
                continuation.resume(Unit)
            }
    }

    suspendCoroutine { continuation ->
        chineseRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotBlank()){
                    isSuccess = true
                    val textList = rotate(visionText.textBlocks, 90)
                    addTextRect(textList)
                }
            }
            .addOnFailureListener { e ->
                isSuccess = false
                Log.e(TAG, "Error processing image for text recognition: ${e.message}")
            }
            .addOnCompleteListener {
                continuation.resume(Unit)
            }
    }

    if (!isSuccess) {
        // maybe put the image only after it finished?
        // also maybe if cannot detect text then need to tell user and go back to camera?
    }

}

fun rotate(
    textBlocks: List<Text.TextBlock>,
    rotation: Int
): List<TextRect> {
    Log.w("Rotation", rotation.toString())
    val updatedTextRects: MutableList<TextRect> = mutableListOf()

    when (rotation) {
        180 -> {
            modifyRectSize(
                textBlocks,
                updatedTextRects,
                top = 2.25f,
                bottom = 2.325f,
                left = 2.1f,
                right = 2.3f
            )
        }

        270 -> {
            modifyRectSize(
                textBlocks,
                updatedTextRects,
                top = 2.25f,
                bottom = 2.275f,
                left = 2.025f,
                right = 2.3f
            )
        }

        0 -> {
            modifyRectSize(
                textBlocks,
                updatedTextRects,
                top = 2.225f,
                bottom = 2.275f,
                left = 2.2f,
                right = 2.3f
            )
        }

        else -> {
            modifyRectSize(
                textBlocks,
                updatedTextRects,
                top = 2.2f,
                bottom = 2.25f,
                left = 1.85f,
                right = 2.25f
            )
        }
    }
    return updatedTextRects
}

private fun modifyRectSize(
    textBlocks: List<Text.TextBlock>,
    updatedTextRects: MutableList<TextRect>,
    top: Float = 1f,
    bottom: Float = 1f,
    left: Float = 1f,
    right: Float = 1f
) {
    for (text in textBlocks) {
        if (text.boundingBox != null) {
            val textBlock = text.boundingBox!!
            updatedTextRects.add(
                TextRect(
                    text.text, Rect(
                        top = textBlock.top.toFloat() * top,
                        bottom = textBlock.bottom.toFloat() * bottom,
                        left = textBlock.left.toFloat() * left,
                        right = textBlock.right.toFloat() * right,
                    )
                )
            )
        }
    }
}