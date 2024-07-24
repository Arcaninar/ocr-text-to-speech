package com.ocrtts.ocr

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequest
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.AnnotateImageRequest
import com.google.api.services.vision.v1.model.AnnotateImageResponse
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest
import com.google.api.services.vision.v1.model.Feature
import com.google.api.services.vision.v1.model.Image
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.ocrtts.BuildConfig
import com.ocrtts.imageCacheFile
import com.ocrtts.type.OCRText
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import com.ocrtts.utils.modifyBitmap
import com.ocrtts.utils.saveBitmapToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalGetImage::class)
suspend fun analyzeCameraOCR(image: ImageProxy, viewModel: ImageSharedViewModel, onTextRecognized: (OCRText, Boolean) -> Unit) {
    // depends on user setting whether want to be accurate, or faster and save more battery
    val hasText: Boolean
    val rotation = image.imageInfo.rotationDegrees
    val useOnline = false

    if (useOnline) {
        val bitmap = image.toBitmap()
        hasText = OnlineOCR.analyzeOCR(bitmap, true, onTextRecognized = onTextRecognized, isReset = false)
    }
    else {
        val inputImage = InputImage.fromMediaImage(image.image!!, rotation)
        hasText = OfflineOCR.analyzeOCR(inputImage, true, onTextRecognized = onTextRecognized, isReset = false)
    }

    withContext(Dispatchers.IO) {
        if (hasText) {
            saveImageCache(image.toBitmap(), rotation, viewModel.size)
        }
    }
}

private fun saveImageCache(image: Bitmap, rotationDegree: Int, screenSize: IntSize) {
    val finalImage = modifyBitmap(image, rotationDegree, screenSize)
    saveBitmapToFile(imageCacheFile, finalImage)
    Log.i("ImageCache", "Image saved to: ${imageCacheFile.absolutePath}")
}

suspend fun analyzeImageOCR(viewSize: IntSize, image: Bitmap, onTextRecognized: (OCRText, Boolean) -> Unit) {
    val scaleFactor = getScaleFactor(viewSize, IntSize(image.width, image.height))

    // do online analysis if internet is online, otherwise offline
    val TAG = "AnalyzeImageOCR"
    val hasInternet = true
    if (hasInternet) {
        Log.i(TAG, "Analyzing image using OnlineOCR")
        OnlineOCR.analyzeOCR(image, false, scaleFactor, onTextRecognized, false)
    }
    else {
        Log.i(TAG, "Analyzing image using OfflineOCR")
        val inputImage = InputImage.fromBitmap(image, 0)
        OfflineOCR.analyzeOCR(inputImage, false, scaleFactor, onTextRecognized, false)
    }
}

fun getScaleFactor(viewSize: IntSize, imageSize: IntSize): Pair<Float, Float> {
    val widthScale = viewSize.width.toFloat() / imageSize.width.toFloat()
    val heightScale = viewSize.height.toFloat() / imageSize.height.toFloat()
    return Pair(widthScale, heightScale)
}

object OnlineOCR {
    private const val API_KEY = BuildConfig.API_KEY
    private const val SHA_CERT = BuildConfig.SHA_CERT
    private const val PACKAGE_NAME = "com.ocrtts"
    private const val TAG = "OnlineOCR"

