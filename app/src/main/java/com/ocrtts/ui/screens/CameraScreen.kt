package com.ocrtts.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
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
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ocrtts.notificationSound
import com.ocrtts.ocr.CameraTextAnalyzer
import com.ocrtts.ui.viewmodels.CameraViewModel
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.concurrent.TimeUnit

@SuppressLint("ComposeViewModelInjection")
@Composable
fun CameraScreen(
    navController: NavController,
    sharedViewModel: ImageSharedViewModel,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
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
                analysis.setAnalyzer(ContextCompat.getMainExecutor(context), CameraTextAnalyzer(sharedViewModel, viewModel::updateRecognizedText, coroutineScope))
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
        val camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
        val cameraControl = camera.cameraControl

        previewView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.i("orientation", "cameraOrientation: " + camera.cameraInfo.sensorRotationDegrees)
                val factory = SurfaceOrientedMeteringPointFactory(previewView.width.toFloat(), previewView.height.toFloat())
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point)
                    .setAutoCancelDuration(5, TimeUnit.SECONDS)
                    .build()

                cameraControl.startFocusAndMetering(action)
            }

            if (event.action == MotionEvent.ACTION_UP) {
                previewView.performClick()
            }

            true
        }

    }, ContextCompat.getMainExecutor(context))

    DisposableEffect(Unit) {
        onDispose {
            cameraProviderFuture.get().unbindAll()
        }
    }

    BackHandler {
        navController.navigate(Screens.HomeScreen.route) {
            popUpTo(Screens.MainCameraScreen.route) { inclusive = true }
        }
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
            sharedViewModel = sharedViewModel
        )
    }
}

@Composable
fun NotifyUser(
    imageCapture: ImageCapture,
    navController: NavController,
    viewModel: CameraViewModel,
    sharedViewModel: ImageSharedViewModel,
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
                color = Color.Black,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(50))
                    .padding(5.dp)
                    .fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.padding(5.dp))
            val context = LocalContext.current
            val config = LocalConfiguration.current
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
                        viewModel.captureImage(
                            imageCapture = imageCapture,
                            context = context,
                            sharedViewModel = sharedViewModel,
                            navController = navController
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
        }
    }
    else {
      viewModel.updateHasText(false)
    }
}