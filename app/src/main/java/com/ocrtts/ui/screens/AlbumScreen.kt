package com.ocrtts.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ocrtts.camera.analyzeOCR
import com.ocrtts.ui.viewmodels.ImageSharedViewModel
import com.ocrtts.ui.viewmodels.ImageViewModel
import com.ocrtts.utils.processImage
import java.io.File
import kotlin.math.roundToInt

@Composable
fun AlbumScreen(
    sharedViewModel: ImageSharedViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ImageViewModel = viewModel()
) {
    // 获取传递过来的图片路径
    val fileName by sharedViewModel.fileName.collectAsStateWithLifecycle()
    val file = File(fileName)
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)

    LaunchedEffect(viewModel.ocrTextList) {
        if (viewModel.ocrTextList == null) {
            analyzeOCR(image = bitmap, viewSize = sharedViewModel.size, viewModel::onTextRecognized)
        }
    }

    // 获取 OCR 文本列表并处理图片
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels

    val processedImages = remember(viewModel.ocrTextList) {
        viewModel.ocrTextList?.let { ocrTextList ->
            processImage(bitmap, ocrTextList, screenWidth, screenHeight)
        }
    }

    var selectedImage by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var angle by remember { mutableStateOf(0f) }
    var zoom by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Surface(modifier = modifier.background(Color.Transparent)) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (processedImages != null) {
                if (selectedImage == null) {
                    // 显示处理后的图片集合
                    LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 128.dp)) {
                        items(processedImages.size) { index ->
                            val image = processedImages[index]
                            Image(
                                bitmap = image.asImageBitmap(),
                                contentDescription = "Processed Image",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .aspectRatio(1f)
                                    .clickable {
                                        selectedImage = image
                                        angle = 0f
                                        zoom = 3f // 设置初始缩放比例
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                            )
                        }
                    }
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        Image(
                            bitmap = selectedImage!!.asImageBitmap(),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .sizeIn(maxWidth = (screenWidth * 0.9).dp, maxHeight = (screenHeight * 0.9).dp)
                                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                                .graphicsLayer(
                                    scaleX = zoom,
                                    scaleY = zoom,
                                    rotationZ = angle
                                )
                                .pointerInput(Unit) {
                                    detectTransformGestures(
                                        onGesture = { _, pan, gestureZoom, gestureRotate ->
                                            angle += gestureRotate
                                            zoom *= gestureZoom
                                            offsetX += pan.x
                                            offsetY += pan.y
                                        }
                                    )
                                }
                        )
                    }
                    IconButton(
                        onClick = { selectedImage = null },
                        modifier = Modifier
                            .size(75.dp)
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back to album grid",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
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
            if (selectedImage == null) {
                IconButton(
                    onClick = { navController.navigate("image") },
                    modifier = Modifier
                        .size(75.dp)
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back to Image Screen",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}