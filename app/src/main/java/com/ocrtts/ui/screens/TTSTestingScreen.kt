package com.ocrtts.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ocrtts.R
import com.ocrtts.base.AzureTextSynthesis

@Composable
fun TTSTestingScreen(navController: NavController, modifier: Modifier = Modifier){

    Box(
        modifier = modifier.fillMaxSize()
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TriggerButton(
                    text = "en",
                    iconId = R.drawable.ic_launcher_foreground,
                    onClick = { synthesizeAndPlayText("Hi I am Pathfinder!", "en-US") }
                )
                Spacer(modifier = Modifier.width(16.dp))
                TriggerButton(
                    text = "zh-HK",
                    iconId = R.drawable.ic_launcher_foreground,
                    onClick = { synthesizeAndPlayText("嗨，我是探路者！", "zh-HK") }
                )
                Spacer(modifier = Modifier.width(16.dp))
                TriggerButton(
                    text = "zh-TW",
                    iconId = R.drawable.ic_launcher_foreground,
                    onClick = { synthesizeAndPlayText("嗨，我是探路者！", "zh-TW") }
                )
            }
//            Button(
//                onClick = { /*TODO*/ },
//                shape = CircleShape,
//                colors = ButtonDefaults.buttonColors(Color.LightGray),
//                modifier = Modifier.size(100.dp)
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
//                    contentDescription = "en",
//                    tint = Color.Black,
//                    modifier = Modifier.size(100.dp)
//                )
//            }
        }

        IconButton(
            onClick = { navController.navigate(Screens.HomeScreen.route) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_return),
                contentDescription = "Return Icon",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

    }
}

@Composable
fun TriggerButton(text: String, iconId: Int, onClick: () -> Unit, modifier: Modifier = Modifier){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(Color.LightGray),
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = text,
                tint = Color.Black,
                modifier = Modifier.size(50.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


fun synthesizeAndPlayText(text: String, language: String) {
    val tts = AzureTextSynthesis()

    tts.language = language
    tts.text = text

    tts.synthesizeAndPlay()
}
