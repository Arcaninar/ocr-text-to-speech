package com.ocrtts.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.ocrtts.type.OCRText
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "ImageTextRecognitionAnalyzer"

//interface OnlineOCRInterface {
//    @POST("images:annotate")
//    fun getAnalysedOCRText(@Query("key") key: String, @Body post: String): String
//}
//
//object OnlineOCRInstance {
//    private val baseUrl = "https://vision.googleapis.com/v1"
//
//    private val retrofit = Retrofit.Builder()
//        .addConverterFactory(ScalarsConverterFactory.create())
//        .baseUrl(baseUrl)
//        .build()
//
//    val onlineOCR: OnlineOCRInterface by lazy {
//        retrofit.create(OnlineOCRInterface::class.java)
//    }
//}

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

fun analyzeOCROnline(image: Bitmap) {
//    val
}


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