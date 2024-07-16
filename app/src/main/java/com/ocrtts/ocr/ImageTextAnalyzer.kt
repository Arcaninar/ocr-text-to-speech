package com.ocrtts.ocr


import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import androidx.exifinterface.media.ExifInterface
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequest
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.AnnotateImageRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse
import com.google.api.services.vision.v1.model.Feature
import com.google.api.services.vision.v1.model.Image
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.ocrtts.BuildConfig
import com.ocrtts.type.OCRText
import com.ocrtts.type.OnlineOCRResponse
import com.ocrtts.utils.TimingUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.checkerframework.checker.units.qual.s
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


private const val TAG = "ImageTextRecognitionAnalyzer"

object OnlineOCR {
    private const val API_KEY = BuildConfig.API_KEY
    private const val SHA_CERT = BuildConfig.SHA_CERT
    private val httpTransport = NetHttpTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()

    private val vision = Vision.Builder(httpTransport, jsonFactory, null)
        .setApplicationName("com.ocrtts")
        .setVisionRequestInitializer(object : VisionRequestInitializer(API_KEY) {
            @Throws(IOException::class)
            override fun initializeVisionRequest(visionRequest: VisionRequest<*>) {
                super.initializeVisionRequest(visionRequest)
                visionRequest.requestHeaders.set("X-Android-Package", "com.ocrtts")
                visionRequest.requestHeaders.set("X-Android-Cert", SHA_CERT)
            }
        })
        .build()

    private val feature = Feature().apply {
        type = "TEXT_DETECTION"
        model = "builtin/latest"
    }

    suspend fun analyzeOCR(imagePath: String, onlyDetect: Boolean, scaleFactor: Pair<Float, Float> = Pair(0f, 0f), onTextRecognized: (List<OCRText>) -> Unit) {
        withContext(Dispatchers.Default) {
            val encoded = Base64.encodeToString(File(imagePath).readBytes(), Base64.NO_WRAP)
            val image = Image().apply {
                content = encoded
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
//            withContext(Dispatchers.IO) {
                Log.i(TAG, "running onlineOCR")
                val annotateRequest = vision.images().annotate(batchRequest)
                annotateRequest.disableGZipContent = true
                val response = annotateRequest.execute()
                val result = response.responses.firstOrNull()?.fullTextAnnotation?.text ?: ""

                val exif = ExifInterface(imagePath)
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

                val rotationDegrees = when (rotation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }

                Log.i("Rotation OCR", rotationDegrees.toString())

                onTextRecognized(convertToOCRText(response, scaleFactor, rotationDegrees))

                if (result.isNotBlank()) {
                    Log.i(TAG, result)
                } else {

                }
//            }
            } catch (e: Exception) {
                Log.e("GoogleCloudVisionAnalyzer", "Error processing image: ${e.message}")
            }
        }
    }
}

object OfflineOCR {
    private val textRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    suspend fun analyzeOCR(image: InputImage, onlyDetect: Boolean, scaleFactor: Pair<Float, Float> = Pair(0f, 0f), onTextRecognized: (List<OCRText>) -> Unit) {
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
    }
}

// better to make this function as a class?
suspend fun analyzeOCR(viewSize: IntSize, imageSize: IntSize, imagePath: String, context: Context, onTextRecognized: (List<OCRText>) -> Unit) {
    // do online analysis if internet is online, otherwise offline
    val scaleFactor = getScaleFactor(viewSize, imageSize)

    val hasInternet = true
    if (hasInternet) {
        OnlineOCR.analyzeOCR(imagePath, false, scaleFactor, onTextRecognized)
    }
    else {
        val inputImage = InputImage.fromFilePath(context, Uri.fromFile(File(imagePath)))
        OfflineOCR.analyzeOCR(inputImage, false, scaleFactor, onTextRecognized)
    }
}

//suspend fun analyzeOCRCamera()

fun getScaleFactor(viewSize: IntSize, imageSize: IntSize): Pair<Float, Float> {
    val widthScale = viewSize.width.toFloat() / imageSize.width.toFloat()
    val heightScale = viewSize.height.toFloat() / imageSize.height.toFloat()
    return Pair(widthScale, heightScale)
}

