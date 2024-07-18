//package com.ocrtts.ui.screens
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.util.Log
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.gestures.detectTransformGestures
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.interaction.PressInteraction
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.rounded.ArrowBack
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.IntSize
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.ocrtts.R
//import com.ocrtts.ocr.adjustRect
//import com.ocrtts.ocr.analyzeOCR
//import com.ocrtts.type.OCRText
//import com.ocrtts.ui.viewmodels.ImageSharedViewModel
//import com.ocrtts.ui.viewmodels.ImageViewModel
//import com.ocrtts.base.AzureTextSynthesis
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//import java.io.File
//
//fun contains(rect: androidx.compose.ui.geometry.Rect, x: Float, y: Float): Boolean {
//    return rect.left - 25 <= x && rect.right + 25 >= x && rect.top - 25 <= y && rect.bottom + 25 >= y
//}
//
//@Composable
//fun ImageScreen(
//    sharedViewModel: ImageSharedViewModel,
//    navController: NavController,
//    modifier: Modifier = Modifier,
//    viewModel: ImageViewModel = viewModel()
//) {
//    val interactionSource = remember { MutableInteractionSource() }
//
//
//    val fileName by sharedViewModel.fileName.collectAsStateWithLifecycle()
//    val context = LocalContext.current
//
//    LaunchedEffect(fileName) {
//        val file = File(fileName)
//
//        Log.d("ImageScreen", "Attempting to decode file: $fileName")
//
//        if (!file.exists()) {
//            Log.e("ImageScreen", "File does not exist: $fileName")
//            return@LaunchedEffect
//        }
//
//        if (!file.canRead()) {
//            Log.e("ImageScreen", "Cannot read file: $fileName")
//            return@LaunchedEffect
//        }
//
//        try {
//            bitmap = BitmapFactory.decodeFile(file.absolutePath)
//            if (bitmap == null) {
//                Log.e("ImageScreen", "Failed to decode file: $fileName. The file might be corrupted or not a valid image format.")
//            } else {
//                Log.d("ImageScreen", "Successfully decoded file: $fileName")
//            }
//        } catch (e: Exception) {
//            Log.e("ImageScreen", "Exception during decoding file: $fileName", e)
//        }
//    }
//
//    if (bitmap == null) {
//        Log.e("ImageScreen", "Bitmap is null after decoding")
//        return
//    }
//
//    LaunchedEffect(interactionSource) {
//        interactionSource.interactions.collectLatest { interaction ->
//            val TAG = "ImagePress"
//            when (interaction) {
//                is PressInteraction.Press -> {
//                    Log.w(TAG + "test", "Pressed: ${interaction.pressPosition}")
//                    val isLongClick = viewModel.longTouchCounter
//
//                    val position = interaction.pressPosition
//                    var hasText = false
//                    for (text in viewModel.ocrTextList ?: listOf()) {
//                        if (contains(text.rect, position.x, position.y)) {
//                            viewModel.updateTextRectSelected(text)
//                            hasText = true
//                            break
//                        }
//                    }
//
//                    if (!hasText) {
//                        viewModel.updateTextRectSelected(OCRText())
//                    }
//
//                    launch {
//                        delay(500L) // 修改为 0.5 秒
//                        if (viewModel.longTouchCounter == isLongClick && hasText) {
//                            Log.w(TAG, "Long press: ${viewModel.ocrTextSelected.text}")
//                            synthesizeAndPlayText(viewModel.ocrTextSelected.text, "en-US", 1.0f, AzureTextSynthesis("en-GB-SoniaNeural"))
//                        }
//                    }
//                }
//
//                is PressInteraction.Release -> {
//                    viewModel.incrementLongTouch()
//                    Log.w(TAG, "Release press: ${viewModel.longTouchCounter}")
//                }
//            }
//        }
//    }
//
//    val image = sharedViewModel.image.collectAsStateWithLifecycle().value!!
//    val viewSize = sharedViewModel.size
//
//    LaunchedEffect(viewModel.ocrTextList) {
//        if (viewModel.ocrTextList == null) {
//            val imageSize = IntSize(image.width, image.height)
//            analyzeOCR(viewSize = viewSize, imageSize, fileName, context, viewModel::onTextRecognized)
//        }
//    }
//
//    val zoom = remember { mutableStateOf(1f) }
//    val offsetX = remember { mutableStateOf(0f) }
//    val offsetY = remember { mutableStateOf(0f) }
//    val angle = remember { mutableStateOf(0f) }
//
//    Surface(modifier = modifier.background(Color.Transparent)) {
//        Box(modifier = Modifier.fillMaxSize()) {
//            if (viewModel.ocrTextList != null) {
//                if (viewModel.ocrTextList!!.isEmpty()) {
//                    Text(
//                        "拍摄的图像不包含文字。可能是因为按下拍摄按钮时移动太快，或者图像不够清晰导致模糊。请返回上一页重新拍照。",
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                } else {
//                    Box(modifier = Modifier.fillMaxSize()) {
//                        Image(
//                            bitmap = bitmap!!.asImageBitmap(),
//                            contentDescription = "Image",
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .graphicsLayer(
//                                    scaleX = zoom.value,
//                                    scaleY = zoom.value,
//                                    rotationZ = angle.value,
//                                    translationX = offsetX.value,
//                                    translationY = offsetY.value
//                                )
//                                .pointerInput(Unit) {
//                                    detectTransformGestures { _, pan, gestureZoom, _ ->
//                                        zoom.value *= gestureZoom
//                                        offsetX.value += pan.x
//                                        offsetY.value += pan.y
//                                    }
//                                }
//                                .clickable(interactionSource = interactionSource, indication = null) {}
//                        )
//
//                        if (viewModel.ocrTextSelected.text.isNotBlank()) {
//                            val box = adjustRect(viewModel.ocrTextSelected.rect)
//                            Canvas(modifier = Modifier
//                                .fillMaxSize()
//                                .graphicsLayer(
//                                    scaleX = zoom.value,
//                                    scaleY = zoom.value,
//                                    translationX = offsetX.value,
//                                    translationY = offsetY.value
//                                )
//                            ) {
//                                val path = Path().apply {
//                                    addRect(
//                                        rect = box
//                                    )
//                                }
//                                drawPath(path, color = Color.Yellow.copy(alpha = 0.5f))
//                            }
//                        }
//                    }
//                }
//            } else {
//                CircularProgressIndicator(
//                    modifier = Modifier
//                        .width(64.dp)
//                        .align(Alignment.Center),
//                    color = MaterialTheme.colorScheme.secondary,
//                    trackColor = MaterialTheme.colorScheme.surfaceVariant
//                )
//            }
//            IconButton(
//                onClick = { navController.navigate(Screens.CameraScreen.route) },
//                modifier = Modifier
//                    .size(75.dp)
//                    .align(Alignment.TopStart)
//                    .padding(8.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
//                    contentDescription = "Back to video",
//                    tint = Color.White,
//                    modifier = Modifier.size(30.dp)
//                )
//            }
//            IconButton(
//                onClick = { navController.navigate(Screens.HistoryScreen.route) },
//                modifier = Modifier
//                    .size(75.dp)
//                    .align(Alignment.TopEnd)
//                    .padding(8.dp)
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.history),
//                    contentDescription = "History Icon",
//                    tint = Color.White,
//                    modifier = Modifier.size(30.dp)
//                )
//            }
//            // 确保相册按钮不嵌套在历史记录按钮中
//            IconButton(
//                onClick = { navController.navigate(Screens.AlbumScreen.route) },
//                modifier = Modifier
//                    .size(75.dp)
//                    .align(Alignment.BottomEnd)
//                    .padding(8.dp)
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.album),
//                    contentDescription = "Album Icon",
//                    tint = Color.White,
//                    modifier = Modifier.size(30.dp)
//                )
//            }
//        }
//    }
//}
package com.ocrtts.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ocrtts.R
import com.ocrtts.base.AzureTextSynthesis
import com.ocrtts.ocr.adjustRect
import com.ocrtts.ocr.analyzeOCR
import com.ocrtts.type.OCRText
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import com.ocrtts.ui.viewmodels.ImageViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.io.File


