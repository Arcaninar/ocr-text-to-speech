package com.ocrtts.ui.screens

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ocrtts.history.DataStoreManager
import com.ocrtts.imageCacheFile
import com.ocrtts.ocr.analyzeImageOCR
import com.ocrtts.type.OCRText
import com.ocrtts.ui.components.CustomIconButton
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import com.ocrtts.ui.viewmodels.ImageViewModel
import com.ocrtts.ui.viewmodels.ImageViewModelFactory
import com.ocrtts.ui.viewmodels.SettingViewModel
import com.ocrtts.ui.viewmodels.TTSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

fun imageToBitmap(image: Image): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.capacity())
    buffer[bytes]
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
}

fun contains(rect: Rect, x: Float, y: Float): Boolean {
//    Log.i("ContainsFunction", "rect: " + rect.left + " " + rect.right + " " + rect.top + " " + rect.bottom)
//    Log.i("ContainsFunction", "tap: " + x.toInt() + " " + y.toInt())
    val offset = 10
    if (rect.left - offset <= x && rect.right + offset >= x && rect.top - offset <= y && rect.bottom + offset >= y) {
        return true
    }

    if (rect.right - offset <= x && rect.left + offset >= x && rect.top - offset <= y && rect.bottom + offset >= y) {
        return true
    }

    if (rect.right - offset <= x && rect.left + offset >= x && rect.bottom - offset <= y && rect.top + offset >= y) {
        return true
    }

    if (rect.left - offset <= x && rect.right + offset >= x && rect.bottom - offset <= y && rect.top + offset >= y) {
        return true
    }

    return false
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun ImageScreen(
    sharedViewModel: ImageSharedViewModel,
    navController: NavController,
    dataStoreManager: DataStoreManager,
    settingViewModel: SettingViewModel,
    ttsViewModel: TTSViewModel,
    modifier: Modifier = Modifier,
    viewModel: ImageViewModel = viewModel(
        factory = ImageViewModelFactory(LocalContext.current.applicationContext as Application, settingViewModel)
    )
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }

    val zoom = remember { mutableFloatStateOf(1f) }
    val offsetX = remember { mutableFloatStateOf(0f) }
    val offsetY = remember { mutableFloatStateOf(0f) }
    val angle = remember { mutableFloatStateOf(0f) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val speed by settingViewModel.speedRate.collectAsState()
//    val language by settingViewModel.langModel.collectAsState()
    var showDialog by viewModel.showDialog

    // sentinel
    DisposableEffect(Unit) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                ttsViewModel.stopAllTTS()
            }
        }
        lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }



    LaunchedEffect(interactionSource) {
        val TAG = "ImagePress"
        interactionSource.interactions.collectLatest { interaction ->
            if (!viewModel.isFinishedAnalysing || viewModel.ocrTextList.isEmpty()) {
                return@collectLatest
            }


            when (interaction) {
                is PressInteraction.Press -> {
                    val isLongClick = viewModel.longTouchCounter

                    val position = interaction.pressPosition
                    var hasText = false
                    for (text in viewModel.ocrTextList) {
                        if (contains(text.rect, position.x, position.y)) {
                            viewModel.updateTextRectSelected(text)
                            Log.i(TAG, "Selected Text: " + text.text)
                            hasText = true
                            break
                        }
                    }

                    if (!hasText) {
                        viewModel.updateTextRectSelected(OCRText())
                    }

                    delay(500L)
                    if (viewModel.longTouchCounter == isLongClick && hasText) {
                        // TODO: Text to Speech
                        ttsViewModel.speak(viewModel.ocrTextSelected.text, 1.0f)
                    }
                }

                is PressInteraction.Release -> {
                    viewModel.incrementLongTouch()
                }
            }
        }
    }

    val activity = context as Activity
    val image = sharedViewModel.image.collectAsStateWithLifecycle().value!!
    val viewSize = sharedViewModel.size
    val orientation = sharedViewModel.orientation.collectAsStateWithLifecycle().value
    activity.requestedOrientation = orientation

    DisposableEffect(Unit) {
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }


    LaunchedEffect(viewModel.isFinishedAnalysing) {
        if (!viewModel.isFinishedAnalysing) {
            analyzeImageOCR(viewSize, image, viewModel::onTextRecognized)
        }
    }

    Surface(modifier = modifier.background(Color.Transparent)) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewModel.isFinishedAnalysing) {
                if (viewModel.ocrTextList.isEmpty()) {
                    val cachePath = imageCacheFile.absolutePath
                    val cacheImage = BitmapFactory.decodeFile(cachePath)
                    sharedViewModel.updateImage(cacheImage)
                    viewModel.resetFinishedAnalysing()
                } else {
                    val isFromHistory by sharedViewModel.isFromHistory.collectAsStateWithLifecycle()

                    viewModel.saveImageToFile(isFromHistory, image, orientation, viewSize, context.filesDir, dataStoreManager)

                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = zoom.floatValue,
                                scaleY = zoom.floatValue,
                                rotationZ = angle.floatValue,
                                translationX = offsetX.floatValue,
                                translationY = offsetY.floatValue
                            )
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    zoom.floatValue *= gestureZoom
                                    offsetX.floatValue += pan.x
                                    offsetY.floatValue += pan.y
                                }
                            }
                            .clickable(interactionSource = interactionSource, indication = null) {}
                    )

                    // INFO: this code is for testing purpose only, please comment it when pushing to github
