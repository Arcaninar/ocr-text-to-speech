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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ocrtts.R
import com.ocrtts.camera.analyzeOCR
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

    val fileName by sharedViewModel.fileName.collectAsStateWithLifecycle()
    val file = File(fileName)
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)

    val viewSize = sharedViewModel.size

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            val TAG = "ImagePress"
            when (interaction) {
                is PressInteraction.Press -> {
                    Log.w(TAG + "test", "Pressed: ${interaction.pressPosition}")
                    val isLongClick = viewModel.longTouchCounter

                    val position = interaction.pressPosition
                    var hasText = false
                    for (text in viewModel.OCRTextList) {
                        if (contains(text.rect, position.x, position.y)) {
                            viewModel.updateTextRectSelected(text)
                            hasText = true
                            break
                        }
                    }

                    if (!hasText) {
                        viewModel.updateTextRectSelected(null)
                    }

                    delay(3000L)
                    if (viewModel.longTouchCounter == isLongClick && hasText) {
                        Log.w(TAG, "Long press: ${viewModel.OCRTextSelected?.text}")
                        // TODO: Text to Speech
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

    LaunchedEffect(viewModel.isFinishedAnalysing) {
        if (!viewModel.isFinishedAnalysing) {
            val textRectList = analyzeOCR(image = bitmap, viewSize = viewSize)

            viewModel.finishedAnalyzing()
            if (textRectList.isNotEmpty()) {
                viewModel.imageContainsText()
                viewModel.updateTextRectList(textRectList)
            }
        }
    }

    Surface(modifier = modifier.background(Color.Transparent)) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewModel.isFinishedAnalysing) {
                if (!viewModel.containText) {
                    Text("The image that you took does not contain text. This can happen when you press the capture button while moving too fast or the image is not focus enough and becomes blurry. Please go back to the previous page and take a picture again", modifier = Modifier.align(Alignment.Center))
                }
                else {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {})
                    )
//                        if (viewModel.OCRTextSelected != null) {
//                            val box = viewModel.OCRTextSelected!!.rect
//                            Canvas(modifier = Modifier.fillMaxSize()) {
//                                val path = Path().apply {
//                                    addRect(
//                                        rect = Rect(
//                                            left = box.left,
//                                            right = box.right,
//                                            top = box.top,
//                                            bottom = box.bottom
//                                        )
//                                    )
//                                }
//                                drawPath(path, color = Color.Yellow.copy(alpha = 0.5f))
//                            }
//                        }
                    val test = viewModel.OCRTextList
                    for (text in test) {
                        val box = text.rect
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