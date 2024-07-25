package com.ocrtts.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ocrtts.ui.viewmodels.SettingViewModel
import kotlinx.coroutines.launch


@Composable
fun SettingScreen(
    navController: NavController,
    settingViewModel: SettingViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val selectedLanguage by settingViewModel.langModel.collectAsState()
    val speed by settingViewModel.speedRate.collectAsState()
    val selectedModel by settingViewModel.modelType.collectAsState()

    var showLanguageSelection by remember { mutableStateOf(selectedModel == "onlineTTS") }

    LaunchedEffect(settingViewModel) {
        showLanguageSelection = selectedModel == "onlineTTS"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModelSelection(
            selectedModel = selectedModel,
            onModelSelected = { newModel ->
                scope.launch {
                    settingViewModel.updateModelType(newModel)
                }
            },
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))
        LanguageSelection(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { newLanguage ->
                scope.launch {
                    settingViewModel.updateLangModel(newLanguage)
                }
            },
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))
        SpeedInput(speed = speed, onSpeedChange = { newSpeed ->
            scope.launch {
                settingViewModel.updateSpeedRate(newSpeed)
            }
        }, modifier = Modifier)
        Spacer(modifier = Modifier.height(16.dp))
        ExampleText(speed = speed, modifier = Modifier)
        Spacer(modifier = Modifier.height(16.dp))
        SaveButton(onClick = {

            navController.navigate(Screens.HomeScreen.route)
        }, modifier = Modifier)
    }
    Log.i("check", "ready to back")
}

@Composable
fun ModelSelection(
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val models = listOf("onlineTTS", "offlineTTS")

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = "Choose a model: $selectedModel", fontSize = 20.sp)
        models.forEach { model ->
            val isSelected = (selectedModel == model)
            Button(
                onClick = { onModelSelected(model) },
                colors = if (isSelected) ButtonDefaults.buttonColors(Color.Blue) else ButtonDefaults.buttonColors(),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(text = model)
            }
        }
    }
}

@Composable
fun LanguageSelection(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val languages = listOf("zh-HK-HiuMaanNeural", "zh-TW-HsiaoChenNeural", "en-GB-SoniaNeural")

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = "Choose a language model: $selectedLanguage", fontSize = 20.sp)
        languages.forEach { language ->
            val isSelected = (selectedLanguage == language)
            Button(
                onClick = { onLanguageSelected(language) },
                colors = if (isSelected) ButtonDefaults.buttonColors(Color.Blue) else ButtonDefaults.buttonColors(),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(text = language)
            }
        }
    }
}

@Composable
fun SpeedInput(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = "Set the Speaking rate", fontSize = 20.sp)
        var speedText by remember { mutableStateOf(speed.toString()) }

        TextField(
            value = speedText,
            onValueChange = { newValue ->
                speedText = newValue
                newValue.toFloatOrNull()?.let { onSpeedChange(it) }
            },
            label = { Text("1.0") }
        )
    }
}

@Composable
fun ExampleText(
    speed: Float,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = "Current speaking speed: $speed", fontSize = 16.sp)
    }
}

@Composable
fun SaveButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(Color.LightGray),
        modifier = modifier
    ) {
        Text(text = "Save Settings", color = Color.Black)
    }
}

