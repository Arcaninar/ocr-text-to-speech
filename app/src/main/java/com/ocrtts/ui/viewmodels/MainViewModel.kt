package com.ocrtts.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
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

data class RandomBox(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color
)

data class RecognizedTextBlock(
    val text: String,
    val rect: android.graphics.Rect?
)

class MainViewModel : ViewModel() {
    private val _randomBoxes = mutableStateListOf<RandomBox>()
    val randomBoxes: List<RandomBox> = _randomBoxes

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _recognizedTextBlocks = MutableStateFlow<List<RecognizedTextBlock>>(emptyList())
    val recognizedTextBlocks = _recognizedTextBlocks.asStateFlow()

//    var textRectList: MutableState<List<TextRect>> = mutableStateOf(listOf())
//
//    var textRectSelected: MutableState<TextRect?> = mutableStateOf(null)
//        private set
//
//    var previousHasText: MutableState<Boolean> = mutableStateOf(false)
//        private set
//
//    var longTouchCounter: MutableState<Int> = mutableStateOf(0)
//        private set
//
//    var imageSelected: MutableState<Image?> = mutableStateOf(null)
//
//    fun setTextRectList(list: List<TextRect>) { textRectList.value = list }
//
//    fun setTextRectSelected(value: TextRect?) { textRectSelected.value = value }
//
//    fun setPreviousHasText(value: Boolean) { previousHasText.value = value }
//
//    fun incrementLongTouch() { longTouchCounter.value += 1 }
//
//    fun setImageSelected(image: Image?) { imageSelected.value = image }

    private val _recognizedText = MutableStateFlow<String>("")
    val recognizedText = _recognizedText.asStateFlow()

//    fun onTextRecognized(result: mutableListOf<RecognizedTextBlock>()) {
////        _recognizedText.value = text
////        Log.i(TAG,text)
//    }
    fun onTextRecognized(textBlocks: List<RecognizedTextBlock>) {
        viewModelScope.launch {
            _recognizedTextBlocks.emit(textBlocks)
            Log.i(TAG, "Recognized ${textBlocks.size} text blocks")
            textBlocks.forEachIndexed { index, block ->
                Log.d(TAG, "Block $index: ${block.text}, Bounds: ${block.rect}")
            }
        }
    }

    suspend fun toggleBoxGeneration(maxWidth: Float, maxHeight: Float) {
        _isGenerating.value = !_isGenerating.value
        while (_isGenerating.value) {
            addRandomBox(maxWidth, maxHeight)
            delay(100) // 每100毫秒添加一个新的box
        }
    }

    private fun addRandomBox(maxWidth: Float, maxHeight: Float) {
        _randomBoxes.add(
            RandomBox(
                x = Random.nextFloat() * maxWidth,
                y = Random.nextFloat() * maxHeight,
                size = Random.nextFloat() * (50f - 20f) + 20f,
                color = Color(
                    red = Random.nextFloat(),
                    green = Random.nextFloat(),
                    blue = Random.nextFloat(),
                    alpha = 0.5f
                )
            )
        )
        if (_randomBoxes.size > 100) {
            _randomBoxes.removeAt(0)
        }
    }
}