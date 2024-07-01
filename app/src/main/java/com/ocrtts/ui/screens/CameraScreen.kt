//package com.ocrtts.ui.screens
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.media.MediaPlayer
//import android.util.Log
//import android.view.ViewGroup
//import android.widget.LinearLayout
//import androidx.annotation.OptIn
//import androidx.camera.core.AspectRatio
//import androidx.camera.core.ExperimentalGetImage
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageProxy
//import androidx.camera.view.CameraController
//import androidx.camera.view.LifecycleCameraController
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.Scaffold
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.LifecycleOwner
//import com.google.mlkit.vision.text.Text
//import com.ocrtts.R
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import com.ocrtts.utils.TimingUtility
//import com.ocrtts.ui.viewmodels.MainViewModel
//import com.ocrtts.camera.TextRecognitionAnalyzer
//
//data class TextRect(
//    val text: String = "",
//    var rect: Rect = Rect(0f, 0f, 0f, 0f)
//)
//
//@Composable
//fun CameraScreen(viewModel: MainViewModel, modifier: Modifier = Modifier, navigate: () -> Unit) {
//    var currentContext = LocalContext.current
//    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
//    val cameraController = LifecycleCameraController(LocalContext.current)
//    val audio = MediaPlayer.create(LocalContext.current, R.raw.ding)
//
//    fun onTextUpdated(updatedText: Text, rotation: Int) {
//        rotate(updatedText.textBlocks, rotation, viewModel::setTextRectList)
//        if (updatedText.text.isNotBlank()) {
//            if (!viewModel.previousHasText.value) {
//                audio.start()
//                viewModel.setPreviousHasText(true)
//            }
//        }
//        else {
//            viewModel.setPreviousHasText(false)
//        }
//    }
//
//    Scaffold(
//        modifier = modifier.fillMaxSize(),
//    ) { paddingValues: PaddingValues ->
//        Box(contentAlignment = Alignment.BottomEnd) {
//            AndroidView(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .clickable { Log.w("Test", "Test") },
//                factory = { context ->
//                    currentContext = context
//                    PreviewView(context).apply {
//                        layoutParams = LinearLayout.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.MATCH_PARENT
//                        )
//                        setBackgroundColor(Color.hashCode())
//                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//                        scaleType = PreviewView.ScaleType.FILL_START
//
//                    }.also { previewView ->
//                        TimingUtility.measureExecutionTime("recognition") {
//                            startTextRecognition(
//                                context = context,
//                                lifecycleOwner = lifecycleOwner,
//                                cameraController = cameraController,
//                                previewView = previewView,
//                                onDetectedTextUpdated = ::onTextUpdated,
//                                viewModel = viewModel
//                            )
//                        }
//
//                    }
//                }
//            )
//
//            if (viewModel.textRectList.value.isNotEmpty()) {
//                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
//                    .offset(y = (-20).dp)
//                    .fillMaxWidth()) {
//                    Text(text = "There is a text in front of you. Click the button below to view it",
//                        modifier = Modifier
//                            .background(Color.Yellow, RoundedCornerShape(50))
//                            .padding(5.dp)
//                            .fillMaxWidth(0.8f)
//                    )
//                    Button(
//                        onClick = {
//                            onClickButton(context = currentContext, cameraController = cameraController, viewModel, navigate)
//                        }) {
//                        CircleShape
//                    }
//                }
//            }
//        }
//
////        val textRectSelected = viewModel.textRectSelected.value
//
////        if (textRectSelected != null) {
////            Canvas(modifier = Modifier.fillMaxSize()) {
////                val box = textRectSelected.rect
////                val path = Path().apply {
////                    addRect(
////                        rect = Rect(
////                            left = box.left,
////                            right = box.right,
////                            top = box.top,
////                            bottom = box.bottom
////                        )
////                    )
////                }
////                drawPath(path, color = Color.Red, style = Stroke(width = 5f))
////            }
////        }
//    }
//}
//
//private fun onClickButton(context: Context, cameraController: LifecycleCameraController, viewModel: MainViewModel, navigate: () -> Unit) {
//    cameraController.takePicture(
//        ContextCompat.getMainExecutor(context),
//        object: ImageCapture.OnImageCapturedCallback() {
//            @OptIn(ExperimentalGetImage::class)
//            override fun onCaptureSuccess(image: ImageProxy) {
//                super.onCaptureSuccess(image)
//                if (image.image != null) {
//                    viewModel.setImageSelected(image.image)
//                    navigate()
//                }
//            }
//        }
//    )
//}
//
//
//@SuppressLint("ClickableViewAccessibility")
//private fun startTextRecognition(
//    context: Context,
//    lifecycleOwner: LifecycleOwner,
//    cameraController: LifecycleCameraController,
//    previewView: PreviewView,
//    onDetectedTextUpdated: (Text, Int) -> Unit,
//    viewModel: MainViewModel
//) {
//
//    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
//    cameraController.setImageAnalysisAnalyzer(
//        ContextCompat.getMainExecutor(context),
//        TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated)
//    )
//
//    cameraController.bindToLifecycle(lifecycleOwner)
//    previewView.controller = cameraController
//
//    previewView.isClickable = true
//
//
//
//    // custom LongTouchListener
////    previewView.setOnTouchListener { v, event ->
////        when (event?.action) {
////            MotionEvent.ACTION_DOWN -> {
////                val localCounter = viewModel.longTouchCounter.value
////
////                if (viewModel.textRectList.value.isNotEmpty()) {
////                    cameraController.takePicture(
////                        ContextCompat.getMainExecutor(context),
////                        object: ImageCapture.OnImageCapturedCallback() {
////                            override fun onCaptureSuccess(image: ImageProxy) {
////                                super.onCaptureSuccess(image)
////                            }
////                        }
////                    )
////                }
////
//////                for (text in viewModel.textRectList.value) {
//////                    if (contains(text.rect, event.x, event.y)) {
//////                        viewModel.setTextRectSelected(text)
//////                    }
//////                }
////
////                CoroutineScope(Dispatchers.Main).launch {
////                    delay(2000L)
////                    if (localCounter == viewModel.longTouchCounter.value) {
////                    Log.w("Test", "Cords: " + event.x.toString() + ", " + event.y.toString())
////                        for (text in viewModel.textRectList.value) {
////                            Log.w("Test", "Box: (x | y)" + text.rect.left.toString() + ", " + text.rect.right.toString() + " | " + text.rect.top.toString() + ", " + text.rect.bottom.toString())
////                            if (contains(text.rect, event.x, event.y)) {
////                                // TODO: Text to Speech the text here
////                                Log.w("Test", "the text: " + text.text)
////                            }
////                        }
////                    }
////                }
////            }
////
////            MotionEvent.ACTION_UP -> {
////                viewModel.incrementLongTouch()
////            }
////        }
////
////        v?.onTouchEvent(event) ?: true
////    }
//}
//
//
//fun rotate(textBlocks: List<Text.TextBlock>, rotation: Int, updateRectTextList: (List<TextRect>) -> Unit) {
//    Log.w("Rotation", rotation.toString())
//    val updatedTextRects: MutableList<TextRect> = mutableListOf()
//
//    when (rotation) {
//        180 -> {
//            for (text in textBlocks) {
//                if (text.boundingBox != null) {
//                    val textBlock = text.boundingBox!!
//                    updatedTextRects.add(
//                        TextRect(text.text, Rect(
//                        top = textBlock.top.toFloat() * 2.25f,
//                        bottom = textBlock.bottom.toFloat() * 2.325f,
//                        right = textBlock.right.toFloat() * 2.3f,
//                        left = textBlock.left.toFloat() * 2.1f
//                    ))
//                    )
//                }
//            }
//        }
//        270 -> {
//            for (text in textBlocks) {
//                if (text.boundingBox != null) {
//                    val textBlock = text.boundingBox!!
//                    updatedTextRects.add(
//                        TextRect(text.text, Rect(
//                        top = textBlock.top.toFloat() * 2.25f,
//                        bottom = textBlock.bottom.toFloat() * 2.275f,
//                        right = textBlock.right.toFloat() * 2.3f,
//                        left = textBlock.left.toFloat() * 2.025f
//                    ))
//                    )
//                }
//            }
//        }
//        0 -> {
//            for (text in textBlocks) {
//                if (text.boundingBox != null) {
//                    val textBlock = text.boundingBox!!
//                    updatedTextRects.add(
//                        TextRect(text.text, Rect(
//                        top = textBlock.top.toFloat() * 2.225f,
//                        bottom = textBlock.bottom.toFloat() * 2.275f,
//                        right = textBlock.right.toFloat() * 2.3f,
//                        left = textBlock.left.toFloat() * 2.2f
//                    ))
//                    )
//                }
//            }
//        }
//        else -> {
//            for (text in textBlocks) {
//                if (text.boundingBox != null) {
//                    val textBlock = text.boundingBox!!
//                    updatedTextRects.add(
//                        TextRect(text.text, Rect(
//                        top = textBlock.top.toFloat() * 2.2f,
//                        bottom = textBlock.bottom.toFloat() * 2.25f,
//                        right = textBlock.right.toFloat() * 2.25f,
//                        left = textBlock.left.toFloat() * 1.85f
//                    ))
//                    )
//                }
//            }
//        }
//    }
//    updateRectTextList(updatedTextRects)
//}
//
//private fun contains(rect: Rect, x: Float, y: Float): Boolean {
//    return rect.left - 25 <= x && rect.right + 25 >= x && rect.top - 25 <= y && rect.bottom + 25 >= y
//}

