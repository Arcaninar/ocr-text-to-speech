package com.ocrtts.type

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect

@Immutable
data class OCRText(
    val text: String = "",
    val rect: Rect = Rect(0f, 0f, 0f,0f)
)