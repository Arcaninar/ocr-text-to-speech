package com.ocrtts.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import java.util.LinkedList
import java.util.Queue

class HistoryViewModel : ViewModel() {
    private val maxHistorySize = 20
    private val imageHistory: Queue<Bitmap> = LinkedList()

    fun addImageToHistory(image: Bitmap) {
        if (imageHistory.size >= maxHistorySize) {
            imageHistory.poll()
        }
        imageHistory.offer(image)
    }

    fun getImageHistory(): List<Bitmap> {
        return imageHistory.toList()
    }
}