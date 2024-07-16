package com.ocrtts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ocrtts.ui.screens.MainScreen
import com.ocrtts.ui.theme.OCRTextToSpeechTheme
import androidx.lifecycle.ViewModelProvider
import com.ocrtts.history.DataStoreManager
import com.ocrtts.ui.viewmodels.SettingViewModel
import com.ocrtts.ui.viewmodels.SettingViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var settingViewModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataStoreManager = DataStoreManager(applicationContext)
        val factory = SettingViewModelFactory(dataStoreManager)
        settingViewModel = ViewModelProvider(this, factory).get(SettingViewModel::class.java)

        setContent {
            OCRTextToSpeechTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(settingViewModel)
                }
            }
        }
    }
}
