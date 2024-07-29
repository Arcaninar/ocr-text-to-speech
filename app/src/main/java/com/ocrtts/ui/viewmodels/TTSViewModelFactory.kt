package com.ocrtts.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ocrtts.history.SettingDataStoreManager

class TTSViewModelFactory(
    private val context: Context,
    private val initialLanguage: String,
    private val initialSpeed: Float,
    private val dataStoreManager: SettingDataStoreManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TTSViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TTSViewModel(context.applicationContext as Application, initialLanguage, initialSpeed, dataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//import android.app.Application
//import android.content.Context
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.ocrtts.history.SettingDataStoreManager
//
//class TTSViewModelFactory(
//    private val application: Application,
////    private val context: Context,
//    private val dataStoreManager: SettingDataStoreManager
//) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(TTSViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return TTSViewModel(application, dataStoreManager) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
