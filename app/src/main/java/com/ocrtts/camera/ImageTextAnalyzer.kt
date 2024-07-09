package com.ocrtts.camera

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.ocrtts.BuildConfig
import com.ocrtts.type.OCRText
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


private const val TAG = "ImageTextRecognitionAnalyzer"

suspend fun analyzeOCR(image: Bitmap, viewSize: IntSize): MutableList<OCRText> {
    // do online analysis if internet is online, otherwise offline
    val scaleFactor = getScaleFactor(viewSize, IntSize(image.width, image.height))
    val textRectList = analyzeOCROffline(InputImage.fromBitmap(image, 0), false, scaleFactor)
    return textRectList
}

fun getScaleFactor(viewSize: IntSize, imageSize: IntSize): Pair<Float, Float> {
    val widthScale = viewSize.width.toFloat() / imageSize.width.toFloat()
    val heightScale = viewSize.height.toFloat() / imageSize.height.toFloat()
    return Pair(widthScale, heightScale)
}

//suspend fun analyzeOCROnline(image: Bitmap) {
//    val client = HttpClient()
//
//    val apiKey = BuildConfig.API_KEY
//
//    val byteArrayOutputStream = ByteArrayOutputStream()
//    image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
//    val byteArray = byteArrayOutputStream.toByteArray()
//    val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
//
////    val request =
////        OnlineOCRRequest(
////            listOf(
////                ImageRequest(
////                    Image(encoded),
////                    listOf(
////                        Feature(
////
////                        )
////                    )
////                )
////            )
////        )
//
//    val response: HttpResponse = client.post("https://vision.googleapis.com/v1/images:annotate") {
//        url {
//            parameters.append("key", apiKey)
//        }
//        setBody("Body content")
//    }
//}


suspend fun analyzeOCROffline(
    image: InputImage,
    onlyDetect: Boolean,
    scaleFactor: Pair<Float, Float> = Pair(0f, 0f)
): MutableList<OCRText> {
    //chinese text recognizer can detect both english and chinese words
    val textRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    val ocrTextList = mutableListOf<OCRText>()

    suspendCoroutine { continuation ->
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotBlank()){
                    if (!onlyDetect) {
                        ocrTextList.addAll(convertToOCRText(visionText.textBlocks, scaleFactor))
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

    return ocrTextList
}

fun convertToOCRText(
    textBlocks: List<Text.TextBlock>,
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
                    text.text,
                    Rect(
                        top = textBlock.top.toFloat() * heightScaleFactor,
                        bottom = textBlock.bottom.toFloat() * heightScaleFactor,
                        left = textBlock.left.toFloat() * widthScaleFactor,
                        right = textBlock.right.toFloat() * widthScaleFactor,
                    )
                )
            )
        }
    }

    return updatedOCRTexts
}