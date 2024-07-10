package com.ocrtts.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.common.util.concurrent.ListenableFuture
import com.ocrtts.notificationSound
import com.ocrtts.camera.CameraTextAnalyzer
import com.ocrtts.history.DataStoreManager
import com.ocrtts.type.OCRText
import com.ocrtts.ui.viewmodels.CameraViewModel
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import com.ocrtts.utils.TimingUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import rotate
import java.io.File
import java.io.FileOutputStream
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

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val viewPort = previewView.viewPort!!
        val useCaseGroup = UseCaseGroup.Builder().setViewPort(viewPort).addUseCase(preview).addUseCase(imageAnalysis).addUseCase(imageCapture).build()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)

    }, ContextCompat.getMainExecutor(context))

//    please don't remove this comment, might be important but not sure lol
//    LaunchedEffect(lensFacing) {
//        val cameraProvider = context.getCameraProvider()
//        cameraProvider.unbindAll()
//        val preview = Preview.Builder().build()
//        preview.setSurfaceProvider(previewView.surfaceProvider)
//        val viewPort = previewView.viewPort!!
//        viewModel.updateViewPort(viewPort)
//        val useCaseGroup = UseCaseGroup.Builder().setViewPort(viewPort).addUseCase(preview).addUseCase(imageAnalysis).addUseCase(imageCapture).build()
//        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
//    }
    BackHandler {
        activity?.finish() // 结束当前Activity
    }
    Box(contentAlignment = Alignment.BottomEnd, modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { size ->
                    sharedViewModel.updateSize(size.size)
                }
        )

        NotifyUser(
            imageCapture = imageCapture,
            navController = navController,
            viewModel = viewModel,
            sharedViewModel = sharedViewModel,
            dataStoreManager = dataStoreManager,
            cameraProviderFuture = cameraProviderFuture
        )
    }
}

@Composable
fun NotifyUser(
    imageCapture: ImageCapture,
    navController: NavController,
    viewModel: CameraViewModel,
    sharedViewModel: ImageSharedViewModel,
    dataStoreManager: DataStoreManager,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    modifier: Modifier = Modifier,
) {
    if (viewModel.isRecognizedText.collectAsStateWithLifecycle().value) {
        if (!viewModel.hasTextBefore.collectAsStateWithLifecycle().value) {
            notificationSound.start()
            viewModel.updateHasText(true)
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .offset(y = (-10).dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "There is a text in front of you. Click the button below to view it",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(50))
                    .padding(5.dp)
                    .fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.padding(5.dp))
            val context = LocalContext.current
            Box(
                contentAlignment= Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
                    .clickable {
                        onClickButton(
                            imageCapture = imageCapture,
                            context = context,
                            navController = navController,
                            sharedViewModel = sharedViewModel,
                            dataStoreManager = dataStoreManager,
                            cameraProviderFuture = cameraProviderFuture
                        )
                    }
            ){
                //internal circle with icon
                Icon(
                    imageVector = Icons.Filled.Circle,
                    contentDescription = "contentDescription",
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color.White, CircleShape)
                        .padding(2.dp),
                    tint = Color.White
                )
            }
//            Button(
//                onClick = {
//                    onClickButton(
//                        imageCapture = imageCapture,
//                        context = context,
//                        navController = navController,
//                        sharedViewModel = sharedViewModel,
//                        dataStoreManager = dataStoreManager,
//                        cameraProviderFuture = cameraProviderFuture
//                    )
//
//
//                }) {
//                CircleShape
//            }
        }
    }
    else {
      viewModel.updateHasText(false)
    }
}

fun onClickButton(
    imageCapture: ImageCapture,
    context: Context,
    navController: NavController,
    sharedViewModel: ImageSharedViewModel,
    dataStoreManager: DataStoreManager,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
) {
    val TAG = "ImageCapture"
    val outputDirectory = context.filesDir
    val photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            @SuppressLint("RestrictedApi")
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val path = photoFile.absolutePath
                Log.d(TAG, "Photo capture succeeded: $path")

                CoroutineScope(Dispatchers.IO).launch {
                    dataStoreManager.addImageToHistory(photoFile.absolutePath)
                }

                val exif = ExifInterface(path)
                val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

                val rotationDegrees = when (rotation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }

//                TimingUtility.measureExecutionTime("rotate and scale bitmap") {
//                    val bitmap = BitmapFactory.decodeFile(path).rotate(rotationDegrees)
//
//                    val size = sharedViewModel.size
//                    val realBitmap = Bitmap.createScaledBitmap(bitmap, size.width, size.height, true)
//                }

                if (rotationDegrees != 0f) {
                    val bitmap = BitmapFactory.decodeFile(path).rotate(rotationDegrees)
                    val size = sharedViewModel.size
                    val realBitmap = Bitmap.createScaledBitmap(bitmap, size.width, size.height, true)

                    val outputStream = FileOutputStream(photoFile)
                    realBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.close()

//                    TimingUtility.measureExecutionTime("save file") {
//                        val outputStream = FileOutputStream(photoFile)
//                        realBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//                        outputStream.close()
//                    }
                }

                sharedViewModel.setFileName(path)
                cameraProviderFuture.get().unbindAll()
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