package com.ocrtts.ui.selected_image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ocrtts.R
import com.ocrtts.ui.MainViewModel

@Composable
fun ImageScreen(viewModel: MainViewModel, navController: NavController, modifier: Modifier = Modifier) {
    val image = imageToBitmap(viewModel.imageSelected.value!!)

    Surface(modifier = modifier.background(Color.Transparent)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "Image",
                modifier = Modifier.fillMaxHeight()
            )
            IconButton(
                onClick = {
                    navController.navigate("cameraScreen") // 使用导航控制器返回到 CameraScreen
                },
                modifier = Modifier
                    .size(75.dp)
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back to video",
                    tint = Color.Black, // 设置箭头颜色为黑色
                    modifier = Modifier.size(24.dp)
                )

            }
            IconButton(
                onClick = {
                    navController.navigate("historyScreen")
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.history), // 使用你的历史图标资源ID
                    contentDescription = "History Icon",
                    tint = Color.Black, // 设置图标颜色
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

// 将 Image 转换为 Bitmap 的函数
private fun imageToBitmap(image: Image): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.capacity())
    buffer[bytes]
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
}