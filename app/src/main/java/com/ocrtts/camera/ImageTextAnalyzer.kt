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
import com.ocrtts.type.AnalysisFeature
import com.ocrtts.type.ImageBase64
import com.ocrtts.type.ImageRequest
import com.ocrtts.type.OCRText
import com.ocrtts.type.OnlineOCRRequest
import com.ocrtts.ui.viewmodels.ImageViewModel
import com.ocrtts.utils.TimingUtility
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


private const val TAG = "ImageTextRecognitionAnalyzer"

// better to make this function as a class?
suspend fun analyzeOCR(image: Bitmap, viewSize: IntSize, onTextRecognized: (List<OCRText>) -> Unit) {
    // do online analysis if internet is online, otherwise offline
    val scaleFactor = getScaleFactor(viewSize, IntSize(image.width, image.height))
    TimingUtility.measureSuspendingExecutionTime("online ocr") { analyzeOCROnline(image) }
//    TimingUtility.measureSuspendingExecutionTime("offline ocr") { analyzeOCROffline(InputImage.fromBitmap(image, 0), false, scaleFactor, onTextRecognized) }
    analyzeOCROffline(InputImage.fromBitmap(image, 0), false, scaleFactor, onTextRecognized)
//    val ocrTextList = analyzeOCROnline(image)
}

fun getScaleFactor(viewSize: IntSize, imageSize: IntSize): Pair<Float, Float> {
    val widthScale = viewSize.width.toFloat() / imageSize.width.toFloat()
    val heightScale = viewSize.height.toFloat() / imageSize.height.toFloat()
    return Pair(widthScale, heightScale)
}

suspend fun analyzeOCROnline(image: Bitmap): Int {
//    TimingUtility.measureExecutionTime("initiate client") {
//        HttpClient(CIO) {
//            install(ContentNegotiation) {
//                json(Json {
//                    prettyPrint = true
//                    isLenient = true
//                })
//            }
//        }
//    }

    // timed the creation of client and it's very fast
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    val apiKey = BuildConfig.API_KEY

//    TimingUtility.measureExecutionTime("create request") {
//        val apiKey = BuildConfig.API_KEY
//        val byteArrayOutputStream = ByteArrayOutputStream()
//        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
//        val byteArray = byteArrayOutputStream.toByteArray()
//        val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
//        val request = OnlineOCRRequest(
//            listOf(
//                ImageRequest(
//                    ImageBase64(encoded),
//                    listOf(
//                        AnalysisFeature("TEXT_DETECTION", 1)
//                    )
//                )
//            )
//        )
//    }

    // timed converting bitmap to base64 and it took quite long, around 2-3 seconds but I think there is a way to fix this or change the conversion
    val byteArrayOutputStream = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)

    val request = OnlineOCRRequest(
        listOf(
            ImageRequest(
                ImageBase64(encoded),
                listOf(
                    AnalysisFeature("TEXT_DETECTION", 1)
                )
            )
        )
    )
    Log.i("response", "start request")
//    TimingUtility.measureSuspendingExecutionTime("send request") {
//        client.post("https://vision.googleapis.com/v1/images:annotate?key=$apiKey") {
////        url {
////            parameters.append("key", apiKey)
////        }
//            headers {
//                append("X-Android-Package", "com.ocrtts")
//                append("X-Android-Cert", "00403C671C1C5F0ACBCC5E4EBCAB03C790AF5BB4")
//            }
//            contentType(ContentType.Application.Json)
//            setBody(request)
//        }
//    }

    // this one takes really long, around 5-10 seconds even though the response is only an error response
    val response: HttpResponse = client.post("https://vision.googleapis.com/v1/images:annotate?key=$apiKey") {
//        url {
//            parameters.append("key", apiKey)
//        }
        headers {
            append("X-Android-Package", "com.ocrtts")
            append("X-Android-Cert", "00403C671C1C5F0ACBCC5E4EBCAB03C790AF5BB4")
        }
        contentType(ContentType.Application.Json)
        setBody(request)
    }
    Log.i("response", response.body())
//    return mutableListOf(OCRText())
    return 0
}


suspend fun analyzeOCROffline(
    image: InputImage,
    onlyDetect: Boolean,
    scaleFactor: Pair<Float, Float> = Pair(0f, 0f),
    onTextRecognized: (List<OCRText>) -> Unit
): Int {
    //chinese text recognizer can detect both english and chinese words
    val textRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    suspendCoroutine { continuation ->
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(Unit)
                if (onlyDetect) {
                    onTextRecognized(listOf(OCRText(text = visionText.text)))
                }
                else {
                    onTextRecognized(convertToOCRText(visionText.textBlocks, scaleFactor))
                }

            }
            .addOnFailureListener { e ->
                continuation.resume(Unit)
                Log.e(TAG, "Error processing image for text recognition: ${e.message}")
            }
    }
    return 0
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