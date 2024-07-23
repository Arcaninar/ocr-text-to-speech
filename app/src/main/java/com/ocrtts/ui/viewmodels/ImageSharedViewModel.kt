package com.ocrtts.ui.viewmodels

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImageSharedViewModel : ViewModel() {
    private val _image = MutableStateFlow<Bitmap?>(null)
    val image: StateFlow<Bitmap?> = _image.asStateFlow()
    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _isFromHistory = MutableStateFlow(false)
    val isFromHistory: StateFlow<Boolean> = _isFromHistory.asStateFlow()

    var size by mutableStateOf(IntSize.Zero)
        private set

    var orientation by mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        private set

    fun setImageInfo(path: String, image: Bitmap) {
        _fileName.value = path
        _image.value = image
    }

    fun updateFromHistory(value: Boolean) { _isFromHistory.value = value}

    fun updateSize(value: IntSize) { size = value }

    fun updateOrientation(value: Int) { orientation = value }
}