suspend fun analyzeOCROnline(pathName: String, scaleFactor: Pair<Float, Float>, onTextRecognized: (List<OCRText>) -> Unit) {
    withContext(Dispatchers.Default) {
        val apiKey = BuildConfig.API_KEY
        val shaCertificate = BuildConfig.SHA_CERT
        val httpTransport = NetHttpTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        val vision = Vision.Builder(httpTransport, jsonFactory, null)
            .setApplicationName("com.ocrtts")
            .setVisionRequestInitializer(object : VisionRequestInitializer(apiKey) {
                @Throws(IOException::class)
                override fun initializeVisionRequest(visionRequest: VisionRequest<*>) {
                    super.initializeVisionRequest(visionRequest)
                    visionRequest.requestHeaders.set("X-Android-Package", "com.ocrtts")
                    visionRequest.requestHeaders.set("X-Android-Cert", shaCertificate)
                }
            })
            .build()

        val feature = Feature().apply {
            type = "TEXT_DETECTION"
            model = "builtin/latest"
        }

        val encoded = Base64.encodeToString(File(pathName).readBytes(), Base64.NO_WRAP)
        val image = Image().apply {
            content = encoded
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
            withContext(Dispatchers.IO) {
                Log.i(TAG, "running onlineOCR")
                val annotateRequest = vision.images().annotate(batchRequest)
                annotateRequest.disableGZipContent = true
                val response = annotateRequest.execute()
                val result = response.responses.firstOrNull()?.fullTextAnnotation?.text ?: ""
                if (result.isNotBlank()) {
                    Log.i(TAG, result)
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleCloudVisionAnalyzer", "Error processing image: ${e.message}")
        }
    }


//    val apiKey = BuildConfig.API_KEY
//    val httpClient = NetHttpTransport().createRequestFactory()
//    val url = "https://vision.googleapis.com/v1/images:annotate?key=$apiKey"
//    val headers = HttpHeaders()
//    headers["X-Android-Package"] = "com.ocrtts"
//    headers["X-Android-Cert"] = "00403C671C1C5F0ACBCC5E4EBCAB03C790AF5BB4"
//    headers["Content-Type"] = "application/json"
//
//    val encoded = Base64.encodeToString(File(pathName).readBytes(), Base64.NO_WRAP)
//    val request = OnlineOCRRequest(
//        listOf(
//            Request(
//                ImageBase64(encoded),
//                listOf(
//                    AnalysisFeature("DOCUMENT_TEXT_DETECTION", 1)
//                )
//            )
//        )
//    )
//    val requestBody = Gson().toJson(request)
//    Log.i("Test connection", requestBody)
//
//    val httpRequest = httpClient.buildPostRequest(GenericUrl(url), ByteArrayContent.fromString("application/json", requestBody))
//    httpRequest.headers = headers
//
//    try {
//        val response = httpRequest.execute()
//        val content = response.parseAsString()
//        val responseImage = Gson().fromJson(content, OnlineOCRResponse::class.java)
////        Log.i("Test connection", content)
//        onTextRecognized(convertToOCRText(responseImage, scaleFactor))
//        Log.i("Test connection", responseImage.responses[0].fullTextAnnotation.text)
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
}

suspend fun analyzeOCROffline(
    image: InputImage,
    onlyDetect: Boolean,
    scaleFactor: Pair<Float, Float> = Pair(0f, 0f),
    onTextRecognized: (List<OCRText>) -> Unit
) {
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
}

fun convertToOCRText(response: BatchAnnotateImagesResponse, scaleFactor: Pair<Float, Float>, rotation: Int): List<OCRText> {
    val ocrTexts: MutableList<OCRText> = mutableListOf()
    val widthScaleFactor = scaleFactor.first
    val heightScaleFactor = scaleFactor.second

    val fullText = response.responses.firstOrNull()?.fullTextAnnotation?.pages?.firstOrNull()
        ?: return ocrTexts

    for (block in fullText.blocks) {
        for (paragraph in block.paragraphs) {
            lateinit var rect: Rect
            if (rotation == 0 || rotation == 180) {
                rect = Rect(
                    top = paragraph.boundingBox.vertices[0].y * heightScaleFactor,
                    bottom = paragraph.boundingBox.vertices[2].y * heightScaleFactor,
                    left = paragraph.boundingBox.vertices[0].x * widthScaleFactor,
                    right = paragraph.boundingBox.vertices[2].x * widthScaleFactor,
                )
            }
            else {
                rect = Rect(
                    top = paragraph.boundingBox.vertices[0].x * heightScaleFactor,
                    bottom = paragraph.boundingBox.vertices[2].x * heightScaleFactor,
                    left = paragraph.boundingBox.vertices[0].y * widthScaleFactor,
                    right = paragraph.boundingBox.vertices[2].y * widthScaleFactor,
                )
            }

            var paragraphText = ""

            for (word in paragraph.words) {
                for (symbol in word.symbols) {
                    if (!symbol.text.contains("""~!@#\$%^&*()_+-={}[]\\|;:'\",<.>/?""")) {
                        paragraphText += symbol.text
                    }
                }
                paragraphText += " "
            }

            Log.i("paragraph", paragraphText)
            ocrTexts.add(OCRText(paragraphText, rect))
        }
    }

    return ocrTexts
}

fun convertToOCRText(texts: List<Text.TextBlock>, scaleFactor: Pair<Float, Float>): List<OCRText> {
    val ocrTexts: MutableList<OCRText> = mutableListOf()
    val widthScaleFactor = scaleFactor.first
    val heightScaleFactor = scaleFactor.second

    for (text in texts) {
        if (text.boundingBox != null) {
            val textBlock = text.boundingBox!!
            ocrTexts.add(
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

    return ocrTexts
}