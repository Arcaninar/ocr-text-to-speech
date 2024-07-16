package com.ocrtts.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImageSharedViewModel : ViewModel() {
    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    var size by mutableStateOf(IntSize.Zero)
        private set

    fun setFileName(value: String) { _fileName.value = value }

    fun updateSize(value: IntSize) { size = value }
}