//                    val test = viewModel.ocrTextList
//                    for (text in test) {
//                        val box = text.rect
//                        Canvas(modifier = Modifier
//                            .fillMaxSize()
//                            .graphicsLayer(
//                                scaleX = zoom.value,
//                                scaleY = zoom.value,
//                                translationX = offsetX.value,
//                                translationY = offsetY.value
//                            )
//                        ) {
//                            val path = Path().apply {
//                                addRect(
//                                    rect = Rect(
//                                        left = box.left,
//                                        right = box.right,
//                                        top = box.top,
//                                        bottom = box.bottom
//                                    )
//                                )
//                            }
//                            drawPath(path, color = Color.Yellow.copy(alpha = 0.5f))
//                        }
//                    }

                    if (viewModel.ocrTextSelected.text.isNotBlank()) {
                        val box = viewModel.ocrTextSelected.rect
                        Canvas(modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = zoom.floatValue,
                                scaleY = zoom.floatValue,
                                translationX = offsetX.floatValue,
                                translationY = offsetY.floatValue
                            )
                        ) {
                            val path = Path().apply {
                                addRect(
                                    rect = Rect(
                                        left = box.left,
                                        right = box.right,
                                        top = box.top,
                                        bottom = box.bottom
                                    )
                                )
                            }
                            drawPath(path, color = Color.Yellow.copy(alpha = 0.5f))
                        }
                    }
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(64.dp)
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            CustomIconButton(
                icon = Icons.Rounded.PhotoCamera,
                description = "Back to video",
                modifier = Modifier.align(Alignment.TopStart),
                innerPadding = 5.dp
            ) {
                navController.navigate(Screens.CameraScreen.route) {
                    popUpTo(Screens.ImageScreen.route) { inclusive = true }
                }
            }
            CustomIconButton(
                icon = Icons.Rounded.History,
                description = "Go to History",
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                navController.navigate(Screens.HistoryScreen.route) {
                    popUpTo(Screens.ImageScreen.route) { inclusive = true }
                }
            }
        }

        //Alert box
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Network Available") },
                text = { Text("Network connection detected. Please manually switch to online Text to Speech") },
                confirmButton = {
                    Button(onClick = { navController.navigate(Screens.SettingScreen.route) }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("No")
                    }
                }
            )
        }
    }
}