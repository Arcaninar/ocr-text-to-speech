package com.ocrtts.ui.selected_image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
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
fun ImageScreen(viewModel: MainViewModel, navController: NavController, fileName: String?, modifier: Modifier = Modifier) {
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(fileName) {
        bitmap.value = fileName?.let { BitmapFactory.decodeFile(it) }
    }

    bitmap.value?.let { bmp ->
        Surface(modifier = modifier.background(Color.Transparent)) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Image",
                    modifier = Modifier.fillMaxHeight()
                )
                IconButton(
                    onClick = {
                        navController.navigate("cameraScreen")
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
                        painter = painterResource(id = R.drawable.history),
                        contentDescription = "History Icon",
                        tint = Color.Black, // 设置图标颜色
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}