package com.ocrtts.ocr

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.YuvImage
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalGetImage::class)
suspend fun analyzeCameraOCR(image: ImageProxy, viewModel: ImageSharedViewModel, onTextRecognized: (OCRText) -> Unit) {
    // depends on user setting whether want to be accurate, or faster and save more battery
    val hasText: Boolean
    val rotation = image.imageInfo.rotationDegrees
    val useOnline = false

    if (useOnline) {
        val base64EncodedImage = image.convertToBase64()
        hasText = OnlineOCR.analyzeOCR(base64EncodedImage, true, onTextRecognized = onTextRecognized)
    }
    else {
        val inputImage = InputImage.fromMediaImage(image.image!!, rotation)
        hasText = OfflineOCR.analyzeOCR(inputImage, true, onTextRecognized = onTextRecognized)
    }

    withContext(Dispatchers.IO) {
        if (hasText) {
            saveImageCache(image.toBitmap(), rotation, viewModel.size)
        }
    }
}

private fun ImageProxy.convertToBase64(): String {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
    val imageBytes = out.toByteArray()

    return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
}

private fun saveImageCache(image: Bitmap, rotationDegree: Int, screenSize: IntSize) {
    val finalImage = modifyBitmap(image, rotationDegree, screenSize)
    saveBitmapToFile(imageCacheFile, finalImage)
    Log.i("ImageCache", "Image saved to: ${imageCacheFile.absolutePath}")
}

suspend fun analyzeImageOCR(viewSize: IntSize, image: Bitmap, onTextRecognized: (OCRText) -> Unit) {
    val scaleFactor = getScaleFactor(viewSize, IntSize(image.width, image.height))

    // do online analysis if internet is online, otherwise offline
    val hasInternet = true
    if (hasInternet) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64EncodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        OnlineOCR.analyzeOCR(base64EncodedImage, false, scaleFactor, onTextRecognized)
    }
    else {
        val inputImage = InputImage.fromBitmap(image, 0)
        OfflineOCR.analyzeOCR(inputImage, false, scaleFactor, onTextRecognized)
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

    suspend fun analyzeOCR(base64EncodedImage: String, onlyDetect: Boolean, scaleFactor: Pair<Float, Float> = Pair(0f, 0f), onTextRecognized: (OCRText) -> Unit): Boolean {
        var hasText = false

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
            Log.i(TAG, "running onlineOCR")
            val annotateRequest = vision.images().annotate(batchRequest)
            annotateRequest.disableGZipContent = true
            withContext(Dispatchers.Default) {
                val responses = annotateRequest.execute()
                val response = responses.responses.firstOrNull()
                val text = response?.fullTextAnnotation?.text ?: ""
                if (text.isNotBlank()) {
                    hasText = true
                }

                if (onlyDetect) {
                    onTextRecognized(OCRText(text))
                }
                else {
                    convertToOCRText(response, scaleFactor, onTextRecognized)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image for online text recognition: ${e.message}")
        }

        return hasText
    }

    private suspend fun convertToOCRText(response: AnnotateImageResponse?, scaleFactor: Pair<Float, Float>, onTextRecognized: (OCRText) -> Unit) {
        try {
            val widthScaleFactor = scaleFactor.first
            val heightScaleFactor = scaleFactor.second

            val fullText = response?.fullTextAnnotation?.pages?.firstOrNull()

            if (fullText == null) {
                onTextRecognized(OCRText())
                return
            }

            withContext(Dispatchers.Main) {
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

                        onTextRecognized(OCRText(paragraphText, rect))
                    }
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

    suspend fun analyzeOCR(image: InputImage, onlyDetect: Boolean, scaleFactor: Pair<Float, Float> = Pair(0f, 0f), onTextRecognized: (OCRText) -> Unit): Boolean {
        var hasText = false
        Log.i(TAG, "running offlineOCR")
        suspendCoroutine { continuation ->
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(Unit)
                    val text = visionText.text
                    if (text.isNotBlank()) {
                        hasText = true
                    }

                    if (onlyDetect) {
                        onTextRecognized(OCRText(text = visionText.text))
                    }
                    else {
                        convertToOCRText(visionText.textBlocks, scaleFactor, onTextRecognized)
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resume(Unit)
                    Log.e(TAG, "Error processing image for offline text recognition: ${e.message}")
                }
        }

        return hasText
    }

    private fun convertToOCRText(texts: List<Text.TextBlock>, scaleFactor: Pair<Float, Float>, onTextRecognized: (OCRText) -> Unit) {
        val widthScaleFactor = scaleFactor.first
        val heightScaleFactor = scaleFactor.second

        for (text in texts) {
            if (text.boundingBox != null) {
                val textBlock = text.boundingBox!!
                onTextRecognized(
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
    }
}