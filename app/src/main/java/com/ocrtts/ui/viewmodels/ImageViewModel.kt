package com.ocrtts.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.Text
import com.ocrtts.data.TextRect

class ImageViewModel : ViewModel() {
    var textRectList: MutableState<List<TextRect>> = mutableStateOf(listOf())
        private set

    var textRectSelected: MutableState<TextRect?> = mutableStateOf(null)
        private set

    var longTouchCounter: MutableState<Int> = mutableStateOf(0)
        private set

    fun setTextRectList(list: List<TextRect>) { textRectList.value = list }

    fun setTextRectSelected(value: TextRect?) { textRectSelected.value = value }

    fun incrementLongTouch() { longTouchCounter.value += 1 }

    fun setRecognizedText(text: Text) {
        rotate(text.textBlocks, 90)
    }

    fun rotate(
        textBlocks: List<Text.TextBlock>,
        rotation: Int
    ) {
        Log.w("Rotation", rotation.toString())
        val updatedTextRects: MutableList<TextRect> = mutableListOf()

        when (rotation) {
            180 -> {
                modifyRectSize(
                    textBlocks,
                    updatedTextRects,
                    top = 2.25f,
                    bottom = 2.325f,
                    left = 2.1f,
                    right = 2.3f
                )
            }

            270 -> {
                modifyRectSize(
                    textBlocks,
                    updatedTextRects,
                    top = 2.25f,
                    bottom = 2.275f,
                    left = 2.025f,
                    right = 2.3f
                )
            }

            0 -> {
                modifyRectSize(
                    textBlocks,
                    updatedTextRects,
                    top = 2.225f,
                    bottom = 2.275f,
                    left = 2.2f,
                    right = 2.3f
                )
            }

            else -> {
                modifyRectSize(
                    textBlocks,
                    updatedTextRects,
                    top = 2.2f,
                    bottom = 2.25f,
                    left = 1.85f,
                    right = 2.25f
                )
            }
        }
        textRectList.value = updatedTextRects
    }

    private fun modifyRectSize(
        textBlocks: List<Text.TextBlock>,
        updatedTextRects: MutableList<TextRect>,
        top: Float = 1f,
        bottom: Float = 1f,
        left: Float = 1f,
        right: Float = 1f
    ) {
        for (text in textBlocks) {
            if (text.boundingBox != null) {
                val textBlock = text.boundingBox!!
                updatedTextRects.add(
                    TextRect(
                        text.text, Rect(
                            top = textBlock.top.toFloat() * top,
                            bottom = textBlock.bottom.toFloat() * bottom,
                            left = textBlock.left.toFloat() * left,
                            right = textBlock.right.toFloat() * right,
                        )
                    )
                )
            }
        }
    }
}
