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
import androidx.lifecycle.lifecycleScope
import com.ocrtts.base.AzureTextSynthesis
import com.ocrtts.history.SettingDataStoreManager
import com.ocrtts.ui.screens.MainScreen
import com.ocrtts.ui.theme.OCRTextToSpeechTheme
import com.ocrtts.ui.viewmodels.ImageViewModel
import com.ocrtts.ui.viewmodels.ImageViewModelFactory
import com.ocrtts.ui.viewmodels.SettingViewModel
import com.ocrtts.ui.viewmodels.SettingViewModelFactory
import com.ocrtts.ui.viewmodels.TTSViewModel
import com.ocrtts.ui.viewmodels.TTSViewModelFactory
import kotlinx.coroutines.launch
import java.io.File


// temporary variables, will be moved to better place/structure
lateinit var notificationSound: MediaPlayer
lateinit var imageCacheFile: File

class MainActivity : ComponentActivity() {

    private lateinit var settingViewModel: SettingViewModel
    private lateinit var ttsViewModel: TTSViewModel
    private lateinit var imageViewModel: ImageViewModel

    private lateinit var azureTextSynthesis: AzureTextSynthesis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingDataStoreManager = SettingDataStoreManager(applicationContext)
        val settingFactory = SettingViewModelFactory(settingDataStoreManager)
        settingViewModel = ViewModelProvider(this, settingFactory)[SettingViewModel::class.java]

        lifecycleScope.launch {
            settingViewModel.langModel.collect { languageModel ->
                // Ensure TTSViewModel is initialized after langModel is received
                if (::ttsViewModel.isInitialized.not()) {
                    val ttsFactory = TTSViewModelFactory(application, languageModel, 1.0f, settingDataStoreManager)
                    ttsViewModel = ViewModelProvider(this@MainActivity, ttsFactory)[TTSViewModel::class.java]
                }
                // If you need to update the language in the existing TTSViewModel
                else {
                    ttsViewModel.updateLanguage(languageModel)
                }
            }
        }

        val imageFactory = ImageViewModelFactory(application, settingViewModel)
        imageViewModel = ViewModelProvider(this, imageFactory)[ImageViewModel::class.java]

        val SDK_INT = Build.VERSION.SDK_INT
        if (SDK_INT > 8) {
            val policy = ThreadPolicy.Builder().permitAll().build()
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



//class MainActivity : ComponentActivity() {
//
//    private lateinit var settingViewModel: SettingViewModel
////    private lateinit var ttsViewModel: TTSViewModel
//    private lateinit var imageViewModel: ImageViewModel
//
//    private lateinit var azureTextSynthesis: AzureTextSynthesis
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        //offline variable
//        val offlineTextSynthesis = OfflineTextSynthesis(this)
//        offlineTextSynthesis.setLanguage(Locale.US)
//
//        //AzureTTS
//        azureTextSynthesis = AzureTextSynthesis("en-GB-SoniaNeural")
//
//        // Use SettingDataStoreManager instead of DataStoreManager
//        val settingDataStoreManager = SettingDataStoreManager(applicationContext)
//        val settingFactory = SettingViewModelFactory(settingDataStoreManager)
////        val dataStoreManager = DataStoreManager(applicationContext)
////        val factory = SettingViewModelFactory(dataStoreManager)
//
////        settingViewModel = ViewModelProvider(this, factory)[SettingViewModel::class.java]
//        settingViewModel = ViewModelProvider(this, settingFactory)[SettingViewModel::class.java]
//
//        //imageviewmodelfactory
//        val imageFactory = ImageViewModelFactory(application, settingViewModel)
//        imageViewModel = ViewModelProvider(this, imageFactory)[ImageViewModel::class.java]
//
//        val SDK_INT = Build.VERSION.SDK_INT
//        if (SDK_INT > 8) {
//            val policy = ThreadPolicy.Builder()
//                .permitAll().build()
//            StrictMode.setThreadPolicy(policy)
//        }
//
//        setContent {
//            notificationSound = MediaPlayer.create(LocalContext.current, R.raw.ding)
//            imageCacheFile = File(LocalContext.current.getExternalFilesDir(null), "image_cache.jpeg")
////            com.ocrtts.speedSetting =
//            OCRTextToSpeechTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    MainScreen(settingViewModel)
//                }
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        notificationSound.stop()
//        notificationSound.release()
//    }
//}
