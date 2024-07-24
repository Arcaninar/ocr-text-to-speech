package com.ocrtts.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class ImageViewModelFactory(
    private val application: Application,
    private val settingViewModel: SettingViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageViewModel::class.java)) {
            return ImageViewModel(application, settingViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
