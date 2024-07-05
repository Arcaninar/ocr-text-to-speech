package com.ocrtts.type

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect

@Immutable
data class TextRect(
    val text: String,
    val rect: Rect
)