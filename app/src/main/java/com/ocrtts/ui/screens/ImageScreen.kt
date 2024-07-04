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
import androidx.compose.runtime.mutableStateOf
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
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import com.ocrtts.ui.viewmodels.ImageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalGetImage::class)
@Composable
fun ImageScreen(fileName: String, navController: NavController, sharedViewModel: ImageSharedViewModel, modifier: Modifier = Modifier, viewModel: ImageViewModel = viewModel()) {
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            val TAG = "ImagePress"
            when (interaction) {
                is PressInteraction.Press -> {
                    Log.w(TAG, "Pressed: " + viewModel.longTouchCounter)
                    val isLongClick = viewModel.longTouchCounter

                    val position = interaction.pressPosition
                    var hasText = false
                    for (text in viewModel.textRectList) {
                        if (contains(text.rect, position.x, position.y)) {
                            viewModel.updateTextRectSelected(text)
                            hasText = true
                            break
                        }
                    }

                    if (hasText.not()) {
                        viewModel.updateTextRectSelected(null)
                    }

                    delay(3000L)
                    if (viewModel.longTouchCounter == isLongClick && hasText) {
                        Log.w(TAG, "Long press: " + viewModel.textRectSelected!!.text)
                        //TODO: Text to Speech
                    }

                }

                is PressInteraction.Release -> {
                    viewModel.incrementLongTouch()
                    Log.w(TAG, "Release press: " + viewModel.longTouchCounter)
                }
            }
        }
    }

    lateinit var image: Bitmap
    if (fileName.isBlank()) {
        val imageProxy by sharedViewModel.sharedImageProxy.collectAsStateWithLifecycle()
        val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
//        CameraTextAnalyzer(viewModel::setRecognizedText, coroutineScope).analyze(imageProxy!!)

        image = imageToBitmap(imageProxy!!.image!!)
    }
    else {
        val bitmap = remember { mutableStateOf<Bitmap?>(null) }
        bitmap.value = fileName.let { filePath ->
            val originalBitmap = BitmapFactory.decodeFile(filePath)
            originalBitmap?.let { rotateBitmap(it, 90f) }
            originalBitmap
        }
        image = bitmap.value!!
    }


    Surface(modifier = modifier.background(Color.Transparent)) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)) {
            if (viewModel.isFinishedAnalysing) {
                if (!viewModel.containText) {
                    Text("The image that you took does not contain text. This can happened when you press the capture button while moving too fast or the image is not focus enough and becomes blurry. Please go back to the previous page and take a picture again", modifier = Modifier.align(Alignment.Center))
                }
                else {
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
                }
            }
            else {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp).align(Alignment.Center),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            IconButton(
                onClick = {
                    navController.navigate(Screens.CameraScreen.route)
                },
                modifier = Modifier
                    .size(75.dp)
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back to video",
                    tint = Color.White, // 设置箭头颜色为黑色
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = {
                    navController.navigate(Screens.HistoryScreen.route)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.history),
                    contentDescription = "History Icon",
                    tint = Color.White, // 设置图标颜色
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        if (viewModel.textRectSelected != null) {
            val box = viewModel.textRectSelected!!.rect
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


private fun imageToBitmap(image: Image): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.capacity())
    buffer[bytes]
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply {
        postRotate(degrees)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun contains(rect: Rect, x: Float, y: Float): Boolean {
    return rect.left - 25 <= x && rect.right + 25 >= x && rect.top - 25 <= y && rect.bottom + 25 >= y
}