fun imageToBitmap(image: Image): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.capacity())
    buffer[bytes]
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
}

fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply {
        postRotate(degrees)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun contains(rect: Rect, x: Float, y: Float): Boolean {
    return rect.left - 25 <= x && rect.right + 25 >= x && rect.top - 25 <= y && rect.bottom + 25 >= y
}


@OptIn(ExperimentalGetImage::class)
@Composable
fun ImageScreen(
    sharedViewModel: ImageSharedViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ImageViewModel = viewModel()
) {
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            val TAG = "ImagePress"
            when (interaction) {
                is PressInteraction.Press -> {
                    Log.w(TAG + "test", "Pressed: ${interaction.pressPosition}")
                    val isLongClick = viewModel.longTouchCounter

                    val position = interaction.pressPosition
                    var hasText = false
                    for (text in viewModel.ocrTextList ?: listOf()) {
                        if (contains(text.rect, position.x, position.y)) {
                            viewModel.updateTextRectSelected(text)
                            hasText = true
                            break
                        }
                    }

                    if (!hasText) {
                        viewModel.updateTextRectSelected(OCRText())
                    }

                    delay(500L)
                    if (viewModel.longTouchCounter == isLongClick && hasText) {
                        Log.w(TAG, "Long press: ${viewModel.ocrTextSelected.text}")
                        // TODO: Text to Speech
                        synthesizeAndPlayText(viewModel.ocrTextSelected.text, "en-US", 1.0f, AzureTextSynthesis("en-GB-SoniaNeural"))
                        // text: viewModel.OCRTextSelected!!.text
                        // language: en
                    }
                }

                is PressInteraction.Release -> {
                    viewModel.incrementLongTouch()
                    Log.w(TAG, "Release press: ${viewModel.longTouchCounter}")
                }
            }
        }
    }

    val context = LocalContext.current
    val fileName = sharedViewModel.fileName.collectAsStateWithLifecycle().value
    val image = sharedViewModel.image.collectAsStateWithLifecycle().value!!
    val viewSize = sharedViewModel.size

    LaunchedEffect(viewModel.ocrTextList) {
        if (viewModel.ocrTextList == null) {
            val imageSize = IntSize(image.width, image.height)
            analyzeOCR(viewSize = viewSize, imageSize, fileName, context, viewModel::onTextRecognized)
        }
    }
    val zoom = remember { mutableStateOf(1f) }
    val offsetX = remember { mutableStateOf(0f) }
    val offsetY = remember { mutableStateOf(0f) }
    val angle = remember { mutableStateOf(0f) }

    Surface(modifier = modifier.background(Color.Transparent)) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewModel.ocrTextList != null) {
                if (viewModel.ocrTextList!!.isEmpty()) {
                    Text(
                        "The image that you took does not contain text. This can happen when you press the capture button while moving too fast or the image is not focus enough and becomes blurry. Please go back to the previous page and take a picture again",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = zoom.value,
                                scaleY = zoom.value,
                                rotationZ = angle.value,
                                translationX = offsetX.value,
                                translationY = offsetY.value
                            )
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    zoom.value *= gestureZoom
                                    offsetX.value += pan.x
                                    offsetY.value += pan.y
                                }
                            }
                            .clickable(interactionSource = interactionSource, indication = null) {}
                    )

                    val test = viewModel.ocrTextList
                    for (text in test!!) {
                        val box = text.rect
                        Canvas(modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = zoom.value,
                                scaleY = zoom.value,
                                translationX = offsetX.value,
                                translationY = offsetY.value
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

//                    if (viewModel.ocrTextSelected.text.isNotBlank()) {
//                        val box = adjustRect(viewModel.ocrTextSelected.rect)
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
//                                    rect = box
//                                )
//                            }
//                            drawPath(path, color = Color.Yellow.copy(alpha = 0.5f))
//                        }
//                    }
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
            IconButton(
                onClick = { navController.navigate(Screens.AlbumScreen.route) },
                modifier = Modifier
                    .size(75.dp)
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.album),
                    contentDescription = "Album Icon",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}