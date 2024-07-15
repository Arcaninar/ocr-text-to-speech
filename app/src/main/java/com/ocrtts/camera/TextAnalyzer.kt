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
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Base64
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequest
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Context
import androidx.compose.ui.platform.LocalContext

//TODO
//Without try-catch, the exception may not be properly catched if the recognizer has error in initialization
//Add Simple Lock
//Try to have simple check for the ocr result

const val TAG = "TextRecognitionAnalyzer"

class TextAnalyzer(
    private val context: Context,
    private val onTextRecognized: (List<RecognizedTextBlock>,Int,Int,Int) -> Unit,
    private val coroutineScope: CoroutineScope) :
    ImageAnalysis.Analyzer {
    private var isLocked: Boolean = false
    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val vision: Vision
    private val feature: Feature
    private val requestList = ArrayList<AnnotateImageRequest>()
    private val apiKey="AIzaSyCHVkdvwpoxofw7FrCSCTQG0qSBonbJWmQ"

    init {
        val httpTransport = NetHttpTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        vision = Vision.Builder(httpTransport, jsonFactory, null)
            .setApplicationName("com.ocrtts")
            .setVisionRequestInitializer(object : VisionRequestInitializer(apiKey) {
                @Throws(IOException::class)
                override fun initializeVisionRequest(visionRequest: VisionRequest<*>) {
                    super.initializeVisionRequest(visionRequest)
                    visionRequest.requestHeaders.set("X-Android-Package", "com.ocrtts")
                    visionRequest.requestHeaders.set("X-Android-Cert", "f6e5ffd6981ea7eddc2f2c0312e777f468fc8057")
                }
            })
            .build()

        feature = Feature().apply {
            type = "TEXT_DETECTION"
            model = "builtin/latest"
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        if (!isLocked) {
            isLocked=true
            coroutineScope.launch {
                onlineOCR(imageProxy)
//                recognizeText(imageProxy, imageProxy.width, imageProxy.height, imageProxy.imageInfo.rotationDegrees)
            }
        }
        else{
            imageProxy.close()
        }
    }

    private suspend fun onlineOCR(imageProxy: ImageProxy) {
        withContext(Dispatchers.Default) {
            val image = imageProxy.toVisionImage()

            val request = AnnotateImageRequest().apply {
                setImage(image)
                features = listOf(feature)
            }
            requestList.add(request)

            val batchRequest = BatchAnnotateImagesRequest().apply {
                requests = requestList
            }

            try {
                Log.i(TAG,"running onlineOCR")
                val annotateRequest = vision.images().annotate(batchRequest)
                annotateRequest.disableGZipContent = true
                val response = annotateRequest.execute()
                val result = response.responses.firstOrNull()?.fullTextAnnotation?.text ?: ""
                if (result.isNotBlank()) {
                    Log.i(TAG,result)
//                    onTextRecognized(result)
                }
                requestList.clear()
            } catch (e: Exception) {
                Log.e("GoogleCloudVisionAnalyzer", "Error processing image: ${e.message}")
            } finally {
                isLocked = false
                imageProxy.close()
            }
        }
    }

    private fun ImageProxy.toVisionImage(): Image {
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
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        //save image for checking
//        saveImageToFile(imageBytes)
        return Image().apply {
            content = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        }
    }
    private fun saveImageToFile(imageBytes: ByteArray) {
        val file = File(context.getExternalFilesDir(null), "ocr_test_image.jpg")
        FileOutputStream(file).use { output ->
            output.write(imageBytes)
        }
        Log.i(TAG, "Image saved to: ${file.absolutePath}")
    }


    //Util function , no use
//    fun ImageProxy.toJpeg(): ByteArray {
//        val yBuffer = planes[0].buffer // Y
//        val uBuffer = planes[1].buffer // U
//        val vBuffer = planes[2].buffer // V
//
//        val ySize = yBuffer.remaining()
//        val uSize = uBuffer.remaining()
//        val vSize = vBuffer.remaining()
//
//        val nv21 = ByteArray(ySize + uSize + vSize)
//
//        //U and V are swapped
//        yBuffer.get(nv21, 0, ySize)
//        vBuffer.get(nv21, ySize, vSize)
//        uBuffer.get(nv21, ySize + vSize, uSize)
//
//        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
//        val out = ByteArrayOutputStream()
//        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
//        return out.toByteArray()
//    }
//    private fun convertImageProxyToJpeg(imageProxy: ImageProxy): ByteArray {
//        val yuvImage = YuvImage(
//            imageProxy.planes[0].buffer.toByteArray(),
//            ImageFormat.NV21,
//            imageProxy.width,
//            imageProxy.height,
//            null
//        )
//
//        val outputStream = ByteArrayOutputStream()
//        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, outputStream)
//        return outputStream.toByteArray()
//    }




    //OfflineOCR
//    @OptIn(ExperimentalGetImage::class)
//    private suspend fun recognizeText(image: ImageProxy,width: Int, height: Int,rotation: Int) {
//        val mediaImage = image.image
//        if (mediaImage != null) {
//            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
//
//            try {
//                withContext(Dispatchers.IO) {
//                    recognizer.process(inputImage)
//                        .addOnSuccessListener { visionText ->
////                            if(visionText.text.isNotBlank()){
////                                onTextRecognized(visionText.text)
////                            }
//                            processRecognizedText(visionText, width, height, rotation)
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e(TAG, "Error processing image for text recognition: ${e.message}")
//                        }
//                        .addOnCompleteListener {
//                            image.close() // Ensure to close the ImageProxy here
//                            isLocked = false
//                        }
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Exception in processing text recognition: ${e.message}")
//                image.close() // Close imageProxy if an exception occurs before addOnCompleteListener
//            }
//        } else {
//            Log.w(TAG, "No media image available for text recognition")
//            image.close() // Close imageProxy if mediaImage is null
//        }
//    }

//    private fun processRecognizedText(visionText: Text, imageWidth: Int, imageHeight: Int, rotation: Int) {
//        val recognizedTextBlocks = mutableListOf<RecognizedTextBlock>()
//        for (block in visionText.textBlocks) {
//            if(block.text.isBlank()|| block.cornerPoints.isNullOrEmpty()){
//                continue
//            }
//            Log.i(TAG," :"+block.cornerPoints?.toList().toString())
//            recognizedTextBlocks.add(RecognizedTextBlock(block.text,  block.cornerPoints?.toList() ?: emptyList()))
//        }
//        if (recognizedTextBlocks.isNotEmpty()) {
//            onTextRecognized(recognizedTextBlocks, imageWidth, imageHeight, rotation)
//        }
//    }

}



