package com.ocrtts.ui.screens

import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    //default language
    var azureTTS = remember { AzureTextSynthesis("en-GB-SoniaNeural") }

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
                    onClick = {
                        azureTTS = AzureTextSynthesis("en-GB-SoniaNeural")
                        synthesizeAndPlayText("你好，我是探路者！定义父布局可使用它来对齐和定位其子布局的偏移线。文本基线是AlignmentLines 的典型示例。Hi I am Pathfinder! Defines an offset line that can be used by parent layouts to align and position their children. Text baselines are representative examples of AlignmentLines. For example, they can be used by Row, to align its children by baseline, or by paddingFrom to achieve a layout with a specific distance from the top to the baseline of the text content. AlignmentLines can be understood as an abstraction over text baselines.", "en-US", 1.0f, azureTTS) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                TriggerButton(
                    text = "zh-HK",
                    iconId = R.drawable.ic_launcher_foreground,
                    onClick = {
                        azureTTS = AzureTextSynthesis("zh-HK-HiuMaanNeural")
                        synthesizeAndPlayText("Hi I am Pathfinder! Defines an offset line that can be used by parent layouts to align and position their children.你好，我是探路者！定义父布局可使用它来对齐和定位其子布局的偏移线。文本基线是AlignmentLines 的典型示例。例如， 可以使用它们来Row按基线对齐其子布局，或者 来paddingFrom实现从顶部到文本内容基线的特定距离的布局。AlignmentLines 可以理解为对文本基线的抽象。", "zh-HK", 1.0f, azureTTS) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                TriggerButton(
                    text = "zh-TW",
                    iconId = R.drawable.ic_launcher_foreground,
                    onClick = {
                        azureTTS = AzureTextSynthesis("zh-TW-HsiaoChenNeural")
                        synthesizeAndPlayText("Hi I am Pathfinder! Defines an offset line that can be used by parent layouts to align and position their children.嗨，我是探路者！定义父布局可使用它来对齐和定位其子布局的偏移线。文本基线是AlignmentLines 的典型示例。例如， 可以使用它们来Row按基线对齐其子布局，或者 来paddingFrom实现从顶部到文本内容基线的特定距离的布局。AlignmentLines 可以理解为对文本基线的抽象。", "zh-TW", 1.0f, azureTTS) }
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

fun TTSLangSelector(){

}


fun synthesizeAndPlayText(text: String, language: String, speed: Float, tts: AzureTextSynthesis) {
    val voice = when(language){
        "en-US" -> "en-GB-SoniaNeural"
        "zh-HK" -> "zh-HK-HiuMaanNeural"
        "zh-TW" -> "zh-TW-HsiaoChenNeural"
        else -> "en-GB-SoniaNeural"
    }
    Log.i("check", "language change")

    //update and play
    tts.updateVoice(voice)
    tts.startPlaying(text, speed)
}