    private val vision = Vision.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), null)
        .setApplicationName(PACKAGE_NAME)
        .setVisionRequestInitializer(object : VisionRequestInitializer(API_KEY) {
            @Throws(IOException::class)
            override fun initializeVisionRequest(visionRequest: VisionRequest<*>) {
                super.initializeVisionRequest(visionRequest)
                visionRequest.requestHeaders.set("X-Android-Package", PACKAGE_NAME)
                visionRequest.requestHeaders.set("X-Android-Cert", SHA_CERT)
            }
        })
        .build()

    private val feature = Feature().apply {
        type = "DOCUMENT_TEXT_DETECTION"
        model = "builtin/latest"
    }

    suspend fun analyzeOCR(bitmap: Bitmap, onlyDetect: Boolean, scaleFactor: Pair<Float, Float> = Pair(0f, 0f), onTextRecognized: (OCRText, Boolean) -> Unit, isReset: Boolean): Boolean {
        var hasText = false

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64EncodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        val image = Image().apply {
            content = base64EncodedImage
        }

        val requestList = ArrayList<AnnotateImageRequest>()
        val request = AnnotateImageRequest().apply {
            setImage(image)
            features = listOf(feature)
        }
        requestList.add(request)

        val batchRequest = BatchAnnotateImagesRequest().apply {
            requests = requestList
        }

        try {
            val annotateRequest = vision.images().annotate(batchRequest)
            annotateRequest.disableGZipContent = true

            val responses = annotateRequest.execute()
            val response = responses.responses.firstOrNull()
            val text = response?.fullTextAnnotation?.text ?: ""
            if (text.isNotBlank()) {
                hasText = true
            }

            if (onlyDetect) {
                onTextRecognized(OCRText(text), isReset)
            } else {
                convertToOCRText(response, scaleFactor, onTextRecognized, isReset)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing image for online text recognition: ${e.message}")
            Log.i(TAG, "Processing image using OfflineOCR now")
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            return OfflineOCR.analyzeOCR(inputImage, onlyDetect, scaleFactor, onTextRecognized, true)
        }

        return hasText
    }

    private suspend fun convertToOCRText(response: AnnotateImageResponse?, scaleFactor: Pair<Float, Float>, onTextRecognized: (OCRText, Boolean) -> Unit, isReset: Boolean) {
        try {
            val widthScaleFactor = scaleFactor.first
            val heightScaleFactor = scaleFactor.second

            val fullText = response?.fullTextAnnotation?.pages?.firstOrNull()

            if (fullText == null) {
                onTextRecognized(OCRText(), isReset)
                return
            }

            for (block in fullText.blocks) {
                for (paragraph in block.paragraphs) {
                    val rect =
                        Rect(
                            top = (paragraph.boundingBox.vertices[0]?.y ?: 0) * heightScaleFactor,
                            bottom = (paragraph.boundingBox.vertices[2]?.y ?: 0) * heightScaleFactor,
                            left = (paragraph.boundingBox.vertices[0]?.x ?: 0) * widthScaleFactor,
                            right = (paragraph.boundingBox.vertices[2]?.x ?: 0) * widthScaleFactor,
                        )

                    var paragraphText = ""

                    for (word in paragraph.words) {
                        for (symbol in word.symbols) {
                            paragraphText += symbol.text
                        }
                        paragraphText += " "
                    }

                    onTextRecognized(OCRText(paragraphText.removeSpecialCharacters(), rect), isReset)
                }
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "Error converting response to OCRText: ${e.message}")
        }
    }
}

object OfflineOCR {
    private const val TAG = "OfflineOCR"
    private val textRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    suspend fun analyzeOCR(image: InputImage, onlyDetect: Boolean, scaleFactor: Pair<Float, Float> = Pair(0f, 0f), onTextRecognized: (OCRText, Boolean) -> Unit, isReset: Boolean): Boolean {
        var hasText = false
        suspendCoroutine { continuation ->
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(Unit)
                    val text = visionText.text
                    if (text.isNotBlank()) {
                        hasText = true
                    }

                    if (onlyDetect) {
                        onTextRecognized(OCRText(text = visionText.text), isReset)
                    }
                    else {
                        convertToOCRText(visionText.textBlocks, scaleFactor, onTextRecognized, isReset)
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resume(Unit)
                    Log.e(TAG, "Error processing image for offline text recognition: ${e.message}")
                    Log.i(TAG, "Retry processing image using OfflineOCR")
                    if (!isReset) {
                        CoroutineScope(Dispatchers.Main).launch {
                            hasText = analyzeOCR(image, onlyDetect, scaleFactor, onTextRecognized, true)
                        }
                    }
                }
        }

        return hasText
    }

    private fun convertToOCRText(texts: List<Text.TextBlock>, scaleFactor: Pair<Float, Float>, onTextRecognized: (OCRText, Boolean) -> Unit, isReset: Boolean) {
        val widthScaleFactor = scaleFactor.first
        val heightScaleFactor = scaleFactor.second

        for (text in texts) {
            if (text.boundingBox != null) {
                val textBlock = text.boundingBox!!
                onTextRecognized(
                    OCRText(
                        text.text.removeSpecialCharacters(),
                        Rect(
                            top = textBlock.top.toFloat() * heightScaleFactor,
                            bottom = textBlock.bottom.toFloat() * heightScaleFactor,
                            left = textBlock.left.toFloat() * widthScaleFactor,
                            right = textBlock.right.toFloat() * widthScaleFactor,
                        )
                    ),
                    isReset
                )
            }
        }
    }
}

private fun String.removeSpecialCharacters(): String {
    val pattern = Regex("[^A-Za-z0-9\\p{script=Han} .,:;()\"'!?\\-+=\$%&<>*#@]")

    return this.replace(pattern, "")
}