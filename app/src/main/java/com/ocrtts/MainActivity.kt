package com.ocrtts

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.ocrtts.history.DataStoreManager
import com.ocrtts.ui.screens.MainScreen
import com.ocrtts.ui.theme.OCRTextToSpeechTheme
import com.ocrtts.ui.viewmodels.SettingViewModel
import com.ocrtts.ui.viewmodels.SettingViewModelFactory
import java.io.File

// temporary variables, will be moved to better place/structure
lateinit var notificationSound: MediaPlayer
lateinit var imageCacheFile: File

class MainActivity : ComponentActivity() {

    private lateinit var settingViewModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataStoreManager = DataStoreManager(applicationContext)
        val factory = SettingViewModelFactory(dataStoreManager)
        settingViewModel = ViewModelProvider(this, factory)[SettingViewModel::class.java]

        val SDK_INT = Build.VERSION.SDK_INT
        if (SDK_INT > 8) {
            val policy = ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        setContent {
            notificationSound = MediaPlayer.create(LocalContext.current, R.raw.ding)
            imageCacheFile = File(LocalContext.current.getExternalFilesDir(null), "image_cache.jpeg")
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

    override fun onDestroy() {
        super.onDestroy()
        notificationSound.stop()
        notificationSound.release()
    }
}
