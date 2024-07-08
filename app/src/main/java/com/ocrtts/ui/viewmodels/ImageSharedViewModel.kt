package com.ocrtts.ui.viewmodels

import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImageSharedViewModel : ViewModel() {
    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _size = MutableStateFlow(IntSize.Zero)
    val size: StateFlow<IntSize> = _size.asStateFlow()

    fun setFileName(value: String) { _fileName.value = value }

    fun updateSize(value: IntSize) { _size.value = value }
}