//TODO
//Still reading....
//Basically, try not to hard-code the things
//


package com.ocrtts.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.ocrtts.camera.TextAnalyzer
import com.ocrtts.ui.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class TextRect(
    val text: String,
    val rect: Rect
)

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.run { get() })
            }, ContextCompat.getMainExecutor(this))
        }
    }

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScreen(
    viewModel: MainViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    val preview = Preview.Builder().build()
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {analysis ->
                val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
                analysis.setAnalyzer(ContextCompat.getMainExecutor(context), TextAnalyzer(viewModel::updateRecognizedText, coroutineScope))
            }
    }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture,imageAnalysis)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    BackHandler {
        activity?.finish() // 結束當前Activity
    }
    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}


@Composable
fun OverlayTexts(textRects: List<TextRect>, modifier: Modifier=Modifier) {
    Box(modifier = modifier) {
        textRects.forEach { textRect ->
            Box(
                modifier = Modifier
                    .offset(
                        x = textRect.rect.left.dp,
                        y = textRect.rect.top.dp
                    )
                    .size(
                        width = (textRect.rect.right - textRect.rect.left).dp,
                        height = (textRect.rect.bottom - textRect.rect.top).dp
                    )
                    .background(Color.Transparent)
                    .clickable { /* Handle text selection */ }
            ) {
                Text(text = textRect.text, color = Color.White)
            }
        }
    }
}

@Composable
fun ControlButtons(modifier: Modifier=Modifier, onCapture: () -> Unit) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onCapture, shape = CircleShape) {
            Text(text = "Capture")
        }
    }
}
