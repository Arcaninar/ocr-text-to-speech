package com.ocrtts.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ocrtts.R
import com.ocrtts.history.DataStoreManager
import com.ocrtts.imageCacheFile
import com.ocrtts.ocr.analyzeImageOCR
import com.ocrtts.type.OCRText
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import com.ocrtts.ui.viewmodels.ImageViewModel
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
    if (rect.left - 25 <= x && rect.right + 25 >= x && rect.top - 25 <= y && rect.bottom + 25 >= y) {
        return true
    }

    if (rect.right - 25 <= x && rect.left + 25 >= x && rect.top - 25 <= y && rect.bottom + 25 >= y) {
        return true
    }

    if (rect.right - 25 <= x && rect.left + 25 >= x && rect.bottom - 25 <= y && rect.top + 25 >= y) {
        return true
    }

    if (rect.left - 25 <= x && rect.right + 25 >= x && rect.bottom - 25 <= y && rect.top + 25 >= y) {
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
    modifier: Modifier = Modifier,
    viewModel: ImageViewModel = viewModel(),
    ttsViewModel: TTSViewModel = viewModel()
) {

     val context = LocalContext.current
     val interactionSource = remember { MutableInteractionSource() }
     val lifecycle = LocalLifecycleOwner.current.lifecycle

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
        interactionSource.interactions.collectLatest { interaction ->
            if (!viewModel.isFinishedAnalysing || viewModel.ocrTextList.isEmpty()) {
                return@collectLatest
            }

            val TAG = "ImagePress"
            when (interaction) {
                is PressInteraction.Press -> {
                    val isLongClick = viewModel.longTouchCounter

                    val position = interaction.pressPosition
                    var hasText = false
                    for (text in viewModel.ocrTextList) {
                        if (contains(text.rect, position.x, position.y)) {
                            viewModel.updateTextRectSelected(text)
                            Log.i(TAG + "SelectedText", text.text)
                            hasText = true
                            break
                        }
                    }

                    if (!hasText) {
                        viewModel.updateTextRectSelected(OCRText())
                    }

                    delay(3000L)
                    if (viewModel.longTouchCounter == isLongClick && hasText) {
                        Log.w(TAG, "Long press: ${viewModel.ocrTextSelected.text}")
                        // TODO: Text to Speech
                        ttsViewModel.speak(viewModel.ocrTextSelected.text, 1.0f)
//                        azureTTS.stopSynthesis()
//                        sAP(viewModel.ocrTextSelected.text, 1.0f, offlineTTS)
//                        synthesizeAndPlayText(viewModel.ocrTextSelected.text, "en-US", 1.0f, AzureTextSynthesis("en-GB-SoniaNeural"))

                    }
                }

                is PressInteraction.Release -> {
                    viewModel.incrementLongTouch()
                    Log.w(TAG, "Release press: ${viewModel.longTouchCounter}")
                }
            }
        }
    }

//    val context = LocalContext.current
    val activity = context as Activity
    val image = sharedViewModel.image.collectAsStateWithLifecycle().value!!
    val viewSize = sharedViewModel.size

    activity.requestedOrientation = sharedViewModel.orientation

    DisposableEffect(Unit) {
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }


    LaunchedEffect(viewModel.isFinishedAnalysing) {
        if (!viewModel.isFinishedAnalysing) {
            analyzeImageOCR(viewSize = viewSize, image, viewModel::onTextRecognized)
        }
    }

    Surface(modifier = modifier.background(Color.Transparent)) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewModel.isFinishedAnalysing) {
                if (viewModel.ocrTextList.isEmpty()) {
                    val cachePath = imageCacheFile.absolutePath
                    val cacheImage = BitmapFactory.decodeFile(cachePath)
                    sharedViewModel.setImageInfo(cacheImage)
                    viewModel.resetFinishedAnalysing()
                }
                else {
                    val isFromHistory by sharedViewModel.isFromHistory.collectAsStateWithLifecycle()
                    viewModel.saveImageToFile(isFromHistory, image, context.filesDir, dataStoreManager)
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {})
                    )
                    if (viewModel.ocrTextSelected.text.isNotBlank()) {
                        val box = viewModel.ocrTextSelected.rect
                        Canvas(modifier = Modifier.fillMaxSize()) {
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
//                    val test = viewModel.ocrTextList
//                    for (text in test) {
//                        val box = text.rect
//                        Canvas(modifier = Modifier.fillMaxSize()) {
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
                }
            }
            else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(64.dp)
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            IconButton(
                onClick = { navController.navigate(Screens.CameraScreen.route) },
                modifier = Modifier
                    .size(75.dp)
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back to video",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton(
                onClick = { navController.navigate(Screens.HistoryScreen.route) },
                modifier = Modifier
                    .size(75.dp)
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.history),
                    contentDescription = "History Icon",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}