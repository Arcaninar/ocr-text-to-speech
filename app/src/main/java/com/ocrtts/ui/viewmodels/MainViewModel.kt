package com.ocrtts.ui.viewmodels

import android.graphics.Point
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.ocrtts.ui.screens.TextRect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

//TODO
//Still reading

const val TAG="MainViewModel"


data class RecognizedTextBlock(
    val text: String,
    val rect: List<Point>
)


data class DrawableTextBlock(
    val topLeft: Offset,
    val size: Size
)

class MainViewModel : ViewModel() {


    private val _recognizedTextBlocks = MutableStateFlow<List<RecognizedTextBlock>>(emptyList())
    val recognizedTextBlocks = _recognizedTextBlocks.asStateFlow()


    private val _drawableTextBlocks = MutableStateFlow<List<DrawableTextBlock>>(emptyList())
    val drawableTextBlocks = _drawableTextBlocks.asStateFlow()


//    fun onTextRecognized(result: mutableListOf<RecognizedTextBlock>()) {
////        _recognizedText.value = text
////        Log.i(TAG,text)
//    }
fun onTextRecognized(textBlocks: List<RecognizedTextBlock>, imageWidth: Int, imageHeight: Int, rotation: Int) {
    viewModelScope.launch {
        val drawableBlocks = textBlocks.mapNotNull { block ->
            if (block.rect.size != 4) return@mapNotNull null

            val adjustedPoints = when (rotation) {
                90 -> block.rect.map { Point(imageHeight - it.y, it.x) }
                180 -> block.rect.map { Point(imageWidth - it.x, imageHeight - it.y) }
                270 -> block.rect.map { Point(it.y, imageWidth - it.x) }
                else -> block.rect // 0 度
            }

            val minX = adjustedPoints.minOf { it.x }.toFloat()
            val minY = adjustedPoints.minOf { it.y }.toFloat()
            val maxX = adjustedPoints.maxOf { it.x }.toFloat()
            val maxY = adjustedPoints.maxOf { it.y }.toFloat()

            DrawableTextBlock(
                topLeft = Offset(minX / imageWidth, minY / imageHeight),
                size = Size((maxX - minX) / imageWidth, (maxY - minY) / imageHeight)
            )
        }
        _drawableTextBlocks.emit(drawableBlocks)
    }
}
}