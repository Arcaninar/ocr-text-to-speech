package com.ocrtts.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ocrtts.R

@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()

    ) {
        Image(
            painter = painterResource(id = R.drawable.t001),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    navController.navigate(Screens.MainCameraScreen.route)
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(Color.Yellow),
                modifier = Modifier
                    .size(120.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = "Camera Icon",
                    tint = Color.Black, // 设置图标颜色
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Discover the endless possibilities",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(8.dp)
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
                painter = painterResource(id = R.drawable.history), // 使用你的历史图标资源ID
                contentDescription = "History Icon",
                tint = Color.White, // 设置图标颜色
                modifier = Modifier.size(30.dp)
            )
        }
    }
}