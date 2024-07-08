package com.ocrtts.ui.screens

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ocrtts.R
import com.ocrtts.camera.CameraTextAnalyzer
import com.ocrtts.history.DataStoreManager
import com.ocrtts.type.OCRText
import com.ocrtts.ui.viewmodels.CameraViewModel
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
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

@Composable
fun CameraScreen(
    navController: NavController,
    sharedViewModel: ImageSharedViewModel,
    dataStoreManager: DataStoreManager,
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
            .also { analysis ->
                val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
                analysis.setAnalyzer(ContextCompat.getMainExecutor(context), CameraTextAnalyzer(viewModel::updateRecognizedText, coroutineScope))
            }
    }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val viewPort = previewView.viewPort
        val useCaseGroup = UseCaseGroup.Builder().setViewPort(viewPort!!).addUseCase(preview).addUseCase(imageAnalysis).addUseCase(imageCapture).build()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
    }
    BackHandler {
        activity?.finish() // 结束当前Activity
    }
    Box(contentAlignment = Alignment.BottomEnd, modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    sharedViewModel.updateSize(coordinates.size)
                }
        )
        if (viewModel.recognizedText) {
            NotifyUser(
                imageCapture = imageCapture,
                navController = navController,
                sharedViewModel = sharedViewModel,
                dataStoreManager = dataStoreManager
            )
        }
    }
}

@Composable
fun NotifyUser(imageCapture: ImageCapture, navController: NavController, sharedViewModel: ImageSharedViewModel, dataStoreManager: DataStoreManager, modifier: Modifier = Modifier) {
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
                onClickButton(
                    imageCapture = imageCapture,
                    context = context,
                    navController = navController,
                    sharedViewModel = sharedViewModel,
                    dataStoreManager = dataStoreManager
                )
            }) {
            CircleShape
        }
    }
}

fun onClickButton(imageCapture: ImageCapture, context: Context, navController: NavController, sharedViewModel: ImageSharedViewModel, dataStoreManager: DataStoreManager) {
    val TAG = "ImageCapture"
    val outputDirectory = context.filesDir
    val photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val path = photoFile.absolutePath
                Log.d(TAG, "Photo capture succeeded: $path")
                // Update ViewModel with the captured image file
                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreManager.addImageToHistory(photoFile.absolutePath)
                }
                sharedViewModel.setFileName(path)
                navController.navigate(Screens.ImageScreen.route)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exception.message}")
            }
        }
    )
}

@Composable
fun OverlayTexts(OCRTexts: List<OCRText>, modifier: Modifier=Modifier) {
    Box(modifier = modifier) {
        OCRTexts.forEach { textRect ->
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
                    .background(Color.Yellow.copy(alpha = 0.5f))
//                    .clickable { /* Handle text selection */ }
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