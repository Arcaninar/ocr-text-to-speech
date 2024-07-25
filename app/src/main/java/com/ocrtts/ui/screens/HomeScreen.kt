package com.ocrtts.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PestControl
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ocrtts.R
import com.ocrtts.ui.components.CustomIconButton

@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val activity = LocalContext.current as Activity
    BackHandler {
        activity.finish()
    }

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
        CustomIconButton(
            icon = Icons.Rounded.Settings,
            description = "Go to Settings",
            modifier = Modifier.align(Alignment.TopStart),
            innerPadding = 5.dp
        ) {
            navController.navigate(Screens.SettingScreen.route)
        }
        CustomIconButton(
            icon = Icons.Rounded.History,
            description = "Go to History",
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            navController.navigate(Screens.HistoryScreen.route)
        }
        CustomIconButton(
            icon = Icons.Rounded.PestControl,
            description = "Debugging",
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            navController.navigate(Screens.TTSTestingScreen.route)
        }

//        IconButton(
//            onClick = { navController.navigate(Screens.TTSTestingScreen.route) },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp)
//        ) {
//            Icon(
//                painter = painterResource(id = R.drawable.ic_debug),
//                contentDescription = "TTSTestingScreen",
//                tint = Color.White,
//                modifier = Modifier.size(50.dp))
//        }
    }
}