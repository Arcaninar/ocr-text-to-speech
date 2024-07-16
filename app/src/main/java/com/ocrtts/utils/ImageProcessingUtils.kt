package com.ocrtts.utils

import android.graphics.Bitmap
import android.graphics.Rect as AndroidRect
import com.ocrtts.type.OCRText
fun processImage(bitmap: Bitmap, ocrTextList: List<OCRText>, screenWidth: Int, screenHeight: Int): List<Bitmap> {
    val textRects = ocrTextList.map { it.rect }
    val processedImages = mutableListOf<Bitmap>()

    for (rect in textRects) {
        // 调整负数坐标
        val adjustedLeft = rect.left.coerceAtLeast(0f)
        val adjustedTop = rect.top.coerceAtLeast(0f)
        val adjustedRight = rect.right.coerceAtMost(bitmap.width.toFloat())
        val adjustedBottom = rect.bottom.coerceAtMost(bitmap.height.toFloat())

        // 重新计算宽度和高度
        val adjustedWidth = (adjustedRight - adjustedLeft).toInt()
        val adjustedHeight = (adjustedBottom - adjustedTop).toInt()

        // 检查调整后的宽度和高度
        if (adjustedWidth > 0 && adjustedHeight > 0) {
            val textBitmap = Bitmap.createBitmap(
                bitmap,
                adjustedLeft.toInt(),
                adjustedTop.toInt(),
                adjustedWidth,
                adjustedHeight
            )

            // 适当放大文本框
            val scaleFactor = 1.5 // 适当放大比例
            val scaledWidth = (textBitmap.width * scaleFactor).toInt()
            val scaledHeight = (textBitmap.height * scaleFactor).toInt()

            val scaledBitmap = Bitmap.createScaledBitmap(textBitmap, scaledWidth, scaledHeight, false)
            processedImages.add(scaledBitmap)
        } else {
            // Log or handle the case where the rect is invalid
            println("Invalid rect after adjustment: ($adjustedLeft, $adjustedTop, $adjustedRight, $adjustedBottom)")
        }
    }

    println("Processed images count: ${processedImages.size}")
    return processedImages
}