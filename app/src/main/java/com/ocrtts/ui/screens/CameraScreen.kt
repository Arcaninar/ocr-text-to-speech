
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
//import androidx.navigation.NavController
//import com.ocrtts.ui.viewmodels.MainViewModel
//import com.ocrtts.ui.camera.TextRecognitionAnalyzer
//
//data class TextRect(
//    val text: String = "",
//    var rect: Rect = Rect(0f, 0f, 0f, 0f)
//)
//
//@Composable
//fun CameraScreen(navController: NavController, viewModel: MainViewModel) {
//
//}
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
//        } else {
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
//                        startTextRecognition(
//                            context = context,
//                            lifecycleOwner = lifecycleOwner,
//                            cameraController = cameraController,
//                            previewView = previewView,
//                            onDetectedTextUpdated = ::onTextUpdated
//                        )
//                    }
//                }
//            )
//
//            if (viewModel.textRectList.value.isNotEmpty()) {
//                Column(
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier
//                        .offset(y = (-20).dp)
//                        .fillMaxWidth()
//                ) {
//                    Text(
//                        text = "There is a text in front of you. Click the button below to view it",
//                        modifier = Modifier
//                            .background(Color.Yellow, RoundedCornerShape(50))
//                            .padding(5.dp)
//                            .fillMaxWidth(0.8f)
//                    )
//                    Button(
//                        onClick = {
//                            onClickButton(
//                                context = currentContext,
//                                cameraController = cameraController,
//                                viewModel,
//                                navigate
//                            )
//                        }) {
//                        CircleShape
//                    }
//                }
//            }
//        }
//    }
//}
//
//private fun onClickButton(
//    context: Context,
//    cameraController: LifecycleCameraController,
//    viewModel: MainViewModel,
//    navigate: () -> Unit
//) {
//    cameraController.takePicture(
//        ContextCompat.getMainExecutor(context),
//        object : ImageCapture.OnImageCapturedCallback() {
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
//    onDetectedTextUpdated: (Text, Int) -> Unit
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
//}
//
///*fun rotate(
//    textBlocks: List<Text.TextBlock>,
//    rotation: Int,
//    updateRectTextList: (List<TextRect>) -> Unit
//) {
//    Log.w("Rotation", rotation.toString())
//    val updatedTextRects: MutableList<TextRect> = mutableListOf()
//
//    when (rotation) {
//        180 -> {
//            modifyRectSize(
//                textBlocks,
//                updatedTextRects,
//                top = 2.25f,
//                bottom = 2.325f,
//                left = 2.1f,
//                right = 2.3f
//            )
//        }
//
//        270 -> {
//            modifyRectSize(
//                textBlocks,
//                updatedTextRects,
//                top = 2.25f,
//                bottom = 2.275f,
//                left = 2.025f,
//                right = 2.3f
//            )
//        }
//
//        0 -> {
//            modifyRectSize(
//                textBlocks,
//                updatedTextRects,
//                top = 2.225f,
//                bottom = 2.275f,
//                left = 2.2f,
//                right = 2.3f
//            )
//        }
//
//        else -> {
//            modifyRectSize(
//                textBlocks,
//                updatedTextRects,
//                top = 2.2f,
//                bottom = 2.25f,
//                left = 1.85f,
//                right = 2.25f
//            )
//        }
//    }
//    updateRectTextList(updatedTextRects)
//}*/
//
//private fun modifyRectSize(
//    textBlocks: List<Text.TextBlock>,
//    updatedTextRects: MutableList<TextRect>,
//    top: Float = 1f,
//    bottom: Float = 1f,
//    left: Float = 1f,
//    right: Float = 1f
//) {
//    for (text in textBlocks) {
//        if (text.boundingBox != null) {
//            val textBlock = text.boundingBox!!
//            updatedTextRects.add(
//                TextRect(
//                    text.text, Rect(
//                        top = textBlock.top.toFloat() * top,
//                        bottom = textBlock.bottom.toFloat() * bottom,
//                        left = textBlock.left.toFloat() * left,
//                        right = textBlock.right.toFloat() * right,
//                    )
//                )
//            )
//        }
//    }
//}

package com.ocrtts.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
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
import androidx.compose.runtime.Immutable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ocrtts.R
import com.ocrtts.camera.TextAnalyzer
import com.ocrtts.data.TextRect
import com.ocrtts.ui.viewmodels.CameraViewModel
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
    navController: NavController,
    sharedViewModel: ImageSharedViewModel,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel()
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
    Box(contentAlignment = Alignment.BottomEnd, modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        if (viewModel.recognizedText.value) {
            NotifyUser(imageCapture = imageCapture, navController = navController, sharedViewModel = sharedViewModel)
        }
    }

}

@Composable
fun NotifyUser(imageCapture: ImageCapture, navController: NavController, sharedViewModel: ImageSharedViewModel, modifier: Modifier = Modifier) {
    val audio = MediaPlayer.create(LocalContext.current, R.raw.ding)
    audio.start()
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .offset(y = (-20).dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "There is a text in front of you. Click the button below to view it",
            modifier = Modifier
                .background(Color.Yellow, RoundedCornerShape(50))
                .padding(5.dp)
                .fillMaxWidth(0.8f)
        )
        val context = LocalContext.current
        Button(
            onClick = {
                onClickButton(imageCapture = imageCapture, context = context, navController = navController, sharedViewModel = sharedViewModel)
            }) {
            CircleShape
        }
    }
}

fun onClickButton(imageCapture: ImageCapture, context: Context, navController: NavController, sharedViewModel: ImageSharedViewModel) {
    val TAG = "ImageCapture"
    imageCapture.takePicture(ContextCompat.getMainExecutor(context),
        object: ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                sharedViewModel.setUpImageProxy(image)
                navController.navigate(Screens.ImageScreen.route)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e(TAG, "Exception in capturing image: ${exception.message}")
            }
        }
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


