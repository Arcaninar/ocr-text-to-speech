package com.ocrtts.ui.viewmodels

import android.graphics.Bitmap
import android.media.Image
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImageSharedViewModel : ViewModel() {
    private val _sharedImageProxy = MutableStateFlow<ImageProxy?>(null)
    val sharedImageProxy = _sharedImageProxy.asStateFlow()

    private val _sharedImage = MutableStateFlow<Bitmap?>(null)
    val sharedImage = _sharedImage.asStateFlow()

    fun setUpImageProxy(imageProxy: ImageProxy) {
        _sharedImageProxy.value = imageProxy
    }
}
