package com.ocrtts.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ThumbnailUtils
import androidx.compose.ui.unit.IntSize
import java.io.File
import java.io.FileOutputStream

fun modifyBitmap(image: Bitmap, rotationDegree: Int = 0, screenSize: IntSize = IntSize(0, 0)): Bitmap {
    var finalImage: Bitmap = image
    if (rotationDegree != 0) {
        finalImage = finalImage.rotate(rotationDegree.toFloat())
    }

    if (screenSize != IntSize(0, 0)) {
        finalImage = ThumbnailUtils.extractThumbnail(finalImage, screenSize.width, screenSize.height)
    }

    return finalImage
}

fun saveBitmapToFile(file: File, image: Bitmap) {
    val outputStream = FileOutputStream(file)
    image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.flush()
    outputStream.close()
}

// Extension function to rotate a bitmap
fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}