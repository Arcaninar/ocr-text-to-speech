package com.ocrtts.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImageSharedViewModel : ViewModel() {
//    private val _sharedImageProxy = MutableStateFlow<ImageProxy?>(null)
//    val sharedImageProxy: StateFlow<ImageProxy?> = _sharedImageProxy.asStateFlow()
//
//    private val _imageHistory = MutableStateFlow<List<File>>(emptyList())
//    val imageHistory: StateFlow<List<File>> = _imageHistory.asStateFlow()
//
//    private val _sharedImage = MutableStateFlow<Bitmap?>(null)
//    val sharedImage: StateFlow<Bitmap?> = _sharedImage.asStateFlow()

    private val _fileName = MutableStateFlow<String>("")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

//    fun setUpImageProxy(bitmap: Bitmap) {
//        _sharedImage.value = bitmap
//    }
//
//    fun addImageToHistory(file: File) {
//        val currentHistory = _imageHistory.value
//        val updatedHistory = if (currentHistory.size >= 10) {
//            currentHistory.drop(1) + file
//        } else {
//            currentHistory + file
//        }
//        _imageHistory.value = updatedHistory
//    }

    fun setFileName(value: String) { _fileName.value = value }
}