package com.ocrtts.ui.camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.media.MediaPlayer
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.text.Text
import com.ocrtts.R
import com.ocrtts.ui.MainViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TextRect(
    val text: String = "",
    var rect: androidx.compose.ui.geometry.Rect = androidx.compose.ui.geometry.Rect(0f, 0f, 0f, 0f)
)

@Composable
fun CameraScreen(viewModel: MainViewModel, modifier: Modifier = Modifier, navigate: (fileName: String) -> Unit) {
    var currentContext = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController = LifecycleCameraController(LocalContext.current)
    val audio = MediaPlayer.create(LocalContext.current, R.raw.ding)

    fun onTextUpdated(updatedText: Text, rotation: Int) {
        // 直接更新文本列表，不进行旋转操作
        val textRects = updatedText.textBlocks.map { textBlock ->
            val boundingBox = textBlock.boundingBox
            if (boundingBox != null) {
                TextRect(
                    text = textBlock.text,
                    rect = androidx.compose.ui.geometry.Rect(
                        top = boundingBox.top.toFloat(),
                        bottom = boundingBox.bottom.toFloat(),
                        right = boundingBox.right.toFloat(),
                        left = boundingBox.left.toFloat()
                    )
                )
            } else {
                TextRect()
            }
        }
        viewModel.setTextRectList(textRects)

        if (updatedText.text.isNotBlank()) {
            if (!viewModel.previousHasText.value) {
                audio.start()
                viewModel.setPreviousHasText(true)
            }
        } else {
            viewModel.setPreviousHasText(false)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { paddingValues: PaddingValues ->
        Box(contentAlignment = Alignment.BottomEnd) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .clickable { Log.w("Test", "Test") },
                factory = { context ->
                    currentContext = context
                    PreviewView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(Color.hashCode())
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_START

                    }.also { previewView ->
                        startTextRecognition(
                            context = context,
                            lifecycleOwner = lifecycleOwner,
                            cameraController = cameraController,
                            previewView = previewView,
                            onDetectedTextUpdated = ::onTextUpdated,
                            viewModel = viewModel
                        )
                    }
                }
            )

            if (viewModel.textRectList.value.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                    .offset(y = (-20).dp)
                    .fillMaxWidth()) {
                    Text(text = "There is a text in front of you. Click the button below to view it",
                        modifier = Modifier
                            .background(Color.Yellow, RoundedCornerShape(50))
                            .padding(5.dp)
                            .fillMaxWidth(0.8f)
                    )
                    Button(
                        onClick = {
                            onClickButton(context = currentContext, cameraController = cameraController, viewModel, navigate)
                        }) {
                        CircleShape
                    }

                }
            }
        }

    }
}

private fun onClickButton(context: Context, cameraController: LifecycleCameraController, viewModel: MainViewModel, navigate: (fileName: String) -> Unit) {
    cameraController.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            @OptIn(ExperimentalGetImage::class)
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val bitmap = image.image?.let { convertImageToBitmap(it) }
                    bitmap?.let {
                        val filePath = saveBitmapToFile(context, it)
                        saveFileNameToSharedPreferences(context, filePath)
                        viewModel.setImageFilePath(filePath)
                        navigate(filePath) // Navigate with file path
                    }
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                } finally {
                    image.close() // Ensure ImageProxy is closed
                }
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                exception.printStackTrace()
            }
        }
    )
}

private fun convertImageToBitmap(image: Image): Bitmap? {
    return try {
        if (image.planes.isNotEmpty()) {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }
    } catch (e: IllegalStateException) {
        e.printStackTrace()
        null
    }
}

private fun saveBitmapToFile(context: Context, bitmap: Bitmap): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val currentDateAndTime: String = sdf.format(Date())
    val fileName = "IMG_$currentDateAndTime.jpg"

    val file = File(context.filesDir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }

    Log.d("CameraScreen", "Saved file to: ${file.absolutePath}") // Log the file path
    return file.absolutePath // Return the absolute path of the file
}

private fun saveFileNameToSharedPreferences(context: Context, fileName: String) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("photo_history", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPreferences.edit()

    val currentTime = System.currentTimeMillis()
    editor.putString(currentTime.toString(), fileName)
    editor.apply()

    Log.d("CameraScreen", "Saved file path to SharedPreferences: $fileName at time $currentTime")
}

@SuppressLint("ClickableViewAccessibility")
private fun startTextRecognition(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraController: LifecycleCameraController,
    previewView: PreviewView,
    onDetectedTextUpdated: (Text, Int) -> Unit,
    viewModel: MainViewModel
) {

    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated)
    )

    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController

    previewView.isClickable = true

}