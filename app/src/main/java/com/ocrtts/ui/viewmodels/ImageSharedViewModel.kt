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

    private val _isFromHistory = MutableStateFlow(false)
    val isFromHistory: StateFlow<Boolean> = _isFromHistory.asStateFlow()

    private val _orientation = MutableStateFlow(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    val orientation: StateFlow<Int> = _orientation.asStateFlow()

    var size by mutableStateOf(IntSize.Zero)
        private set

    fun updateImage(image: Bitmap) { _image.value = image }

    fun updateImageInfo(image: Bitmap, isFromHistory: Boolean, orientation: Int) {
        _image.value = image
        _isFromHistory.value = isFromHistory
        _orientation.value = orientation
    }

    fun updateSize(value: IntSize) { size = value }
}